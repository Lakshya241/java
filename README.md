# Electricity Bill Generator - Project Guide & Documentation

This repository contains a modular, object-oriented desktop application for **Electricity Bill Generation**, written in Java with a Swing GUI interface and SQLite JDBC storage.

---

## 🏛️ Project Architecture & OOP Mapping

This application is designed to showcase core Object-Oriented Programming (OOP) principles. Below is the mapping of how these concepts are implemented in the code:

### 1. Encapsulation (Data Hiding)
*   **Concept:** Restricting direct access to an object's components and exposing them safely through public accessors.
*   **Where to find in code:**
    *   [`Person.java`](file:///src/com/electricity/model/Person.java): Private instance fields (`name`, `email`, `phone`, `address`) are accessed exclusively via getter/setter methods.
    *   [`Customer.java`](file:///src/com/electricity/model/Customer.java): Restricts access to `customerId` and `meterNumber`.
    *   [`Bill.java`](file:///src/com/electricity/model/Bill.java) and [`Account.java`](file:///src/com/electricity/model/Account.java): Encapsulates statement metrics and authentication properties.

### 2. Inheritance (Code Reuse)
*   **Concept:** Creating a new class derived from an existing class to reuse behavior and extend properties.
*   **Where to find in code:**
    *   [`Customer.java` (L10)](file:///src/com/electricity/model/Customer.java#L10) extends [`Person.java`](file:///src/com/electricity/model/Person.java).
    *   The `Customer` class inherits name, email, phone, and address attributes.
    *   `Customer(L23)` calls `super(...)` to invoke the parent constructor and reuse properties.

### 3. Abstraction & Interfaces
*   **Concept:** Defining structural contracts without committing to implementation details.
*   **Where to find in code:**
    *   [`BillCalculator.java`](file:///src/com/electricity/calculator/BillCalculator.java): Defines an abstract calculation interface declaring `calculateBill(double units)` and `getSlabBreakdown(double units)`.
    *   Loose Coupling: GUI components rely on the `BillCalculator` interface rather than a concrete class, making the billing engine highly modular and interchangeable.

### 4. Polymorphism
*   **Concept:** The ability of a single reference type to execute different underlying implementations depending on the object it points to.
*   **Where to find in code:**
    *   [`SlabBillCalculator.java`](file:///src/com/electricity/calculator/SlabBillCalculator.java) implements `BillCalculator`.
    *   Inside [`ElectricityBillGeneratorApp.java` (L45)](file:///src/com/electricity/gui/ElectricityBillGeneratorApp.java#L45), the calculator is declared as `private final BillCalculator calculator;` and initialized as `new SlabBillCalculator();`. Calls like `calculator.calculateBill(units)` execute the slab-wise strategy polymorphically.

---

## 🗄️ Database Architecture & JDBC Details

The application uses **SQLite**—a file-based, serverless database that creates a local file `electricity_bill.db` on startup. 

### Database Features:
1.  **Try-with-Resources:** Implemented throughout [`DatabaseManager.java`](file:///src/com/electricity/db/DatabaseManager.java) to automatically manage resources (Connections, Statements, ResultSets) and prevent memory leaks.
2.  **ACID Transactions:** Inside [`DatabaseManager.java` (L125)](file:///src/com/electricity/db/DatabaseManager.java#L125), `addCustomer()` uses manual transaction boundaries:
    *   Disables autocommit: `conn.setAutoCommit(false);`
    *   Atomically writes the customer profile and their login account.
    *   Rolls back operations if any error is encountered to prevent database corruption.
3.  **SQL Injection Prevention:** Uses parameterized `PreparedStatement` boundaries for user queries.

---

## ⚠️ Input Validation & Exception Handling

We use a checked custom exception [`ValidationException.java`](file:///src/com/electricity/exception/ValidationException.java) to handle input issues:
*   **Customer Fields:** Validates Name, Email (RFC 5322 regex checks), Phone (exactly 10 digits), and Address are not empty.
*   **Billing Readings:** Checks that current reading is a positive number and is greater than or equal to the previous reading.
*   **Login validation:** Warns on empty fields or unauthorized access roles.
*   GUI Catching: The presentation layer catches validation exceptions and displays warnings to the user via user-friendly `JOptionPane` alerts.

---

## 🚀 How to Run the Project

1.  **To Compile:** Double-click [`compile.bat`](file:///compile.bat) in the project root.
2.  **To Run:** Double-click [`run.bat`](file:///run.bat) in the project root.

---

## 🎓 Evaluator Q&A / Presentation Study Guide

Here are standard questions the evaluation panel might ask, along with references to answer them:

**Q1: Why did you use an interface for billing calculations?**
> *"We used the `BillCalculator` interface to define a contract for our billing engine. By coding to an interface rather than a concrete class, we decoupled our GUI from the specific calculation logic. If we want to add a Commercial Slab Rate or a Flat-rate pricing model in the future, we can simply implement the `BillCalculator` interface and swap the object without altering the GUI code."*

**Q2: How do you prevent connection leaks and resource depletion?**
> *"We utilize Java's try-with-resources syntax inside `DatabaseManager.java`. Since `Connection`, `Statement`, and `ResultSet` implement `AutoCloseable`, this construct guarantees that resources are closed automatically when the execution exits the block, even if SQL exceptions are thrown."*

**Q3: How does transaction rollback work when registering a customer?**
> *"When a customer registers, we must create both a customer profile entry and a login credential entry. We set `conn.setAutoCommit(false);` to treat these two queries as an atomic transaction. If either insert fails, the `catch` block calls `conn.rollback();` to discard changes, keeping the database in a consistent state."*

**Q4: How did you implement inheritance?**
> *"Our `Customer` class inherits from the `Person` base class. This avoids code redundancy by reusing attributes like Name, Email, Phone, and Address. The `Customer` class calls `super(...)` in its constructor to pass these attributes to `Person` while adding customer-specific details (Customer ID, Meter Number)."*
