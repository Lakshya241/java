package com.electricity.gui;

import com.electricity.calculator.BillCalculator;
import com.electricity.calculator.SlabBillCalculator;
import com.electricity.db.DatabaseManager;
import com.electricity.exception.ValidationException;
import com.electricity.model.Account;
import com.electricity.model.Bill;
import com.electricity.model.Customer;
import com.formdev.flatlaf.FlatLightLaf;

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
 * Integrates dual-role login, user self-registration, custom cream styles, and SQLite JDBC persistence.
 */
public class ElectricityBillGeneratorApp extends JFrame {

    private final DatabaseManager dbManager;
    private final BillCalculator calculator;
    private Account currentAccount;

    // Card Layout for Login vs Main App switching
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Login screen components
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JComboBox<String> loginRoleCombo;

    // Self-Registration screen components
    private JTextField regNameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JTextArea regAddressArea;

    // Admin View components
    private JTabbedPane adminTabbedPane;
    private JTextField custIdField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextField meterField;
    private JTable customerTable;
    private DefaultTableModel customerTableModel;

    private JComboBox<String> customerCombo;
    private JTextField prevReadingField;
    private JTextField currReadingField;
    private JTextField unitsField;
    private JTextField dateField;
    private JComboBox<String> statusCombo;
    private JLabel calcPreviewLabel;

    private JTable adminBillTable;
    private DefaultTableModel adminBillTableModel;
    private JTextField adminSearchField;

    // Customer View components
    private JPanel customerMainPanel;
    private JLabel custProfileName;
    private JLabel custProfileId;
    private JLabel custProfileMeter;
    private JLabel custProfileEmail;
    private JLabel custProfilePhone;
    private JLabel custProfileAddress;
    private JTable custBillTable;
    private DefaultTableModel custBillTableModel;
    private JTextField custSearchField;

    // Warm Cream Palette Color Tokens
    private static final Color CREAM_BG = new Color(253, 251, 247);
    private static final Color CARD_BG = new Color(255, 254, 250);
    private static final Color TEXT_DARK = new Color(62, 39, 35); // Cocoa
    private static final Color ACCENT_TAN = new Color(193, 154, 107); // Caramel
    private static final Color BORDER_CREAM = new Color(230, 220, 205);

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
        setTitle("LogiTrack Utilities - Electricity Billing System");
        setSize(980, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Setup CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create Screens
        cardPanel.add(createLoginScreen(), "LOGIN");
        cardPanel.add(createRegisterScreen(), "REGISTER");
        cardPanel.add(createAdminAppScreen(), "ADMIN_APP");
        cardPanel.add(createCustomerAppScreen(), "CUSTOMER_APP");

        add(cardPanel);
        
        // Start on Login Card
        cardLayout.show(cardPanel, "LOGIN");
    }

    /**
     * Creates the centered card layout Login Screen.
     */
    private JPanel createLoginScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(CREAM_BG);

        // Login Card Panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CREAM, 1),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(380, 470));
        card.setMaximumSize(new Dimension(380, 470));

