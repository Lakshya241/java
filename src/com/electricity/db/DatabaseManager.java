package com.electricity.db;

import com.electricity.model.Customer;
import com.electricity.model.Bill;
import com.electricity.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations using JDBC with SQLite.
 * 
 * DESIGN PRINCIPLE: Database Integration and Separation of Concerns
 * - Fully encapsulates JDBC connection lifecycle and SQL query execution.
 * - Employs try-with-resources blocks (Automatic Resource Management) to ensure Statement,
 *   Connection, and ResultSet objects are closed automatically to prevent connection leaks.
 * - Utilizes transactions (ACID compliance) for atomic multi-table inserts.
 * - Uses PreparedStatements to defend against SQL Injection attacks.
 */
public class DatabaseManager {
    private static final String DB_NAME = "electricity_bill.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_NAME;

    // Static block to register the SQLite JDBC driver at class loading time
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in Classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Initializes database tables and seeds the administrator account.
     * Throws SQLException to be handled gracefully by UI error alerts.
     */
    public void initializeDatabase() throws SQLException {
        // Try-with-resources automatically closes Connection and Statement
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Enable SQLite foreign key constraint checks
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Customers Profile Table
            String createCustomersTable = "CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id TEXT PRIMARY KEY, "
                    + "name TEXT NOT NULL, "
                    + "email TEXT, "
                    + "phone TEXT, "
                    + "address TEXT, "
                    + "meter_number TEXT UNIQUE NOT NULL"
                    + ");";
            stmt.execute(createCustomersTable);

            // User Accounts Table (stores Admin credentials and auto-generated Customer logins)
            String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts ("
                    + "username TEXT PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "customer_id TEXT, "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createAccountsTable);

            // Bills Table (binds readings and statements to a customer profile)
            String createBillsTable = "CREATE TABLE IF NOT EXISTS bills ("
                    + "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "customer_id TEXT NOT NULL, "
                    + "prev_reading REAL NOT NULL, "
                    + "curr_reading REAL NOT NULL, "
                    + "units_consumed REAL NOT NULL, "
                    + "total_amount REAL NOT NULL, "
                    + "bill_date TEXT NOT NULL, "
                    + "payment_status TEXT DEFAULT 'Unpaid', "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createBillsTable);

            // Seed default Admin credentials if table is clean
            seedAdminAccount(conn);
        }
    }

    /**
     * Helper to obtain a database connection connection.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Seeds the initial admin account (admin/admin123) if none exists.
     */
    private void seedAdminAccount(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM accounts WHERE role = 'Admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO accounts (username, password, role, customer_id) VALUES ('admin', 'admin123', 'Admin', NULL)";
                stmt.executeUpdate(insertSql);
                System.out.println("Seeded default admin user (admin/admin123)");
            }
        }
    }

    /**
     * Validates credentials against the database.
     * Uses parameterized queries to prevent SQL Injection.
     */
    public Account authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Binding parameters securely
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("customer_id")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Registers a new customer profile and inserts a Customer Login Account.
     * 
     * DATABASE TRANSACTION PRINCIPLE:
     * - Uses setAutoCommit(false) to treat insertions as an atomic unit (All-or-Nothing).
     * - Rolls back modifications in the catch block if any error occurs to maintain database integrity.
     */
    public void addCustomer(Customer customer) throws SQLException {
        String sqlCustomer = "INSERT INTO customers (customer_id, name, email, phone, address, meter_number) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAccount = "INSERT INTO accounts (username, password, role, customer_id) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Enable manual transaction boundary
            
            // 1. Insert Customer Profile
            try (PreparedStatement pstmtCust = conn.prepareStatement(sqlCustomer)) {
                pstmtCust.setString(1, customer.getCustomerId());
                pstmtCust.setString(2, customer.getName());
                pstmtCust.setString(3, customer.getEmail());
                pstmtCust.setString(4, customer.getPhone());
                pstmtCust.setString(5, customer.getAddress());
                pstmtCust.setString(6, customer.getMeterNumber());
                pstmtCust.executeUpdate();
            }
            
            // 2. Insert Login Account (Username: Meter Number, Password: Phone)
            try (PreparedStatement pstmtAcc = conn.prepareStatement(sqlAccount)) {
                pstmtAcc.setString(1, customer.getMeterNumber());
                pstmtAcc.setString(2, customer.getPhone());
                pstmtAcc.setString(3, "Customer");
                pstmtAcc.setString(4, customer.getCustomerId());
                pstmtAcc.executeUpdate();
            }
            
            // Commit changes if both inserts succeeded
            conn.commit();
        } catch (SQLException e) {
            // Roll back modifications if transaction failed
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Re-throw to inform GUI
        } finally {
            if (conn != null) {
                conn.close(); // Return connection to system pool
            }
        }
    }

    /**
     * Retrieves all customer profiles from the database.
     */
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("meter_number")
                );
                customers.add(c);
            }
        }
        return customers;
    }

    /**
     * Fetches a customer by customer ID.
     */
    public Customer getCustomer(String customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("meter_number")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Fetches a customer by Meter Number.
     */
    public Customer getCustomerByMeter(String meterNumber) throws SQLException {
        String sql = "SELECT * FROM customers WHERE meter_number = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, meterNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("customer_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("meter_number")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Gets the latest reading (current reading of their last bill) for a customer.
     * Returns 0.0 if no prior bill exists.
     */
    public double getLatestReading(String customerId) throws SQLException {
        String sql = "SELECT curr_reading FROM bills WHERE customer_id = ? ORDER BY bill_id DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("curr_reading");
                }
            }
        }
        return 0.0;
    }

    /**
     * Inserts a new bill record.
     */
    public void addBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (customer_id, prev_reading, curr_reading, units_consumed, total_amount, bill_date, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.getCustomerId());
            pstmt.setDouble(2, bill.getPreviousReading());
            pstmt.setDouble(3, bill.getCurrentReading());
            pstmt.setDouble(4, bill.getUnitsConsumed());
            pstmt.setDouble(5, bill.getTotalAmount());
            pstmt.setString(6, bill.getBillDate());
            pstmt.setString(7, bill.getPaymentStatus());
            pstmt.executeUpdate();
        }
    }

    /**
     * Fetches all bills (Admin access).
     */
    public List<Bill> getAllBills() throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY bill_id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bill b = new Bill(
                        rs.getInt("bill_id"),
                        rs.getString("customer_id"),
                        rs.getDouble("prev_reading"),
                        rs.getDouble("curr_reading"),
                        rs.getDouble("units_consumed"),
                        rs.getDouble("total_amount"),
                        rs.getString("bill_date"),
                        rs.getString("payment_status")
                );
                bills.add(b);
            }
        }
        return bills;
    }

    /**
     * Fetches bills belonging to a specific customer (Customer access).
     */
    public List<Bill> getCustomerBills(String customerId) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE customer_id = ? ORDER BY bill_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Bill b = new Bill(
                            rs.getInt("bill_id"),
                            rs.getString("customer_id"),
                            rs.getDouble("prev_reading"),
                            rs.getDouble("curr_reading"),
                            rs.getDouble("units_consumed"),
                            rs.getDouble("total_amount"),
                            rs.getString("bill_date"),
                            rs.getString("payment_status")
                    );
                    bills.add(b);
                }
            }
        }
        return bills;
    }

    /**
     * Updates payment status of a bill.
     */
    public void updateBillStatus(int billId, String status) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ? WHERE bill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, billId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Searches all bills using wildcards (Admin access).
     */
    public List<Bill> searchBills(String query) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT b.* FROM bills b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "WHERE b.customer_id LIKE ? OR c.name LIKE ? OR c.meter_number LIKE ? OR b.payment_status LIKE ? "
                + "ORDER BY b.bill_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcardQuery = "%" + query + "%";
            pstmt.setString(1, wildcardQuery);
            pstmt.setString(2, wildcardQuery);
            pstmt.setString(3, wildcardQuery);
            pstmt.setString(4, wildcardQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Bill b = new Bill(
                            rs.getInt("bill_id"),
                            rs.getString("customer_id"),
                            rs.getDouble("prev_reading"),
                            rs.getDouble("curr_reading"),
                            rs.getDouble("units_consumed"),
                            rs.getDouble("total_amount"),
                            rs.getString("bill_date"),
                            rs.getString("payment_status")
                    );
                    bills.add(b);
                }
            }
        }
        return bills;
    }

    /**
     * Searches bills for a specific customer (Customer access).
     */
    public List<Bill> searchCustomerBills(String customerId, String query) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT b.* FROM bills b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "WHERE b.customer_id = ? AND (b.payment_status LIKE ? OR b.bill_date LIKE ?) "
                + "ORDER BY b.bill_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcardQuery = "%" + query + "%";
            pstmt.setString(1, customerId);
            pstmt.setString(2, wildcardQuery);
            pstmt.setString(3, wildcardQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Bill b = new Bill(
                            rs.getInt("bill_id"),
                            rs.getString("customer_id"),
                            rs.getDouble("prev_reading"),
                            rs.getDouble("curr_reading"),
                            rs.getDouble("units_consumed"),
                            rs.getDouble("total_amount"),
                            rs.getString("bill_date"),
                            rs.getString("payment_status")
                    );
                    bills.add(b);
                }
            }
        }
        return bills;
    }
}
