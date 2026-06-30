package com.electricity.gui;

import com.electricity.calculator.BillCalculator;
import com.electricity.calculator.SlabBillCalculator;
import com.electricity.db.DatabaseManager;
import com.electricity.exception.ValidationException;
import com.electricity.model.Bill;
import com.electricity.model.Customer;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Main application window for the Electricity Bill Generator.
 * Implements the Swing GUI, validation, and JDBC database binding.
 */
public class ElectricityBillGeneratorApp extends JFrame {

    private final DatabaseManager dbManager;
    private final BillCalculator calculator;

    // Customer Registration components
    private JTextField custIdField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField meterField;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;

    // Bill Generation components
    private JComboBox<String> customerCombo;
    private JTextField prevReadingField;
    private JTextField currReadingField;
    private JTextField unitsField;
    private JTextField dateField;
    private JComboBox<String> statusCombo;
    private JLabel calcPreviewLabel;

    // History components
    private JTable billTable;
    private DefaultTableModel billTableModel;
    private JTextField searchField;

    public ElectricityBillGeneratorApp() {
        dbManager = new DatabaseManager();
        calculator = new SlabBillCalculator();

        // Initialize Database
        try {
            dbManager.initializeDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database initialization failed:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Configure Frame
        setTitle("LogiTrack Utilities - Electricity Bill Generator");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel (Gradient)
        GradientHeaderPanel headerPanel = new GradientHeaderPanel(
                "LogiTrack Utilities",
                "Electricity Bill Generator & Customer Management System"
        );
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Add Tabs
        tabbedPane.addTab("Customer Directory", createCustomerPanel());
        tabbedPane.addTab("New Bill Generator", createBillingPanel());
        tabbedPane.addTab("Billing History", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Load Initial Data
        refreshCustomerTable();
        populateCustomerCombo();
        refreshBillTable();
    }

    /**
     * Creates the Customer registration and directory tab.
     */
    private JPanel createCustomerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Register Customer",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(33, 150, 243)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        custIdField = new JTextField(15);
        custIdField.setEditable(false);
        formPanel.add(custIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Meter Number:"), gbc);
        gbc.gridx = 1;
        meterField = new JTextField(15);
        meterField.setEditable(false);
        formPanel.add(meterField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Email Address:*"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Phone Number:*"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(15);
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Billing Address:*"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(4, 15);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(addressArea), gbc);

        // Buttons Form
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton regBtn = new JButton("Register");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regBtn.setBackground(new Color(33, 150, 243));
        regBtn.setForeground(Color.WHITE);
        
        JButton resetBtn = new JButton("Reset");
        
        btnPanel.add(regBtn);
        btnPanel.add(resetBtn);
        formPanel.add(btnPanel, gbc);

        generateCustomerAndMeterIds();

        // Right Panel: Table of existing customers
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Registered Directory",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(33, 150, 243)
        ));

        String[] cols = {"Cust ID", "Name", "Meter No", "Email", "Phone", "Address"};
        customerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(22);
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePanel.add(new JScrollPane(customerTable), BorderLayout.CENTER);

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Register Action Listener
        regBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String custId = custIdField.getText().trim();
                    String meterNum = meterField.getText().trim();
                    String name = nameField.getText().trim();
                    String email = emailField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String address = addressArea.getText().trim();

                    // Perform Exception-based Input Validation
                    validateCustomerInput(name, email, phone, address);

                    Customer customer = new Customer(custId, name, email, phone, address, meterNum);
                    dbManager.addCustomer(customer);

                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Customer registered successfully!\nID: " + custId + "\nMeter: " + meterNum,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh Views
                    refreshCustomerTable();
                    populateCustomerCombo();
                    clearCustomerForm();
                    generateCustomerAndMeterIds();

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Reset Action Listener
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCustomerForm();
                generateCustomerAndMeterIds();
            }
        });

        return mainPanel;
    }

    /**
     * Creates the Billing panel where reading details are submitted.
     */
    private JPanel createBillingPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Record Meter Readings",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(76, 175, 80)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Customer:*"), gbc);
        gbc.gridx = 1;
        customerCombo = new JComboBox<>();
        formPanel.add(customerCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Previous Reading (kWh):"), gbc);
        gbc.gridx = 1;
        prevReadingField = new JTextField(12);
        prevReadingField.setEditable(false);
        formPanel.add(prevReadingField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Current Reading (kWh):*"), gbc);
        gbc.gridx = 1;
        currReadingField = new JTextField(12);
        formPanel.add(currReadingField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Calculated Consumption:"), gbc);
        gbc.gridx = 1;
        unitsField = new JTextField(12);
        unitsField.setEditable(false);
        formPanel.add(unitsField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Billing Date:*"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(12);
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        formPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Payment Status:*"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Unpaid", "Paid"});
        formPanel.add(statusCombo, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton genBtn = new JButton("Generate Bill");
        genBtn.setBackground(new Color(76, 175, 80));
        genBtn.setForeground(Color.WHITE);
        genBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton clearBtn = new JButton("Clear");
        btnPanel.add(genBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel, gbc);

        // Right Panel: Slab Breakdown Preview
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Dynamic Slab Calculation Summary",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(76, 175, 80)
        ));
        
        calcPreviewLabel = new JLabel("<html><body><p style='color: #888;'>Select a customer and enter a current reading to preview calculations.</p></body></html>");
        calcPreviewLabel.setVerticalAlignment(JLabel.TOP);
        calcPreviewLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(calcPreviewLabel);
        previewPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(previewPanel, BorderLayout.CENTER);

        // Combo Selection Listener
        customerCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLiveCalculation();
            }
        });

        // Live input calculation listeners
        currReadingField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLiveCalculation(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLiveCalculation(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLiveCalculation(); }
        });

        // Generate Bill Action
        genBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validateBillInput();

                    String selectedItem = (String) customerCombo.getSelectedItem();
                    String meterNo = selectedItem.split(" - ")[1].trim();
                    Customer customer = dbManager.getCustomerByMeter(meterNo);
                    
                    double prev = Double.parseDouble(prevReadingField.getText());
                    double curr = Double.parseDouble(currReadingField.getText().trim());
                    double units = curr - prev;
                    double total = calculator.calculateBill(units);
                    String date = dateField.getText().trim();
                    String status = (String) statusCombo.getSelectedItem();

                    Bill bill = new Bill(0, customer.getCustomerId(), prev, curr, units, total, date, status);
                    dbManager.addBill(bill);

                    // Show Successful Invoice summary
                    showInvoiceDialog(customer, bill);

                    // Refresh Views
                    refreshBillTable();
                    clearBillForm();
                    updateLiveCalculation();

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearBillForm();
                updateLiveCalculation();
            }
        });

        return mainPanel;
    }

    /**
     * Creates the Bill History panel.
     */
    private JPanel createHistoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search Query (Name/Meter/ID/Status):"));
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        // Center Table
        String[] cols = {"Bill ID", "Cust ID", "Prev Rdg", "Curr Rdg", "Units", "Amount", "Bill Date", "Status"};
        billTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billTable = new JTable(billTableModel);
        billTable.setRowHeight(22);
        billTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(billTable);

        // Bottom Actions Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton payBtn = new JButton("Mark as Paid");
        payBtn.setBackground(new Color(76, 175, 80));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton viewBtn = new JButton("View Invoice Summary");
        viewBtn.setBackground(new Color(33, 150, 243));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        actionPanel.add(viewBtn);
        actionPanel.add(payBtn);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        // Search Action
        ActionListener searchAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = searchField.getText().trim();
                    List<Bill> bills = dbManager.searchBills(query);
                    populateBillTable(bills);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Search Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        searchBtn.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                refreshBillTable();
            }
        });

        // Pay action
        payBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = billTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select a bill from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) billTable.getValueAt(row, 0);
                try {
                    dbManager.updateBillStatus(billId, "Paid");
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Bill #" + billId + " marked as Paid.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBillTable();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // View invoice summary action
        viewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = billTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select a bill from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) billTable.getValueAt(row, 0);
                String custId = (String) billTable.getValueAt(row, 1);
                double prev = (Double) billTable.getValueAt(row, 2);
                double curr = (Double) billTable.getValueAt(row, 3);
                double units = (Double) billTable.getValueAt(row, 4);
                double amt = (Double) billTable.getValueAt(row, 5);
                String date = (String) billTable.getValueAt(row, 6);
                String status = (String) billTable.getValueAt(row, 7);

                try {
                    Customer customer = dbManager.getCustomer(custId);
                    if (customer == null) {
                        JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                                "Customer not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Bill b = new Bill(billId, custId, prev, curr, units, amt, date, status);
                    showInvoiceDialog(customer, b);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return mainPanel;
    }

    // Helper Methods & Form Logic

    private void generateCustomerAndMeterIds() {
        Random rand = new Random();
        int custNum = 1000 + rand.nextInt(9000);
        int metNum = 5000 + rand.nextInt(4000);
        custIdField.setText("CUST-" + custNum);
        meterField.setText("MET-" + metNum);
    }

    private void clearCustomerForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
    }

    private void validateCustomerInput(String name, String email, String phone, String address) throws ValidationException {
        if (name.isEmpty()) {
            throw new ValidationException("Customer Name cannot be empty.");
        }
        if (email.isEmpty()) {
            throw new ValidationException("Email Address cannot be empty.");
        }
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ValidationException("Invalid Email format (e.g., mail@example.com).");
        }
        if (phone.isEmpty()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        if (!phone.matches("^\\d{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
        if (address.isEmpty()) {
            throw new ValidationException("Address cannot be empty.");
        }
    }

    private void validateBillInput() throws ValidationException {
        String selectedItem = (String) customerCombo.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty() || selectedItem.startsWith("Select")) {
            throw new ValidationException("Please select a customer.");
        }
        String currStr = currReadingField.getText().trim();
        if (currStr.isEmpty()) {
            throw new ValidationException("Please enter the current meter reading.");
        }
        try {
            double curr = Double.parseDouble(currStr);
            if (curr < 0) {
                throw new ValidationException("Current meter reading cannot be negative.");
            }
            double prev = Double.parseDouble(prevReadingField.getText());
            if (curr < prev) {
                throw new ValidationException("Current reading (" + curr + ") cannot be less than previous reading (" + prev + ").");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Current reading must be a valid decimal number.");
        }
        
        String date = dateField.getText().trim();
        if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new ValidationException("Billing date must be in YYYY-MM-DD format.");
        }
    }

    private void clearBillForm() {
        if (customerCombo.getItemCount() > 0) {
            customerCombo.setSelectedIndex(0);
        }
        currReadingField.setText("");
        unitsField.setText("");
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        statusCombo.setSelectedIndex(0);
    }

    private void refreshCustomerTable() {
        try {
            List<Customer> list = dbManager.getAllCustomers();
            customerTableModel.setRowCount(0);
            for (Customer c : list) {
                customerTableModel.addRow(new Object[]{
                        c.getCustomerId(), c.getName(), c.getMeterNumber(),
                        c.getEmail(), c.getPhone(), c.getAddress()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateCustomerCombo() {
        try {
            List<Customer> list = dbManager.getAllCustomers();
            customerCombo.removeAllItems();
            customerCombo.addItem("Select Customer...");
            for (Customer c : list) {
                customerCombo.addItem(c.getName() + " - " + c.getMeterNumber());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshBillTable() {
        try {
            List<Bill> list = dbManager.getAllBills();
            populateBillTable(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateBillTable(List<Bill> list) {
        billTableModel.setRowCount(0);
        for (Bill b : list) {
            billTableModel.addRow(new Object[]{
                    b.getBillId(), b.getCustomerId(), b.getPreviousReading(),
                    b.getCurrentReading(), b.getUnitsConsumed(), b.getTotalAmount(),
                    b.getBillDate(), b.getPaymentStatus()
            });
        }
    }

    private void updateLiveCalculation() {
        try {
            String selectedItem = (String) customerCombo.getSelectedItem();
            if (selectedItem == null || selectedItem.isEmpty() || selectedItem.startsWith("Select")) {
                prevReadingField.setText("");
                unitsField.setText("");
                calcPreviewLabel.setText("<html><body><p style='color: #888;'>Select a customer and enter a current reading to preview calculations.</p></body></html>");
                return;
            }
            String meterNo = selectedItem.split(" - ")[1].trim();
            Customer customer = dbManager.getCustomerByMeter(meterNo);
            if (customer == null) return;

            double prevReading = dbManager.getLatestReading(customer.getCustomerId());
            prevReadingField.setText(String.format("%.2f", prevReading));

            String currStr = currReadingField.getText().trim();
            if (currStr.isEmpty()) {
                unitsField.setText("");
                calcPreviewLabel.setText("<html><body><p style='color: #888;'>Enter current reading to see calculation preview.</p></body></html>");
                return;
            }

            double currReading = Double.parseDouble(currStr);
            if (currReading < prevReading) {
                unitsField.setText("");
                calcPreviewLabel.setText("<html><body><p style='color: #ff5252;'>Error: Current reading cannot be less than previous reading (" + prevReading + ").</p></body></html>");
                return;
            }

            double units = currReading - prevReading;
            unitsField.setText(String.format("%.2f", units));

            String breakdown = calculator.getSlabBreakdown(units);
            calcPreviewLabel.setText(breakdown);
        } catch (NumberFormatException e) {
            unitsField.setText("");
            calcPreviewLabel.setText("<html><body><p style='color: #ff5252;'>Error: Current reading must be a valid decimal number.</p></body></html>");
        } catch (Exception e) {
            calcPreviewLabel.setText("<html><body><p style='color: #ff5252;'>Error: " + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * Display a beautiful visual Invoice details pop-up modal.
     */
    private void showInvoiceDialog(Customer customer, Bill bill) {
        JDialog dialog = new JDialog(this, "Invoice Summary - Bill #" + bill.getBillId(), true);
        dialog.setSize(420, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), getWidth(), 0, new Color(142, 68, 173));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(420, 65));
        headerPanel.setLayout(new GridBagLayout());
        JLabel headerLbl = new JLabel("INVOICE SUMMARY");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLbl.setForeground(Color.WHITE);
        headerPanel.add(headerLbl);

        // Body
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Utility Details Block
        JPanel metaPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        metaPanel.setBorder(BorderFactory.createTitledBorder("Billing & Customer Metadata"));
        metaPanel.add(new JLabel("Customer Name:"));
        metaPanel.add(new JLabel(customer.getName()));
        metaPanel.add(new JLabel("Customer ID:"));
        metaPanel.add(new JLabel(customer.getCustomerId()));
        metaPanel.add(new JLabel("Meter Number:"));
        metaPanel.add(new JLabel(customer.getMeterNumber()));
        metaPanel.add(new JLabel("Invoice Date:"));
        metaPanel.add(new JLabel(bill.getBillDate()));
        bodyPanel.add(metaPanel);
        bodyPanel.add(Box.createVerticalStrut(10));

        // Readings details block
        JPanel rdgPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        rdgPanel.setBorder(BorderFactory.createTitledBorder("Meter Reading Stats"));
        rdgPanel.add(new JLabel("Previous Reading:"));
        rdgPanel.add(new JLabel(bill.getPreviousReading() + " kWh"));
        rdgPanel.add(new JLabel("Current Reading:"));
        rdgPanel.add(new JLabel(bill.getCurrentReading() + " kWh"));
        rdgPanel.add(new JLabel("Units Consumed:"));
        rdgPanel.add(new JLabel(bill.getUnitsConsumed() + " kWh"));
        bodyPanel.add(rdgPanel);
        bodyPanel.add(Box.createVerticalStrut(10));

        // Cost Slab Summary Block
        JPanel costPanel = new JPanel(new BorderLayout());
        costPanel.setBorder(BorderFactory.createTitledBorder("Charge Calculation Detail"));
        JLabel costBreakdownLabel = new JLabel(calculator.getSlabBreakdown(bill.getUnitsConsumed()));
        costPanel.add(costBreakdownLabel, BorderLayout.CENTER);
        bodyPanel.add(costPanel);

        // Status block
        bodyPanel.add(Box.createVerticalStrut(10));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Payment Status: "));
        JLabel statusLbl = new JLabel(bill.getPaymentStatus().toUpperCase());
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (bill.getPaymentStatus().equalsIgnoreCase("Paid")) {
            statusLbl.setForeground(new Color(76, 175, 80));
        } else {
            statusLbl.setForeground(new Color(244, 67, 54));
        }
        statusPanel.add(statusLbl);
        bodyPanel.add(statusPanel);

        // Scroll
        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom Print / Close panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton printBtn = new JButton("Simulate Print");
        printBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(dialog,
                        "Invoice sent to simulated printer queue.\nPrinted successfully!",
                        "Printing Invoice", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        bottomPanel.add(printBtn);
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Subclassed custom JComponent panel with gradient backgrounds.
     */
    private static class GradientHeaderPanel extends JPanel {
        private final String title;
        private final String subtitle;

        public GradientHeaderPanel(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            setPreferredSize(new Dimension(800, 75));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), getWidth(), getHeight(), new Color(142, 68, 173));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2d.drawString(title, 20, 32);

            g2d.setColor(new Color(220, 220, 220));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.drawString(subtitle, 20, 52);
        }
    }

    public static void main(String[] args) {
        // Setup FlatLaf look and feel
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("FlatLaf could not be initialized. Using default look and feel.");
        }

        // Run application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ElectricityBillGeneratorApp().setVisible(true);
            }
        });
    }
}
