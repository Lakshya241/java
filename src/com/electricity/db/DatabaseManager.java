package com.electricity.db;

import com.electricity.model.Customer;
import com.electricity.model.Bill;
import com.electricity.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations using JDBC with SQLite.
 */
public class DatabaseManager {
    private static final String DB_NAME = "electricity_bill.db";
    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_NAME;

    static {
        try {
            // Register JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    /**
     * Initializes the database, creating tables and seeding the admin user if they do not exist.
     */
    public void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Create Customers Table
            String createCustomersTable = "CREATE TABLE IF NOT EXISTS customers ("
                    + "customer_id TEXT PRIMARY KEY, "
                    + "name TEXT NOT NULL, "
                    + "email TEXT, "
                    + "phone TEXT, "
                    + "address TEXT, "
                    + "meter_number TEXT UNIQUE NOT NULL"
                    + ");";
            stmt.execute(createCustomersTable);

            // Create Accounts Table (for role-based login)
            String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts ("
                    + "username TEXT PRIMARY KEY, "
                    + "password TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "customer_id TEXT, "
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createAccountsTable);

            // Create Bills Table
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

            // Seed default Admin account if not present
            seedAdminAccount(conn);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

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
     * Authenticates a user and returns their Account model if successful.
     */
    public Account authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     * Inserts a new customer and automatically generates their Customer account in a transaction.
     */
    public void addCustomer(Customer customer) throws SQLException {
        String sqlCustomer = "INSERT INTO customers (customer_id, name, email, phone, address, meter_number) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAccount = "INSERT INTO accounts (username, password, role, customer_id) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start Transaction
            
            try (PreparedStatement pstmtCust = conn.prepareStatement(sqlCustomer)) {
                pstmtCust.setString(1, customer.getCustomerId());
                pstmtCust.setString(2, customer.getName());
                pstmtCust.setString(3, customer.getEmail());
                pstmtCust.setString(4, customer.getPhone());
                pstmtCust.setString(5, customer.getAddress());
                pstmtCust.setString(6, customer.getMeterNumber());
                pstmtCust.executeUpdate();
            }
            
            // Create user login (username = meter number, password = phone)
            String sqlAccountInsert = "INSERT INTO accounts (username, password, role, customer_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtAcc = conn.prepareStatement(sqlAccountInsert)) {
                pstmtAcc.setString(1, customer.getMeterNumber());
                pstmtAcc.setString(2, customer.getPhone());
                pstmtAcc.setString(3, "Customer");
                pstmtAcc.setString(4, customer.getCustomerId());
                pstmtAcc.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Fetches all customers.
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
     * Gets customer by customer ID.
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
     * Gets customer by Meter Number.
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
     * Fetches all bills (Admin only).
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
     * Fetches bills belonging to a specific customer (User only).
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
     * Searches all bills (Admin only).
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
     * Searches bills for a specific customer (User only).
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
