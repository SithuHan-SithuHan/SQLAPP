-- Practice Database Schema for SQL Learning
-- This database is used for practice exercises and can be reset
-- Created: 2025-09-26 05:02:00 UTC
-- User: SithuHan-SithuHan

-- =====================================================
-- PRACTICE DATABASE SCHEMA (Resetable for exercises)
-- =====================================================

-- Companies/Departments table
CREATE TABLE departments (
                             id INTEGER PRIMARY KEY AUTO_INCREMENT,
                             department_name VARCHAR(100) NOT NULL UNIQUE,
                             location VARCHAR(100),
                             budget DECIMAL(15,2),
                             manager_name VARCHAR(100),
                             established_date DATE,
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employees table (comprehensive for all SQL practice)
CREATE TABLE employees (
                           id INTEGER PRIMARY KEY AUTO_INCREMENT,
                           employee_code VARCHAR(20) UNIQUE,
                           first_name VARCHAR(50) NOT NULL,
                           last_name VARCHAR(50) NOT NULL,
                           email VARCHAR(100) UNIQUE,
                           phone VARCHAR(20),
                           salary DECIMAL(10,2),
                           hire_date DATE,
                           birth_date DATE,
                           department_id INTEGER,
                           manager_id INTEGER,
                           job_title VARCHAR(100),
                           employment_status VARCHAR(20) DEFAULT 'ACTIVE',
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                           FOREIGN KEY (department_id) REFERENCES departments(id),
                           FOREIGN KEY (manager_id) REFERENCES employees(id),

                           CONSTRAINT chk_salary CHECK (salary >= 0),
                           CONSTRAINT chk_status CHECK (employment_status IN ('ACTIVE', 'INACTIVE', 'TERMINATED', 'ON_LEAVE'))
);

-- Projects table
CREATE TABLE projects (
                          id INTEGER PRIMARY KEY AUTO_INCREMENT,
                          project_code VARCHAR(20) UNIQUE,
                          project_name VARCHAR(100) NOT NULL,
                          description TEXT,
                          start_date DATE,
                          end_date DATE,
                          budget DECIMAL(12,2),
                          status VARCHAR(20) DEFAULT 'PLANNING',
                          priority VARCHAR(10) DEFAULT 'MEDIUM',
                          department_id INTEGER,
                          project_manager_id INTEGER,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (department_id) REFERENCES departments(id),
                          FOREIGN KEY (project_manager_id) REFERENCES employees(id),

                          CONSTRAINT chk_project_status CHECK (status IN ('PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),
                          CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- Employee-Project assignments (many-to-many relationship)
CREATE TABLE employee_projects (
                                   id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                   employee_id INTEGER NOT NULL,
                                   project_id INTEGER NOT NULL,
                                   role VARCHAR(50),
                                   start_date DATE,
                                   end_date DATE,
                                   hours_allocated DECIMAL(5,2),
                                   hourly_rate DECIMAL(8,2),
                                   is_active BOOLEAN DEFAULT TRUE,

                                   FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
                                   FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Customers table
CREATE TABLE customers (
                           id INTEGER PRIMARY KEY AUTO_INCREMENT,
                           customer_code VARCHAR(20) UNIQUE,
                           company_name VARCHAR(100) NOT NULL,
                           contact_person VARCHAR(100),
                           email VARCHAR(100),
                           phone VARCHAR(20),
                           address TEXT,
                           city VARCHAR(50),
                           state VARCHAR(50),
                           country VARCHAR(50) DEFAULT 'USA',
                           postal_code VARCHAR(10),
                           credit_limit DECIMAL(12,2) DEFAULT 0,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
                        id INTEGER PRIMARY KEY AUTO_INCREMENT,
                        order_number VARCHAR(20) UNIQUE,
                        customer_id INTEGER NOT NULL,
                        order_date DATE NOT NULL,
                        required_date DATE,
                        shipped_date DATE,
                        total_amount DECIMAL(10,2) DEFAULT 0,
                        discount_amount DECIMAL(8,2) DEFAULT 0,
                        tax_amount DECIMAL(8,2) DEFAULT 0,
                        status VARCHAR(20) DEFAULT 'PENDING',
                        priority VARCHAR(10) DEFAULT 'NORMAL',
                        notes TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (customer_id) REFERENCES customers(id),

                        CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED')),
                        CONSTRAINT chk_order_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

-- Products table (for advanced exercises)
CREATE TABLE products (
                          id INTEGER PRIMARY KEY AUTO_INCREMENT,
                          product_code VARCHAR(20) UNIQUE,
                          product_name VARCHAR(100) NOT NULL,
                          category VARCHAR(50),
                          unit_price DECIMAL(8,2),
                          units_in_stock INTEGER DEFAULT 0,
                          reorder_level INTEGER DEFAULT 0,
                          discontinued BOOLEAN DEFAULT FALSE,
                          supplier_id INTEGER,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
                          CONSTRAINT chk_stock CHECK (units_in_stock >= 0)
);

-- Order Details (for complex JOIN exercises)
CREATE TABLE order_details (
                               id INTEGER PRIMARY KEY AUTO_INCREMENT,
                               order_id INTEGER NOT NULL,
                               product_id INTEGER NOT NULL,
                               quantity INTEGER NOT NULL DEFAULT 1,
                               unit_price DECIMAL(8,2) NOT NULL,
                               discount DECIMAL(5,4) DEFAULT 0,

                               FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                               FOREIGN KEY (product_id) REFERENCES products(id),

                               CONSTRAINT chk_quantity CHECK (quantity > 0),
                               CONSTRAINT chk_order_unit_price CHECK (unit_price >= 0),
                               CONSTRAINT chk_discount CHECK (discount >= 0 AND discount <= 1)
);

-- Person table for JOIN exercises (matching your original questions)
CREATE TABLE Person (
                        personId INTEGER PRIMARY KEY AUTO_INCREMENT,
                        lastName VARCHAR(50),
                        firstName VARCHAR(50)
);

-- Address table for JOIN exercises (matching your original questions) - FIXED
CREATE TABLE Address (
                         addressId INTEGER PRIMARY KEY AUTO_INCREMENT,
                         personId INTEGER,
                         city VARCHAR(50),
                         state VARCHAR(50),
                         FOREIGN KEY (personId) REFERENCES Person(personId)
);

-- Address table for normalization exercises (lowercase)
CREATE TABLE addresses (
                           id INTEGER PRIMARY KEY AUTO_INCREMENT,
                           entity_type VARCHAR(20) NOT NULL,
                           entity_id INTEGER NOT NULL,
                           address_type VARCHAR(20) DEFAULT 'PRIMARY',
                           street_address VARCHAR(200),
                           city VARCHAR(50),
                           state VARCHAR(50),
                           postal_code VARCHAR(10),
                           country VARCHAR(50) DEFAULT 'USA',
                           is_active BOOLEAN DEFAULT TRUE
);

-- Performance testing table (for advanced exercises)
CREATE TABLE performance_test (
                                  id INTEGER PRIMARY KEY AUTO_INCREMENT,
                                  test_name VARCHAR(100),
                                  execution_time_ms INTEGER,
                                  memory_usage_mb DECIMAL(8,2),
                                  test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  user_id VARCHAR(100) DEFAULT 'SithuHan-SithuHan'
);