        // App Logo/Header
        JLabel logoLbl = new JLabel("LogiTrack Utilities");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLbl.setForeground(ACCENT_TAN);
        logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLbl);

        JLabel subLogoLbl = new JLabel("Electricity Billing Portal");
        subLogoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLogoLbl.setForeground(TEXT_DARK);
        subLogoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subLogoLbl);
        card.add(Box.createVerticalStrut(20));

        // Form Fields
        JPanel form = new JPanel(new GridLayout(6, 1, 3, 3));
        form.setBackground(CARD_BG);

        form.add(new JLabel("Username / Meter Number:"));
        loginUserField = new JTextField();
        loginUserField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(loginUserField);

        form.add(new JLabel("Password / Phone Number:"));
        loginPassField = new JPasswordField();
        loginPassField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(loginPassField);

        form.add(new JLabel("Portal Access Role:"));
        loginRoleCombo = new JComboBox<>(new String[]{"Admin", "Customer"});
        loginRoleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(loginRoleCombo);

        card.add(form);
        card.add(Box.createVerticalStrut(20));

        // Login Button
        JButton loginBtn = new JButton("Access Portal");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(ACCENT_TAN);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(300, 38));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginBtn);

        card.add(Box.createVerticalStrut(10));

        // Self Register Link button
        JButton registerLink = new JButton("New Customer? Register Profile");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(new Color(120, 100, 80));
        registerLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(registerLink);

        container.add(card);

        // Authentication Event
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String username = loginUserField.getText().trim();
                    String password = new String(loginPassField.getPassword()).trim();
                    String role = (String) loginRoleCombo.getSelectedItem();

                    if (username.isEmpty() || password.isEmpty()) {
                        throw new ValidationException("Please fill in both Username and Password fields.");
                    }

                    Account account = dbManager.authenticate(username, password);

                    if (account == null) {
                        throw new ValidationException("Invalid username or password. Please try again.");
                    }

                    if (!account.getRole().equalsIgnoreCase(role)) {
                        throw new ValidationException("Access role mismatch. Selected role is unauthorized for this account.");
                    }

                    // Authenticated successfully
                    currentAccount = account;
                    loginUserField.setText("");
                    loginPassField.setText("");

                    if (role.equalsIgnoreCase("Admin")) {
                        // Refresh Admin Screens
                        refreshCustomerTable();
                        populateCustomerCombo();
                        refreshAdminBillTable();
                        cardLayout.show(cardPanel, "ADMIN_APP");
                    } else {
                        // Load Customer Details
                        Customer customer = dbManager.getCustomer(account.getCustomerId());
                        if (customer != null) {
                            custProfileName.setText(customer.getName());
                            custProfileId.setText(customer.getCustomerId());
                            custProfileMeter.setText(customer.getMeterNumber());
                            custProfileEmail.setText(customer.getEmail());
                            custProfilePhone.setText(customer.getPhone());
                            custProfileAddress.setText(customer.getAddress());
                        }
                        refreshCustBillTable();
                        cardLayout.show(cardPanel, "CUSTOMER_APP");
                    }

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Authentication Warning", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database query failed:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Register Click Link
        registerLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "REGISTER");
            }
        });

        // Add Enter Key bindings for quick submission
        ActionListener enterSubmit = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginBtn.doClick();
            }
        };
        loginUserField.addActionListener(enterSubmit);
        loginPassField.addActionListener(enterSubmit);

        return container;
    }

    /**
     * Creates the Customer Self-Registration Card view screen.
     */
    private JPanel createRegisterScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(CREAM_BG);

        // Card Panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CREAM, 1),
                new EmptyBorder(25, 35, 25, 35)
        ));
        card.setPreferredSize(new Dimension(400, 480));
        card.setMaximumSize(new Dimension(400, 480));

        // Header Title
        JLabel titleLbl = new JLabel("Customer Self-Registration");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(ACCENT_TAN);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);

        JLabel subTitleLbl = new JLabel("Create a utility directory profile & billing account");
        subTitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subTitleLbl.setForeground(TEXT_DARK);
        subTitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subTitleLbl);
        card.add(Box.createVerticalStrut(20));

        // Form Panels
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Full Name:*"), gbc);
        gbc.gridx = 1;
        regNameField = new JTextField(16);
        form.add(regNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Email Address:*"), gbc);
        gbc.gridx = 1;
        regEmailField = new JTextField(16);
        form.add(regEmailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Phone Number:*"), gbc);
        gbc.gridx = 1;
        regPhoneField = new JTextField(16);
        form.add(regPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Billing Address:*"), gbc);
        gbc.gridx = 1;
        regAddressArea = new JTextArea(4, 16);
        regAddressArea.setLineWrap(true);
        regAddressArea.setWrapStyleWord(true);
        form.add(new JScrollPane(regAddressArea), gbc);

        card.add(form);
        card.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnPanel.setBackground(CARD_BG);
        
        JButton submitBtn = new JButton("Register Account");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setBackground(ACCENT_TAN);
        submitBtn.setForeground(Color.WHITE);

        JButton cancelBtn = new JButton("Back to Login");

        btnPanel.add(submitBtn);
        btnPanel.add(cancelBtn);
        card.add(btnPanel);

        container.add(card);

        // Actions
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = regNameField.getText().trim();
                    String email = regEmailField.getText().trim();
                    String phone = regPhoneField.getText().trim();
                    String address = regAddressArea.getText().trim();

                    // Validation Checks
                    validateCustomerInput(name, email, phone, address);

                    // Generate Customer ID and Meter login username
                    Random rand = new Random();
                    int custNum = 1000 + rand.nextInt(9000);
                    int metNum = 5000 + rand.nextInt(4000);
                    String custId = "CUST-" + custNum;
                    String meterNum = "MET-" + metNum;

                    // Add Customer & Account atomically
                    Customer customer = new Customer(custId, name, email, phone, address, meterNum);
                    dbManager.addCustomer(customer);

                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Registration Successful!\n\n" +
                            "Please save your login details:\n" +
                            "• Username (Meter No): " + meterNum + "\n" +
                            "• Password (Phone): " + phone + "\n\n" +
                            "You can now use these details to log into the Customer Portal.",
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields and transition
                    clearSelfRegForm();
                    cardLayout.show(cardPanel, "LOGIN");

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database insertion failed:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSelfRegForm();
                cardLayout.show(cardPanel, "LOGIN");
            }
        });

        return container;
    }

    private void clearSelfRegForm() {
        regNameField.setText("");
        regEmailField.setText("");
        regPhoneField.setText("");
        regAddressArea.setText("");
    }

    /**
     * Creates the complete application screen for Administrators.
     */
    private JPanel createAdminAppScreen() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header Panel (Gradient) with Logout button
        JPanel headerWrap = new JPanel(new BorderLayout());
        GradientHeaderPanel headerPanel = new GradientHeaderPanel(
                "LogiTrack Utilities - Administrator Portal",
                "Perform database operations, manage directories and generate progressive bills"
        );
        headerWrap.add(headerPanel, BorderLayout.CENTER);

        JButton logoutBtn = createLogoutButton();
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        logoutPanel.setBackground(new Color(234, 214, 189)); // Match header gradient start color roughly
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutBtn);
        headerWrap.add(logoutPanel, BorderLayout.EAST);
        panel.add(headerWrap, BorderLayout.NORTH);

        // Tabbed Pane
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        adminTabbedPane.addTab("Customer Directory", createAdminCustomerTab());
        adminTabbedPane.addTab("New Bill Generator", createAdminBillingTab());
        adminTabbedPane.addTab("Billing History Log", createAdminHistoryTab());

        panel.add(adminTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the Admin customer directories manager.
     */
    private JPanel createAdminCustomerTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "Register Customer Profile",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        formPanel.add(new JLabel("Phone / Password:*"), gbc);
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

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setOpaque(false);
        JButton regBtn = new JButton("Register Profile");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regBtn.setBackground(ACCENT_TAN);
        regBtn.setForeground(Color.WHITE);
        
        JButton resetBtn = new JButton("Reset");
        btnPanel.add(regBtn);
        btnPanel.add(resetBtn);
        formPanel.add(btnPanel, gbc);

        generateCustomerAndMeterIds();

        // Right Panel: Table of existing customers
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CARD_BG);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "Registered Directory",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
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

        // Actions
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

                    validateCustomerInput(name, email, phone, address);

                    Customer customer = new Customer(custId, name, email, phone, address, meterNum);
                    dbManager.addCustomer(customer);

                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Customer profile registered!\nID: " + custId + "\nMeter login username: " + meterNum + "\nDefault password: " + phone,
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

                    refreshCustomerTable();
                    populateCustomerCombo();
                    clearCustomerForm();
                    generateCustomerAndMeterIds();

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Insertion Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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
     * Creates the Admin billing generator.
     */
    private JPanel createAdminBillingTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "Record Meter Readings",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
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

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setOpaque(false);
        JButton genBtn = new JButton("Generate Bill");
        genBtn.setBackground(ACCENT_TAN);
        genBtn.setForeground(Color.WHITE);
        genBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton clearBtn = new JButton("Clear");
        btnPanel.add(genBtn);
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel, gbc);

        // Right Panel: Slab Breakdown Preview
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(CARD_BG);
        previewPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "Dynamic Slab Calculation Summary",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
        ));
        
        calcPreviewLabel = new JLabel("<html><body><p style='color: #888;'>Select a customer and enter a current reading to preview calculations.</p></body></html>");
        calcPreviewLabel.setVerticalAlignment(JLabel.TOP);
        calcPreviewLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(calcPreviewLabel);
        previewPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(previewPanel, BorderLayout.CENTER);

        // Listeners
        customerCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLiveCalculation();
            }
        });

        currReadingField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLiveCalculation(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLiveCalculation(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLiveCalculation(); }
        });

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

                    showInvoiceDialog(customer, bill);

                    refreshAdminBillTable();
                    clearBillForm();
                    updateLiveCalculation();

                } catch (ValidationException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Insertion Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
     * Creates the Admin billing history manager.
     */
    private JPanel createAdminHistoryTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search Filter:"));
        adminSearchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton resetBtn = new JButton("Reset");
        searchPanel.add(adminSearchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        // Center Table
        String[] cols = {"Bill ID", "Cust ID", "Prev Rdg", "Curr Rdg", "Units", "Amount", "Bill Date", "Status"};
        adminBillTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        adminBillTable = new JTable(adminBillTableModel);
        adminBillTable.setRowHeight(22);
        adminBillTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(adminBillTable);

        // Bottom Actions Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton payBtn = new JButton("Mark as Paid");
        payBtn.setBackground(ACCENT_TAN);
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton viewBtn = new JButton("View Invoice Summary");
        viewBtn.setBackground(ACCENT_TAN);
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        actionPanel.add(viewBtn);
        actionPanel.add(payBtn);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        // Actions
        ActionListener searchAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = adminSearchField.getText().trim();
                    List<Bill> bills = dbManager.searchBills(query);
                    populateAdminBillTable(bills);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Search query failed:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        searchBtn.addActionListener(searchAction);
        adminSearchField.addActionListener(searchAction);

        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adminSearchField.setText("");
                refreshAdminBillTable();
            }
        });

        payBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = adminBillTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select a bill from the history table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) adminBillTable.getValueAt(row, 0);
                try {
                    dbManager.updateBillStatus(billId, "Paid");
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Bill #" + billId + " marked as Paid.", "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
                    refreshAdminBillTable();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Update Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = adminBillTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select a bill from the history table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) adminBillTable.getValueAt(row, 0);
                String custId = (String) adminBillTable.getValueAt(row, 1);
                double prev = (Double) adminBillTable.getValueAt(row, 2);
                double curr = (Double) adminBillTable.getValueAt(row, 3);
                double units = (Double) adminBillTable.getValueAt(row, 4);
                double amt = (Double) adminBillTable.getValueAt(row, 5);
                String date = (String) adminBillTable.getValueAt(row, 6);
                String status = (String) adminBillTable.getValueAt(row, 7);

                try {
                    Customer customer = dbManager.getCustomer(custId);
                    if (customer == null) {
                        JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                                "Customer metadata could not be fetched.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Bill b = new Bill(billId, custId, prev, curr, units, amt, date, status);
                    showInvoiceDialog(customer, b);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Database Retrieval Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return mainPanel;
    }

    /**
     * Creates the application interface for Customer users.
     */
    private JPanel createCustomerAppScreen() {
        customerMainPanel = new JPanel(new BorderLayout());

        // Header Panel with Logout button
        JPanel headerWrap = new JPanel(new BorderLayout());
        GradientHeaderPanel headerPanel = new GradientHeaderPanel(
                "LogiTrack Utilities - Customer Portal",
                "View your utility readings, account information, and pay bills online"
        );
        headerWrap.add(headerPanel, BorderLayout.CENTER);

        JButton logoutBtn = createLogoutButton();
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutBtn);
        headerWrap.add(logoutPanel, BorderLayout.EAST);
        customerMainPanel.add(headerWrap, BorderLayout.NORTH);

        // Body split layout
        JPanel body = new JPanel(new BorderLayout(15, 15));
        body.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Profile Panel Card (Left)
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(CARD_BG);
        profilePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "My Account Profile",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
        ));
        profilePanel.setPreferredSize(new Dimension(300, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        profilePanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        custProfileName = new JLabel("-");
        custProfileName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        profilePanel.add(custProfileName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        profilePanel.add(new JLabel("Customer ID:"), gbc);
        gbc.gridx = 1;
        custProfileId = new JLabel("-");
        profilePanel.add(custProfileId, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        profilePanel.add(new JLabel("Meter Number:"), gbc);
        gbc.gridx = 1;
        custProfileMeter = new JLabel("-");
        profilePanel.add(custProfileMeter, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        profilePanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1;
        custProfileEmail = new JLabel("-");
        profilePanel.add(custProfileEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        profilePanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        custProfilePhone = new JLabel("-");
        profilePanel.add(custProfilePhone, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        profilePanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        custProfileAddress = new JLabel("-");
        profilePanel.add(custProfileAddress, gbc);

        // Fill remaining vertical space in profile
        gbc.gridy = 6; gbc.weighty = 1.0;
        profilePanel.add(Box.createGlue(), gbc);

        body.add(profilePanel, BorderLayout.WEST);

        // Bill History Panel (Right)
        JPanel billsPanel = new JPanel(new BorderLayout(10, 10));
        billsPanel.setBackground(CARD_BG);
        billsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_CREAM), "My Billing Statements",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), ACCENT_TAN
        ));

        // Bill Search Bar
        JPanel billSearchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        billSearchWrap.setOpaque(false);
        billSearchWrap.add(new JLabel("Filter (Date/Status):"));
        custSearchField = new JTextField(15);
        JButton searchBtn = new JButton("Filter");
        JButton resetBtn = new JButton("Reset");
        billSearchWrap.add(custSearchField);
        billSearchWrap.add(searchBtn);
        billSearchWrap.add(resetBtn);
        billsPanel.add(billSearchWrap, BorderLayout.NORTH);

        // Bill Table
        String[] cols = {"Bill ID", "Prev Rdg", "Curr Rdg", "Units", "Amount Due", "Bill Date", "Status"};
        custBillTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        custBillTable = new JTable(custBillTableModel);
        custBillTable.setRowHeight(22);
        custBillTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        billsPanel.add(new JScrollPane(custBillTable), BorderLayout.CENTER);

        // Bill Table Actions
        JPanel billActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        billActions.setOpaque(false);
        JButton viewInvoiceBtn = new JButton("View Detailed Invoice");
        viewInvoiceBtn.setBackground(ACCENT_TAN);
        viewInvoiceBtn.setForeground(Color.WHITE);
        viewInvoiceBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton payOnlineBtn = new JButton("Pay Statement Online");
        payOnlineBtn.setBackground(ACCENT_TAN);
        payOnlineBtn.setForeground(Color.WHITE);
        payOnlineBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        billActions.add(viewInvoiceBtn);
        billActions.add(payOnlineBtn);
        billsPanel.add(billActions, BorderLayout.SOUTH);

        body.add(billsPanel, BorderLayout.CENTER);
        customerMainPanel.add(body, BorderLayout.CENTER);

        // Event actions
        ActionListener filterAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = custSearchField.getText().trim();
                    List<Bill> list = dbManager.searchCustomerBills(currentAccount.getCustomerId(), query);
                    populateCustBillTable(list);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        };
        searchBtn.addActionListener(filterAction);
        custSearchField.addActionListener(filterAction);

        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                custSearchField.setText("");
                refreshCustBillTable();
            }
        });

        viewInvoiceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = custBillTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select a bill from your statement history.", "Statement Required", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) custBillTable.getValueAt(row, 0);
                double prev = (Double) custBillTable.getValueAt(row, 1);
                double curr = (Double) custBillTable.getValueAt(row, 2);
                double units = (Double) custBillTable.getValueAt(row, 3);
                double amt = (Double) custBillTable.getValueAt(row, 4);
                String date = (String) custBillTable.getValueAt(row, 5);
                String status = (String) custBillTable.getValueAt(row, 6);

                try {
                    Customer customer = dbManager.getCustomer(currentAccount.getCustomerId());
                    Bill b = new Bill(billId, customer.getCustomerId(), prev, curr, units, amt, date, status);
                    showInvoiceDialog(customer, b);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        payOnlineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = custBillTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "Please select an unpaid statement to process.", "Statement Required", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int billId = (Integer) custBillTable.getValueAt(row, 0);
                String status = (String) custBillTable.getValueAt(row, 6);

                if (status.equalsIgnoreCase("Paid")) {
                    JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                            "This statement is already fully paid.", "Payment Complete", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int choice = JOptionPane.showConfirmDialog(ElectricityBillGeneratorApp.this,
                        "Proceed with online payment simulation for Bill #" + billId + "?",
                        "Online Payment", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        dbManager.updateBillStatus(billId, "Paid");
                        JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                                "Payment succeeded! Statement marked as Paid.", "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
                        refreshCustBillTable();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(ElectricityBillGeneratorApp.this,
                                "Payment processing failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return customerMainPanel;
    }

    /**
     * Shared Logout button builder.
     */
    private JButton createLogoutButton() {
        JButton btn = new JButton("Logout");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(new Color(213, 76, 60)); // Coral Red for warning actions
        btn.setForeground(Color.WHITE);
        btn.setFocusable(false);
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentAccount = null;
                cardLayout.show(cardPanel, "LOGIN");
            }
        });
        return btn;
    }

    // Helper functions

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
        if (name.isEmpty()) throw new ValidationException("Customer Name cannot be empty.");
        if (email.isEmpty()) throw new ValidationException("Email Address cannot be empty.");
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ValidationException("Invalid Email address format.");
        }
        if (phone.isEmpty()) throw new ValidationException("Phone number cannot be empty.");
        if (!phone.matches("^\\d{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
        if (address.isEmpty()) throw new ValidationException("Billing Address cannot be empty.");
    }

    private void validateBillInput() throws ValidationException {
        String selectedItem = (String) customerCombo.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty() || selectedItem.startsWith("Select")) {
            throw new ValidationException("Please select a customer target.");
        }
        String currStr = currReadingField.getText().trim();
        if (currStr.isEmpty()) {
            throw new ValidationException("Please enter the current meter reading.");
        }
        try {
            double curr = Double.parseDouble(currStr);
            if (curr < 0) throw new ValidationException("Current reading cannot be negative.");
            double prev = Double.parseDouble(prevReadingField.getText());
            if (curr < prev) {
                throw new ValidationException("Current reading cannot be less than previous reading (" + prev + ").");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Current reading must be a valid decimal number.");
        }
        
        String date = dateField.getText().trim();
        if (date.isEmpty() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new ValidationException("Date must follow YYYY-MM-DD formatting.");
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

    private void refreshAdminBillTable() {
        try {
            List<Bill> list = dbManager.getAllBills();
            populateAdminBillTable(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateAdminBillTable(List<Bill> list) {
        adminBillTableModel.setRowCount(0);
        for (Bill b : list) {
            adminBillTableModel.addRow(new Object[]{
                    b.getBillId(), b.getCustomerId(), b.getPreviousReading(),
                    b.getCurrentReading(), b.getUnitsConsumed(), b.getTotalAmount(),
                    b.getBillDate(), b.getPaymentStatus()
            });
        }
    }

    private void refreshCustBillTable() {
        try {
            if (currentAccount != null) {
                List<Bill> list = dbManager.getCustomerBills(currentAccount.getCustomerId());
                populateCustBillTable(list);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateCustBillTable(List<Bill> list) {
        custBillTableModel.setRowCount(0);
        for (Bill b : list) {
            custBillTableModel.addRow(new Object[]{
                    b.getBillId(), b.getPreviousReading(), b.getCurrentReading(),
                    b.getUnitsConsumed(), b.getTotalAmount(), b.getBillDate(),
                    b.getPaymentStatus()
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
                calcPreviewLabel.setText("<html><body><p style='color: #8C6239;'>Error: Current reading cannot be less than previous reading (" + prevReading + ").</p></body></html>");
                return;
            }

            double units = currReading - prevReading;
            unitsField.setText(String.format("%.2f", units));

            String breakdown = calculator.getSlabBreakdown(units);
            calcPreviewLabel.setText(breakdown);
        } catch (NumberFormatException e) {
            unitsField.setText("");
            calcPreviewLabel.setText("<html><body><p style='color: #8C6239;'>Error: Current reading must be a valid decimal number.</p></body></html>");
        } catch (Exception e) {
            calcPreviewLabel.setText("<html><body><p style='color: #8C6239;'>Error: " + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * Displays visual Invoice details pop-up modal.
     */
    private void showInvoiceDialog(Customer customer, Bill bill) {
        JDialog dialog = new JDialog(this, "Invoice Summary - Bill #" + bill.getBillId(), true);
        dialog.setSize(420, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header Gradient
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(234, 214, 189), getWidth(), 0, new Color(215, 176, 139));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(420, 65));
        headerPanel.setLayout(new GridBagLayout());
        JLabel headerLbl = new JLabel("INVOICE STATEMENT");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLbl.setForeground(TEXT_DARK);
        headerPanel.add(headerLbl);

        // Body
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(CARD_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Metadata Block
        JPanel metaPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        metaPanel.setBackground(CARD_BG);
        metaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_CREAM), "Billing & Customer Details"));
        ((TitledBorder)metaPanel.getBorder()).setTitleColor(ACCENT_TAN);
        metaPanel.add(new JLabel("Customer Name:"));
        metaPanel.add(new JLabel(customer.getName()));
        metaPanel.add(new JLabel("Customer ID:"));
        metaPanel.add(new JLabel(customer.getCustomerId()));
        metaPanel.add(new JLabel("Meter Number:"));
        metaPanel.add(new JLabel(customer.getMeterNumber()));
        metaPanel.add(new JLabel("Statement Date:"));
        metaPanel.add(new JLabel(bill.getBillDate()));
        bodyPanel.add(metaPanel);
        bodyPanel.add(Box.createVerticalStrut(10));

        // Readings details block
        JPanel rdgPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        rdgPanel.setBackground(CARD_BG);
        rdgPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_CREAM), "Consumption Stats"));
        ((TitledBorder)rdgPanel.getBorder()).setTitleColor(ACCENT_TAN);
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
        costPanel.setBackground(CARD_BG);
        costPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_CREAM), "Charge Calculations"));
        ((TitledBorder)costPanel.getBorder()).setTitleColor(ACCENT_TAN);
        JLabel costBreakdownLabel = new JLabel(calculator.getSlabBreakdown(bill.getUnitsConsumed()));
        costPanel.add(costBreakdownLabel, BorderLayout.CENTER);
        bodyPanel.add(costPanel);

        // Status block
        bodyPanel.add(Box.createVerticalStrut(10));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(CARD_BG);
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

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom Print / Close panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(CREAM_BG);
        JButton printBtn = new JButton("Simulate Print");
        printBtn.setBackground(ACCENT_TAN);
        printBtn.setForeground(Color.WHITE);
        printBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(dialog,
                        "Invoice sent to simulated printer queue.\nPrinted successfully!",
                        "Printing Statement", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton closeBtn = new JButton("Close Statement");
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
     * Subclassed custom JComponent panel with warm creamy gradients.
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

            // Creamy/Tan Gradient
            GradientPaint gp = new GradientPaint(0, 0, new Color(234, 214, 189), getWidth(), getHeight(), new Color(215, 176, 139));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(TEXT_DARK);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2d.drawString(title, 20, 32);

            g2d.setColor(new Color(110, 95, 80));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.drawString(subtitle, 20, 52);
        }
    }

    public static void main(String[] args) {
        // Setup FlatLaf Look and Feel with custom Cream tokens
        try {
            FlatLightLaf.setup();
            
            // Apply warm creamy customizations
            Color creamBg = new Color(253, 251, 247);
            Color warmWhite = new Color(255, 254, 250);
            Color darkCocoa = new Color(62, 39, 35);
            Color tanAccent = new Color(193, 154, 107);
            Color tabBg = new Color(245, 238, 228);
            Color tableSelected = new Color(234, 214, 189);

            UIManager.put("Panel.background", creamBg);
            UIManager.put("Label.foreground", darkCocoa);
            UIManager.put("Button.background", tanAccent);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusedBackground", tanAccent.darker());
            UIManager.put("TabbedPane.background", tabBg);
            UIManager.put("TabbedPane.selectedBackground", creamBg);
            UIManager.put("TabbedPane.foreground", darkCocoa);
            UIManager.put("Table.background", warmWhite);
            UIManager.put("Table.gridColor", new Color(230, 220, 205));
            UIManager.put("Table.selectionBackground", tableSelected);
            UIManager.put("Table.selectionForeground", darkCocoa);
            UIManager.put("TableHeader.background", tabBg);
            UIManager.put("TableHeader.foreground", darkCocoa);
            UIManager.put("TextField.background", warmWhite);
            UIManager.put("TextField.foreground", darkCocoa);
            UIManager.put("PasswordField.background", warmWhite);
            UIManager.put("PasswordField.foreground", darkCocoa);
            UIManager.put("TextArea.background", warmWhite);
            UIManager.put("TextArea.foreground", darkCocoa);
            UIManager.put("ComboBox.background", warmWhite);
            UIManager.put("ComboBox.foreground", darkCocoa);
            
        } catch (Exception e) {
            System.err.println("FlatLaf Light could not be initialized.");
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
