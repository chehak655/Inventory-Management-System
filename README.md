# 📦 Inventory Management System

A full-featured **desktop inventory application** built with Java Swing and MySQL, featuring a modern UI with dashboard analytics, CRUD operations, category filtering, and real-time stock alerts.

---

## 🖥️ Screenshots

> Dashboard with live stats, sidebar navigation, and color-coded stock status.

---

## ✨ Features

- 📊 **Dashboard** — Live stats: total SKUs, units in stock, inventory value, low stock count
- ➕ **Add / Edit / Delete** items with form validation
- 🔍 **Search** inventory globally from the top bar
- 🗂️ **Category filter** to browse by product type
- ⚠️ **Low Stock alerts** — items at or below threshold highlighted automatically
- 📈 **Reports view** — per-category breakdown with total value and average price
- 🎨 **Modern UI** — gradient sidebar, alternating table rows, color-coded status badges

---

## 🛠️ Tech Stack

| Layer      | Technology              |
|------------|-------------------------|
| Language   | Java 8+                 |
| UI         | Java Swing              |
| Database   | MySQL 8.x               |
| Connector  | mysql-connector-j (JDBC)|

---

## ⚙️ Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/chehak655/Inventory-Management-System.git
cd Inventory-Management-System
```

### 2. Set up the database
Open MySQL and run:
```sql
CREATE DATABASE inventory_db;
USE inventory_db;

CREATE TABLE inventory (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL UNIQUE,
    category   VARCHAR(50)   NOT NULL DEFAULT 'General',
    quantity   INT           NOT NULL DEFAULT 0,
    price      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Download MySQL JDBC Driver
Download `mysql-connector-j` from https://dev.mysql.com/downloads/connector/j/
and place the `.jar` in the project folder.

### 4. Set environment variables
```cmd
set DB_URL=jdbc:mysql://localhost:3306/inventory_db
set DB_USER=your_mysql_username
set DB_PASS=your_mysql_password
```

### 5. Compile
```bash
javac -cp .;mysql-connector-j-9.x.x.jar InventoryManagementSystem.java
```
> On Mac/Linux use `:` instead of `;`

### 6. Run
```bash
java -cp .;mysql-connector-j-9.x.x.jar InventoryManagementSystem
```

---

## 🔐 Security

Credentials are **never hardcoded**. The app reads database configuration from environment variables at runtime. See `.env.example` for the required variables.

---

## 📁 Project Structure

```
Inventory-Management-System/
├── InventoryManagementSystem.java   # Main application (single-file architecture)
├── .env.example                     # Environment variable template
├── .gitignore                       # Git ignore rules
└── README.md                        # Project documentation
```

---

## 👩‍💻 Author

**Chehak** — [github.com/chehak655](https://github.com/chehak655)
