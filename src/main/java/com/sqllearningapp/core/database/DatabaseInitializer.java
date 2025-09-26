package com.sqllearningapp.core.database;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Schema and Sample Data Initializer
 * Preserves all your existing table structures and sample data
 */
@Slf4j
public class DatabaseInitializer {

    private final EmbeddedDatabase database;

    public DatabaseInitializer(EmbeddedDatabase database) {
        this.database = database;
    }

    /**
     * Initialize sample data for practice exercises
     * Preserves your existing ResourceLoader sample data structure
     */
    public void initializeSampleData() throws SQLException {
        log.info("Initializing sample data for practice exercises...");

        Connection connection = database.getPracticeConnection();

        try (Statement stmt = connection.createStatement()) {

            // Create all your existing tables
            createDepartmentsTable(stmt);
            createEmployeesTable(stmt);
            createProjectsTable(stmt);
            createEmployeeProjectsTable(stmt);
            createCustomersTable(stmt);
            createOrdersTable(stmt);

            // Insert your existing sample data
            insertSampleDepartments(stmt);
            insertSampleEmployees(stmt);
            insertSampleProjects(stmt);
            insertSampleEmployeeProjects(stmt);
            insertSampleCustomers(stmt);
            insertSampleOrders(stmt);

            log.info("Sample data initialization completed successfully");

        } catch (SQLException e) {
            log.error("Failed to initialize sample data", e);
            throw e;
        }
    }

