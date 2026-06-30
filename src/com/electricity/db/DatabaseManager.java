package com.electricity.db;

import com.electricity.model.Customer;
import com.electricity.model.Bill;

import java.io.File;
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
     * Initializes the database, creating tables if they do not exist.
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
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }

    /**
     * Inserts a new customer into the database.
     */
    public void addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, name, email, phone, address, meter_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustomerId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getAddress());
            pstmt.setString(6, customer.getMeterNumber());
            pstmt.executeUpdate();
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
     * Fetches all bills.
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
     * Searches bills by customer ID, name, meter number, or payment status.
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
}
