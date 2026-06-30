# Evaluation Criteria Mapping - Electricity Bill Generator

This document explains how the **Electricity Bill Generator** codebase satisfies each item in the project evaluation guidelines.

---

## 1. Input Validation & Graceful Exception Handling
*   **Checked Exceptions:** We created a custom exception class [`ValidationException.java`](file:///src/com/electricity/exception/ValidationException.java) (inheriting from `java.lang.Exception`) to manage input anomalies.
*   **Form Validation:** In [`ElectricityBillGeneratorApp.java`](file:///src/com/electricity/gui/ElectricityBillGeneratorApp.java), validation checks are performed prior to database commits:
    *   **Customer Fields:** Checks if fields are empty, applies an RFC 5322 regular expression to validate email formatting, and checks that phone numbers consist of exactly 10 digits.
    *   **Billing Readings:** Validates that the current meter reading is a positive decimal number and is greater than or equal to the previous reading retrieved from SQLite.
    *   **Login validation:** Handles empty credential entries and checks that user access roles match the database profile credentials.
*   **Graceful Recovery:** The presentation layer catches validation exceptions and alerts the user using `JOptionPane.showMessageDialog` error dialogues rather than causing system failure.

---

## 2. Code Modularity & Modular Programming
The codebase follows a modular structure separated into distinct packages, each holding a clear, single responsibility:
1.  **`com.electricity.model`**: Holds encapsulated data classes representing domain objects (`Person`, `Customer`, `Bill`, `Account`).
2.  **`com.electricity.calculator`**: Houses the progressive slab pricing calculation logic (`BillCalculator` and `SlabBillCalculator`).
3.  **`com.electricity.db`**: Manages all raw JDBC actions, connection lifetimes, table initialization, and CRUD query commands (`DatabaseManager`).
4.  **`com.electricity.exception`**: Houses checked custom exception declarations (`ValidationException`).
5.  **`com.electricity.gui`**: Contains the visual Presentation layer layout grids and actions (`ElectricityBillGeneratorApp`).

---

## 3. Application of OOP Concepts

### A. Encapsulation (Data Protection)
*   All data members in domain models are declared **`private`** (e.g. [`Person.java`](file:///src/com/electricity/model/Person.java#L11), [`Customer.java`](file:///src/com/electricity/model/Customer.java#L14), [`Bill.java`](file:///src/com/electricity/model/Bill.java#L11)).
*   Access and modification of these values are restricted to public **getter** and **setter** methods.

### B. Inheritance (Code Reusability)
*   [`Customer.java`](file:///src/com/electricity/model/Customer.java) inherits from the [`Person.java`](file:///src/com/electricity/model/Person.java) base class, inheriting contact fields (name, email, phone, address).
*   The child class constructor uses `super(name, email, phone, address)` to reuse parent class initialization, then adds customer-specific attributes (CustomerId, MeterNumber).

### C. Abstraction & Interfaces (Loose Coupling)
*   [`BillCalculator.java`](file:///src/com/electricity/calculator/BillCalculator.java) is defined as an `interface`. It hides implementation details and acts as a contract for billing calculators.
*   The UI components reference the abstract interface type rather than concrete classes, facilitating loose coupling.

### D. Polymorphism
*   [`SlabBillCalculator.java`](file:///src/com/electricity/calculator/SlabBillCalculator.java) implements the `BillCalculator` interface.
*   In the GUI frame, the billing calculator is referenced via the interface type `private final BillCalculator calculator;` and initialized as `new SlabBillCalculator();`. Calls to `calculator.calculateBill(units)` execute the correct subclass strategy polymorphically at runtime.

---

## 4. Database Integration (JDBC & SQLite)
*   **JDBC Driver & Connection:** Uses `DriverManager.getConnection("jdbc:sqlite:electricity_bill.db")` to interact with a serverless, local SQLite database file. No server installations or accounts are required.
*   **Try-with-Resources:** All JDBC operations in [`DatabaseManager.java`](file:///src/com/electricity/db/DatabaseManager.java) (e.g., `authenticate`, `getAllCustomers`, `addBill`) use try-with-resources blocks. This guarantees that `Connection`, `PreparedStatement`, and `ResultSet` interfaces are automatically closed on termination, avoiding leaks.
*   **Database Transactions:** In `DatabaseManager.addCustomer()`, we use transaction boundaries:
    1.  Disable auto-commit: `conn.setAutoCommit(false);`
    2.  Write customer record.
    3.  Write customer credentials.
    4.  Commit the transaction: `conn.commit();`
    5.  Roll back modifications on failure: `conn.rollback();` (guaranteeing ACID compliance).
*   **SQL Injection Defense:** All queries with dynamic variables are bound securely via parameterized `PreparedStatements`.

---

## 5. Swing User Interface (UI)
*   **Layout Management:** We use a `CardLayout` at the root container to switch views between the Login Screen, Self-Registration, the Admin Dashboard, and the Customer Dashboard. Inside forms, components are aligned using a combination of `GridBagLayout`, `BorderLayout`, and `FlowLayout` to maintain responsiveness.
*   **Event-Driven Design:** Action listeners are mapped to buttons, and a live `DocumentListener` is bound to the current meter reading input to update units consumed and display a progressive slab HTML breakdown dynamically as you type.
*   **Visual Aesthetics:** Incorporates the Light FlatLaf Swing Look and Feel, customized globally with UIManager keys (vanilla/cream background `#FDFBF7`, warm tan accents `#C19A6B`, and cocoa text `#3E2723`).

---

## 6. Coding Standards & Modularity
*   **CamelCase Naming Rules:** Classes start with uppercase (e.g., `DatabaseManager`), and methods/variables start with lowercase (e.g., `validateCustomerInput`, `prevReadingField`).
*   **Inline Documentation:** Source files are annotated with comments explicitly labeling OOP design implementations and business logic steps.
*   **Java 8+ Release Target:** Code is compiled with the `--release 8` flag, ensuring compiled class file version compatibility across older Java 8 runtimes and newer Java 21 JDKs.