    // Table creation methods (preserving your exact structure)
    private void createDepartmentsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS departments (
                id INT PRIMARY KEY AUTO_INCREMENT,
                department_name VARCHAR(100) NOT NULL,
                location VARCHAR(100),
                budget DECIMAL(15,2)
            );
            """;
        stmt.execute(sql);
        log.debug("Created departments table");
    }

    private void createEmployeesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS employees (
                id INT PRIMARY KEY AUTO_INCREMENT,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE,
                salary DECIMAL(10,2),
                hire_date DATE,
                department_id INT,
                manager_id INT,
                is_active BOOLEAN DEFAULT TRUE,
                FOREIGN KEY (department_id) REFERENCES departments(id),
                FOREIGN KEY (manager_id) REFERENCES employees(id)
            );
            """;
        stmt.execute(sql);
        log.debug("Created employees table");
    }

    private void createProjectsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS projects (
                id INT PRIMARY KEY AUTO_INCREMENT,
                project_name VARCHAR(100) NOT NULL,
                description TEXT,
                start_date DATE,
                end_date DATE,
                budget DECIMAL(12,2),
                department_id INT,
                FOREIGN KEY (department_id) REFERENCES departments(id)
            );
            """;
        stmt.execute(sql);
        log.debug("Created projects table");
    }

    private void createEmployeeProjectsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS employee_projects (
                employee_id INT,
                project_id INT,
                role VARCHAR(50),
                start_date DATE,
                end_date DATE,
                PRIMARY KEY (employee_id, project_id),
                FOREIGN KEY (employee_id) REFERENCES employees(id),
                FOREIGN KEY (project_id) REFERENCES projects(id)
            );
            """;
        stmt.execute(sql);
        log.debug("Created employee_projects table");
    }

    private void createCustomersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS customers (
                id INT PRIMARY KEY AUTO_INCREMENT,
                company_name VARCHAR(100) NOT NULL,
                contact_person VARCHAR(100),
                email VARCHAR(100),
                phone VARCHAR(20),
                address TEXT,
                city VARCHAR(50),
                country VARCHAR(50)
            );
            """;
        stmt.execute(sql);
        log.debug("Created customers table");
    }

    private void createOrdersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS orders (
                id INT PRIMARY KEY AUTO_INCREMENT,
                customer_id INT,
                order_date DATE,
                total_amount DECIMAL(10,2),
                status VARCHAR(20) DEFAULT 'PENDING',
                FOREIGN KEY (customer_id) REFERENCES customers(id)
            );
            """;
        stmt.execute(sql);
        log.debug("Created orders table");
    }

    // Sample data insertion methods (preserving your existing data)
    private void insertSampleDepartments(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO departments (department_name, location, budget) VALUES
            ('Engineering', 'San Francisco', 2000000.00),
            ('Marketing', 'New York', 800000.00),
            ('Sales', 'Chicago', 1200000.00),
            ('HR', 'Boston', 500000.00),
            ('Finance', 'Seattle', 600000.00);
            """;
        stmt.execute(sql);
        log.debug("Inserted sample departments");
    }

    private void insertSampleEmployees(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO employees (first_name, last_name, email, salary, hire_date, department_id, manager_id) VALUES
            ('John', 'Smith', 'john.smith@company.com', 95000.00, '2020-01-15', 1, NULL),
            ('Sarah', 'Johnson', 'sarah.johnson@company.com', 87000.00, '2020-03-20', 1, 1),
            ('Michael', 'Brown', 'michael.brown@company.com', 82000.00, '2019-11-10', 1, 1),
            ('Emily', 'Davis', 'emily.davis@company.com', 78000.00, '2021-05-08', 2, NULL),
            ('David', 'Wilson', 'david.wilson@company.com', 85000.00, '2020-09-12', 2, 4),
            ('Lisa', 'Anderson', 'lisa.anderson@company.com', 92000.00, '2019-07-25', 3, NULL),
            ('James', 'Taylor', 'james.taylor@company.com', 71000.00, '2021-02-14', 3, 6),
            ('Jennifer', 'Moore', 'jennifer.moore@company.com', 88000.00, '2020-06-30', 4, NULL),
            ('Robert', 'Jackson', 'robert.jackson@company.com', 94000.00, '2019-12-05', 5, NULL),
            ('Amanda', 'White', 'amanda.white@company.com', 73000.00, '2021-08-18', 5, 9);
            """;
        stmt.execute(sql);
        log.debug("Inserted sample employees");
    }

    private void insertSampleProjects(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO projects (project_name, description, start_date, end_date, budget, department_id) VALUES
            ('Website Redesign', 'Complete overhaul of company website', '2023-01-01', '2023-06-30', 150000.00, 1),
            ('Mobile App Development', 'Native mobile app for iOS and Android', '2023-02-15', '2023-12-31', 300000.00, 1),
            ('Marketing Campaign Q1', 'Digital marketing campaign for Q1', '2023-01-01', '2023-03-31', 75000.00, 2),
            ('Sales Training Program', 'Comprehensive sales training initiative', '2023-03-01', '2023-05-31', 50000.00, 3),
            ('HR System Upgrade', 'Modernization of HR management system', '2023-04-01', '2023-09-30', 120000.00, 4);
            """;
        stmt.execute(sql);
        log.debug("Inserted sample projects");
    }

    private void insertSampleEmployeeProjects(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO employee_projects (employee_id, project_id, role, start_date, end_date) VALUES
            (1, 1, 'Project Manager', '2023-01-01', '2023-06-30'),
            (2, 1, 'Frontend Developer', '2023-01-01', '2023-06-30'),
            (3, 1, 'Backend Developer', '2023-01-01', '2023-06-30'),
            (1, 2, 'Technical Lead', '2023-02-15', '2023-12-31'),
            (2, 2, 'Mobile Developer', '2023-02-15', '2023-12-31'),
            (4, 3, 'Campaign Manager', '2023-01-01', '2023-03-31'),
            (5, 3, 'Marketing Specialist', '2023-01-01', '2023-03-31'),
            (6, 4, 'Training Coordinator', '2023-03-01', '2023-05-31'),
            (7, 4, 'Sales Trainer', '2023-03-01', '2023-05-31'),
            (8, 5, 'Project Lead', '2023-04-01', '2023-09-30');
            """;
        stmt.execute(sql);
        log.debug("Inserted sample employee-project relationships");
    }

    private void insertSampleCustomers(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO customers (company_name, contact_person, email, phone, address, city, country) VALUES
            ('TechCorp Inc.', 'Alice Cooper', 'alice@techcorp.com', '555-0101', '123 Tech St', 'San Francisco', 'USA'),
            ('Global Solutions', 'Bob Martinez', 'bob@globalsol.com', '555-0102', '456 Business Ave', 'New York', 'USA'),
            ('Innovation Labs', 'Carol Kim', 'carol@innovlabs.com', '555-0103', '789 Innovation Dr', 'Austin', 'USA'),
            ('Digital Dynamics', 'Daniel Park', 'daniel@digidyn.com', '555-0104', '321 Digital Blvd', 'Seattle', 'USA'),
            ('Future Systems', 'Eva Rodriguez', 'eva@futuresys.com', '555-0105', '654 Future Rd', 'Boston', 'USA');
            """;
        stmt.execute(sql);
        log.debug("Inserted sample customers");
    }

    private void insertSampleOrders(Statement stmt) throws SQLException {
        String sql = """
            INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES
            (1, '2023-01-15', 25000.00, 'COMPLETED'),
            (2, '2023-02-20', 35000.00, 'COMPLETED'),
            (3, '2023-03-10', 15000.00, 'PENDING'),
            (1, '2023-04-05', 42000.00, 'IN_PROGRESS'),
            (4, '2023-04-12', 28000.00, 'COMPLETED'),
            (5, '2023-05-01', 33000.00, 'PENDING'),
            (2, '2023-05-15', 19000.00, 'COMPLETED'),
            (3, '2023-06-03', 31000.00, 'IN_PROGRESS');
            """;
        stmt.execute(sql);
        log.debug("Inserted sample orders");
    }
}