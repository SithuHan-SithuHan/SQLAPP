-- Sample Data for SQL Learning Application
-- Comprehensive realistic data for all practice exercises
-- Date: 2025-09-26 05:02:00 UTC
-- User: SithuHan-SithuHan

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- Clear existing data (for reset functionality)
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM addresses;
DELETE FROM order_details;
DELETE FROM products;
DELETE FROM orders;
DELETE FROM customers;
DELETE FROM employee_projects;
DELETE FROM projects;
DELETE FROM employees;
DELETE FROM departments;
DELETE FROM Address;
DELETE FROM Person;
DELETE FROM performance_test;
SET FOREIGN_KEY_CHECKS = 1;

-- Reset auto increment counters
ALTER TABLE departments AUTO_INCREMENT = 1;
ALTER TABLE employees AUTO_INCREMENT = 1;
ALTER TABLE projects AUTO_INCREMENT = 1;
ALTER TABLE customers AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE products AUTO_INCREMENT = 1;
ALTER TABLE Person AUTO_INCREMENT = 1;
ALTER TABLE Address AUTO_INCREMENT = 1;

-- =====================================================
-- DEPARTMENTS DATA
-- =====================================================
INSERT INTO departments (department_name, location, budget, manager_name, established_date, is_active) VALUES
                                                                                                           ('Engineering', 'San Francisco', 2500000.00, 'Sarah Johnson', '2018-01-15', TRUE),
                                                                                                           ('Marketing', 'New York', 1200000.00, 'Michael Chen', '2019-03-01', TRUE),
                                                                                                           ('Sales', 'Chicago', 1800000.00, 'Lisa Anderson', '2018-06-15', TRUE),
                                                                                                           ('Human Resources', 'Boston', 800000.00, 'David Wilson', '2019-01-10', TRUE),
                                                                                                           ('Finance', 'Seattle', 1000000.00, 'Jennifer Martinez', '2018-09-01', TRUE),
                                                                                                           ('Operations', 'Austin', 1500000.00, 'Robert Thompson', '2020-02-01', TRUE),
                                                                                                           ('Customer Support', 'Denver', 600000.00, 'Amanda White', '2020-05-15', TRUE),
                                                                                                           ('Research & Development', 'Portland', 2000000.00, 'Dr. James Lee', '2019-08-01', TRUE);

-- =====================================================
-- EMPLOYEES DATA (Comprehensive for all exercises)
-- =====================================================
INSERT INTO employees (employee_code, first_name, last_name, email, phone, salary, hire_date, birth_date, department_id, manager_id, job_title, employment_status) VALUES
-- Engineering Department (ID: 1)
('ENG001', 'Sarah', 'Johnson', 'sarah.johnson@company.com', '555-0101', 125000.00, '2020-01-15', '1985-03-12', 1, NULL, 'Engineering Manager', 'ACTIVE'),
('ENG002', 'John', 'Smith', 'john.smith@company.com', '555-0102', 95000.00, '2020-03-20', '1990-07-25', 1, 1, 'Senior Software Engineer', 'ACTIVE'),
('ENG003', 'Emily', 'Davis', 'emily.davis@company.com', '555-0103', 87000.00, '2021-02-10', '1992-11-08', 1, 1, 'Software Engineer', 'ACTIVE'),
('ENG004', 'Michael', 'Brown', 'michael.brown@company.com', '555-0104', 92000.00, '2020-08-05', '1988-04-15', 1, 1, 'DevOps Engineer', 'ACTIVE'),
('ENG005', 'Jessica', 'Wilson', 'jessica.wilson@company.com', '555-0105', 98000.00, '2021-01-12', '1987-09-22', 1, 1, 'Full Stack Developer', 'ACTIVE'),

-- Marketing Department (ID: 2)
('MKT001', 'Michael', 'Chen', 'michael.chen@company.com', '555-0201', 110000.00, '2019-03-01', '1983-12-05', 2, NULL, 'Marketing Director', 'ACTIVE'),
('MKT002', 'Ashley', 'Rodriguez', 'ashley.rodriguez@company.com', '555-0202', 75000.00, '2021-05-15', '1994-02-18', 2, 6, 'Digital Marketing Specialist', 'ACTIVE'),
('MKT003', 'Christopher', 'Taylor', 'christopher.taylor@company.com', '555-0203', 68000.00, '2022-01-08', '1996-06-30', 2, 6, 'Content Marketing Manager', 'ACTIVE'),
('MKT004', 'Samantha', 'Lee', 'samantha.lee@company.com', '555-0204', 82000.00, '2020-11-20', '1989-10-14', 2, 6, 'Brand Manager', 'ACTIVE'),

-- Sales Department (ID: 3)
('SAL001', 'Lisa', 'Anderson', 'lisa.anderson@company.com', '555-0301', 115000.00, '2018-06-15', '1982-08-20', 3, NULL, 'Sales Director', 'ACTIVE'),
('SAL002', 'Daniel', 'Jackson', 'daniel.jackson@company.com', '555-0302', 85000.00, '2020-09-12', '1991-01-07', 3, 9, 'Senior Sales Representative', 'ACTIVE'),
('SAL003', 'Rachel', 'Garcia', 'rachel.garcia@company.com', '555-0303', 78000.00, '2021-07-25', '1993-05-12', 3, 9, 'Sales Representative', 'ACTIVE'),
('SAL004', 'Kevin', 'Martinez', 'kevin.martinez@company.com', '555-0304', 72000.00, '2022-03-10', '1995-12-03', 3, 9, 'Sales Associate', 'ACTIVE'),

-- Human Resources (ID: 4)
('HR001', 'David', 'Wilson', 'david.wilson@company.com', '555-0401', 105000.00, '2019-01-10', '1980-04-28', 4, NULL, 'HR Director', 'ACTIVE'),
('HR002', 'Michelle', 'Thompson', 'michelle.thompson@company.com', '555-0402', 72000.00, '2021-04-18', '1990-09-15', 4, 13, 'HR Specialist', 'ACTIVE'),
('HR003', 'Andrew', 'White', 'andrew.white@company.com', '555-0403', 68000.00, '2022-02-28', '1994-07-08', 4, 13, 'Recruiter', 'ACTIVE'),

-- Finance (ID: 5)
('FIN001', 'Jennifer', 'Martinez', 'jennifer.martinez@company.com', '555-0501', 120000.00, '2018-09-01', '1981-11-16', 5, NULL, 'Finance Director', 'ACTIVE'),
('FIN002', 'Robert', 'Johnson', 'robert.johnson@company.com', '555-0502', 88000.00, '2020-12-05', '1988-03-25', 5, 16, 'Financial Analyst', 'ACTIVE'),
('FIN003', 'Laura', 'Davis', 'laura.davis@company.com', '555-0503', 79000.00, '2021-08-18', '1992-01-11', 5, 16, 'Accounting Specialist', 'ACTIVE'),

-- Operations (ID: 6)
('OPS001', 'Robert', 'Thompson', 'robert.thompson@company.com', '555-0601', 108000.00, '2020-02-01', '1984-06-09', 6, NULL, 'Operations Manager', 'ACTIVE'),
('OPS002', 'Maria', 'Hernandez', 'maria.hernandez@company.com', '555-0602', 74000.00, '2021-06-12', '1991-12-22', 6, 19, 'Operations Coordinator', 'ACTIVE'),

-- Customer Support (ID: 7)
('SUP001', 'Amanda', 'White', 'amanda.white@company.com', '555-0701', 95000.00, '2020-05-15', '1986-02-14', 7, NULL, 'Support Manager', 'ACTIVE'),
('SUP002', 'Thomas', 'Miller', 'thomas.miller@company.com', '555-0702', 58000.00, '2021-09-20', '1993-08-05', 7, 21, 'Senior Support Specialist', 'ACTIVE'),
('SUP003', 'Nicole', 'Wilson', 'nicole.wilson@company.com', '555-0703', 52000.00, '2022-01-15', '1995-04-18', 7, 21, 'Support Specialist', 'ACTIVE'),

-- R&D (ID: 8)
('RD001', 'James', 'Lee', 'james.lee@company.com', '555-0801', 135000.00, '2019-08-01', '1978-10-30', 8, NULL, 'Research Director', 'ACTIVE'),
('RD002', 'Patricia', 'Anderson', 'patricia.anderson@company.com', '555-0802', 102000.00, '2020-10-10', '1985-12-07', 8, 24, 'Senior Research Scientist', 'ACTIVE');

-- =====================================================
-- PROJECTS DATA
-- =====================================================
INSERT INTO projects (project_code, project_name, description, start_date, end_date, budget, status, priority, department_id, project_manager_id) VALUES
                                                                                                                                                      ('PRJ001', 'Website Redesign', 'Complete overhaul of company website with modern design', '2023-01-01', '2023-06-30', 250000.00, 'COMPLETED', 'HIGH', 1, 1),
                                                                                                                                                      ('PRJ002', 'Mobile App Development', 'Native mobile app for iOS and Android platforms', '2023-02-15', '2024-02-15', 500000.00, 'IN_PROGRESS', 'CRITICAL', 1, 2),
                                                                                                                                                      ('PRJ003', 'CRM System Integration', 'Integration of new CRM system with existing tools', '2023-03-01', '2023-09-30', 180000.00, 'IN_PROGRESS', 'MEDIUM', 2, 6),
                                                                                                                                                      ('PRJ004', 'Sales Training Program', 'Comprehensive sales training initiative', '2023-04-01', '2023-12-31', 75000.00, 'IN_PROGRESS', 'MEDIUM', 3, 9),
                                                                                                                                                      ('PRJ005', 'HR System Upgrade', 'Modernization of HR management system', '2023-05-01', '2024-01-31', 120000.00, 'PLANNING', 'LOW', 4, 13),
                                                                                                                                                      ('PRJ006', 'Financial Reporting Automation', 'Automate monthly financial reporting process', '2023-06-01', '2023-11-30', 95000.00, 'IN_PROGRESS', 'HIGH', 5, 16),
                                                                                                                                                      ('PRJ007', 'Cloud Migration', 'Migration of on-premise infrastructure to cloud', '2023-07-01', '2024-06-30', 800000.00, 'PLANNING', 'CRITICAL', 6, 19),
                                                                                                                                                      ('PRJ008', 'AI Research Initiative', 'Research into AI applications for business processes', '2023-08-01', '2024-12-31', 1200000.00, 'IN_PROGRESS', 'HIGH', 8, 24);

-- =====================================================
-- EMPLOYEE-PROJECT ASSIGNMENTS
-- =====================================================
INSERT INTO employee_projects (employee_id, project_id, role, start_date, end_date, hours_allocated, hourly_rate, is_active) VALUES
-- Website Redesign (PRJ001)
(1, 1, 'Project Manager', '2023-01-01', '2023-06-30', 40.00, 75.00, FALSE),
(2, 1, 'Lead Developer', '2023-01-01', '2023-06-30', 40.00, 65.00, FALSE),
(3, 1, 'Frontend Developer', '2023-01-01', '2023-06-30', 40.00, 55.00, FALSE),
(4, 1, 'DevOps Engineer', '2023-02-01', '2023-06-30', 20.00, 60.00, FALSE),

-- Mobile App Development (PRJ002)
(2, 2, 'Technical Lead', '2023-02-15', '2024-02-15', 40.00, 65.00, TRUE),
(3, 2, 'Mobile Developer', '2023-02-15', '2024-02-15', 40.00, 55.00, TRUE),
(5, 2, 'Full Stack Developer', '2023-03-01', '2024-02-15', 35.00, 58.00, TRUE),

-- CRM System Integration (PRJ003)
(6, 3, 'Project Lead', '2023-03-01', '2023-09-30', 30.00, 70.00, TRUE),
(7, 3, 'Marketing Analyst', '2023-03-01', '2023-09-30', 25.00, 45.00, TRUE),

-- Sales Training Program (PRJ004)
(9, 4, 'Program Director', '2023-04-01', '2023-12-31', 25.00, 72.00, TRUE),
(10, 4, 'Senior Trainer', '2023-04-01', '2023-12-31', 30.00, 55.00, TRUE),
(11, 4, 'Training Coordinator', '2023-04-01', '2023-12-31', 20.00, 48.00, TRUE),

-- HR System Upgrade (PRJ005)
(13, 5, 'Project Sponsor', '2023-05-01', '2024-01-31', 15.00, 68.00, TRUE),
(14, 5, 'HR Analyst', '2023-05-01', '2024-01-31', 30.00, 45.00, TRUE),

-- Financial Reporting Automation (PRJ006)
(16, 6, 'Finance Lead', '2023-06-01', '2023-11-30', 25.00, 75.00, TRUE),
(17, 6, 'Financial Analyst', '2023-06-01', '2023-11-30', 35.00, 52.00, TRUE),

-- AI Research Initiative (PRJ008)
(24, 8, 'Research Director', '2023-08-01', '2024-12-31', 30.00, 85.00, TRUE),
(25, 8, 'Senior Researcher', '2023-08-01', '2024-12-31', 40.00, 70.00, TRUE);

-- =====================================================
-- CUSTOMERS DATA
-- =====================================================
INSERT INTO customers (customer_code, company_name, contact_person, email, phone, address, city, state, country, postal_code, credit_limit, is_active) VALUES
                                                                                                                                                           ('CUST001', 'TechCorp Industries', 'Alice Cooper', 'alice@techcorp.com', '555-1001', '123 Technology Drive', 'San Francisco', 'CA', 'USA', '94102', 50000.00, TRUE),
                                                                                                                                                           ('CUST002', 'Global Solutions LLC', 'Bob Martinez', 'bob@globalsol.com', '555-1002', '456 Business Avenue', 'New York', 'NY', 'USA', '10001', 75000.00, TRUE),
                                                                                                                                                           ('CUST003', 'Innovation Labs Inc', 'Carol Kim', 'carol@innovlabs.com', '555-1003', '789 Innovation Boulevard', 'Austin', 'TX', 'USA', '73301', 100000.00, TRUE),
                                                                                                                                                           ('CUST004', 'Digital Dynamics', 'Daniel Park', 'daniel@digidyn.com', '555-1004', '321 Digital Street', 'Seattle', 'WA', 'USA', '98101', 60000.00, TRUE),
                                                                                                                                                           ('CUST005', 'Future Systems Corp', 'Eva Rodriguez', 'eva@futuresys.com', '555-1005', '654 Future Road', 'Boston', 'MA', 'USA', '02101', 80000.00, TRUE),
                                                                                                                                                           ('CUST006', 'Mega Enterprises', 'Frank Thompson', 'frank@megaent.com', '555-1006', '987 Enterprise Way', 'Chicago', 'IL', 'USA', '60601', 120000.00, TRUE),
                                                                                                                                                           ('CUST007', 'Smart Tech Solutions', 'Grace Lee', 'grace@smarttech.com', '555-1007', '147 Smart Avenue', 'Denver', 'CO', 'USA', '80201', 45000.00, TRUE),
                                                                                                                                                           ('CUST008', 'Advanced Systems Inc', 'Henry Wilson', 'henry@advsys.com', '555-1008', '258 Advanced Drive', 'Portland', 'OR', 'USA', '97201', 65000.00, TRUE),
                                                                                                                                                           ('CUST009', 'NextGen Technologies', 'Irene Davis', 'irene@nextgen.com', '555-1009', '369 NextGen Plaza', 'Miami', 'FL', 'USA', '33101', 55000.00, TRUE),
                                                                                                                                                           ('CUST010', 'Premier Software Group', 'Jack Brown', 'jack@premier.com', '555-1010', '741 Premier Street', 'Atlanta', 'GA', 'USA', '30301', 90000.00, TRUE);

-- =====================================================
-- PRODUCTS DATA
-- =====================================================
INSERT INTO products (product_code, product_name, category, unit_price, units_in_stock, reorder_level, discontinued, supplier_id) VALUES
                                                                                                                                      ('PROD001', 'Enterprise Software License', 'Software', 2999.99, 100, 10, FALSE, NULL),
                                                                                                                                      ('PROD002', 'Cloud Storage - Premium', 'Cloud Services', 299.99, 500, 50, FALSE, NULL),
                                                                                                                                      ('PROD003', 'Mobile App Development Kit', 'Development Tools', 1499.99, 75, 15, FALSE, NULL),
                                                                                                                                      ('PROD004', 'Database Management System', 'Software', 4999.99, 25, 5, FALSE, NULL),
                                                                                                                                      ('PROD005', 'Security Suite - Professional', 'Security', 899.99, 150, 20, FALSE, NULL),
                                                                                                                                      ('PROD006', 'Analytics Dashboard', 'Analytics', 1999.99, 80, 10, FALSE, NULL),
                                                                                                                                      ('PROD007', 'API Gateway License', 'Development Tools', 3499.99, 40, 8, FALSE, NULL),
                                                                                                                                      ('PROD008', 'Machine Learning Platform', 'AI/ML', 7999.99, 15, 3, FALSE, NULL),
                                                                                                                                      ('PROD009', 'Backup & Recovery Solution', 'Infrastructure', 1299.99, 60, 12, FALSE, NULL),
                                                                                                                                      ('PROD010', 'Monitoring & Alerting System', 'Infrastructure', 999.99, 120, 25, FALSE, NULL);

-- =====================================================
-- ORDERS DATA
-- =====================================================
INSERT INTO orders (order_number, customer_id, order_date, required_date, shipped_date, total_amount, discount_amount, tax_amount, status, priority, notes) VALUES
                                                                                                                                                                ('ORD001', 1, '2025-01-15', '2025-02-15', '2025-01-20', 8999.97, 500.00, 679.98, 'DELIVERED', 'HIGH', 'Rush order for Q1 implementation'),
                                                                                                                                                                ('ORD002', 2, '2025-02-01', '2025-03-01', '2025-02-05', 15999.94, 1000.00, 1199.99, 'DELIVERED', 'NORMAL', 'Annual license renewal'),
                                                                                                                                                                ('ORD003', 3, '2025-02-15', '2025-03-15', NULL, 12499.95, 750.00, 937.50, 'PROCESSING', 'HIGH', 'New client setup package'),
                                                                                                                                                                ('ORD004', 4, '2025-03-01', '2025-04-01', '2025-03-10', 5999.98, 300.00, 419.99, 'DELIVERED', 'NORMAL', 'Standard business package'),
                                                                                                                                                                ('ORD005', 5, '2025-03-15', '2025-04-15', NULL, 22999.92, 2000.00, 1679.99, 'SHIPPED', 'URGENT', 'Enterprise upgrade package'),
                                                                                                                                                                ('ORD006', 6, '2025-04-01', '2025-05-01', NULL, 18499.93, 1200.00, 1297.49, 'PROCESSING', 'HIGH', 'Multi-department rollout'),
                                                                                                                                                                ('ORD007', 7, '2025-04-15', '2025-05-15', NULL, 7999.96, 400.00, 559.99, 'PENDING', 'NORMAL', 'Small business starter'),
                                                                                                                                                                ('ORD008', 8, '2025-05-01', '2025-06-01', NULL, 11499.95, 600.00, 806.99, 'PENDING', 'LOW', 'Quarterly refresh'),
                                                                                                                                                                ('ORD009', 9, '2025-05-15', '2025-06-15', NULL, 9999.96, 500.00, 699.99, 'PENDING', 'NORMAL', 'Standard renewal'),
                                                                                                                                                                ('ORD010', 10, '2025-06-01', '2025-07-01', NULL, 25999.90, 2500.00, 1859.99, 'PENDING', 'URGENT', 'Major system overhaul');

-- =====================================================
-- ORDER DETAILS DATA
-- =====================================================
INSERT INTO order_details (order_id, product_id, quantity, unit_price, discount) VALUES
-- Order 1 (ORD001)
(1, 1, 2, 2999.99, 0.10),
(1, 2, 10, 299.99, 0.05),

-- Order 2 (ORD002)
(2, 4, 2, 4999.99, 0.08),
(2, 5, 5, 899.99, 0.10),
(2, 6, 2, 1999.99, 0.05),

-- Order 3 (ORD003)
(3, 3, 5, 1499.99, 0.12),
(3, 7, 1, 3499.99, 0.08),
(3, 9, 2, 1299.99, 0.10),

-- Order 4 (ORD004)
(4, 2, 15, 299.99, 0.08),
(4, 10, 2, 999.99, 0.05),

-- Order 5 (ORD005)
(5, 8, 2, 7999.99, 0.15),
(5, 4, 1, 4999.99, 0.10),
(5, 6, 3, 1999.99, 0.12),

-- Order 6 (ORD006)
(6, 1, 4, 2999.99, 0.12),
(6, 5, 8, 899.99, 0.10),
(6, 10, 3, 999.99, 0.08),

-- Order 7 (ORD007)
(7, 2, 20, 299.99, 0.10),
(7, 9, 1, 1299.99, 0.05),

-- Order 8 (ORD008)
(8, 3, 3, 1499.99, 0.08),
(8, 6, 2, 1999.99, 0.10),
(8, 10, 4, 999.99, 0.12),

-- Order 9 (ORD009)
(9, 1, 2, 2999.99, 0.08),
(9, 5, 4, 899.99, 0.10),

-- Order 10 (ORD010)
(10, 8, 3, 7999.99, 0.18),
(10, 4, 1, 4999.99, 0.12),
(10, 7, 1, 3499.99, 0.10);

-- =====================================================
-- PERSON AND ADDRESS DATA (for JOIN exercises)
-- =====================================================
INSERT INTO Person (personId, lastName, firstName) VALUES
                                                       (1, 'Smith', 'John'),
                                                       (2, 'Johnson', 'Mary'),
                                                       (3, 'Williams', 'Robert'),
                                                       (4, 'Brown', 'Patricia'),
                                                       (5, 'Davis', 'Michael'),
                                                       (6, 'Miller', 'Jennifer'),
                                                       (7, 'Wilson', 'William'),
                                                       (8, 'Moore', 'Linda'),
                                                       (9, 'Taylor', 'David'),
                                                       (10, 'Anderson', 'Barbara');

INSERT INTO Address (addressId, personId, city, state) VALUES
                                                           (1, 1, 'San Francisco', 'California'),
                                                           (2, 2, 'New York', 'New York'),
                                                           (3, 3, 'Chicago', 'Illinois'),
                                                           (4, 5, 'Boston', 'Massachusetts'),
                                                           (5, 6, 'Seattle', 'Washington'),
                                                           (6, 8, 'Austin', 'Texas'),
                                                           (7, 9, 'Denver', 'Colorado'),
-- Note: persons 4, 7, 10 don't have addresses (for NULL exercises)
                                                           (8, 1, 'Los Angeles', 'California'), -- John has secondary address
                                                           (9, 2, 'Albany', 'New York'); -- Mary has secondary address

-- =====================================================
-- ADDRESSES DATA (for normalization exercises)
-- =====================================================
INSERT INTO addresses (entity_type, entity_id, address_type, street_address, city, state, postal_code, country, is_active) VALUES
-- Employee addresses
('EMPLOYEE', 1, 'PRIMARY', '123 Main Street', 'San Francisco', 'CA', '94102', 'USA', TRUE),
('EMPLOYEE', 2, 'PRIMARY', '456 Oak Avenue', 'San Francisco', 'CA', '94103', 'USA', TRUE),
('EMPLOYEE', 6, 'PRIMARY', '789 Broadway', 'New York', 'NY', '10001', 'USA', TRUE),
('EMPLOYEE', 9, 'PRIMARY', '321 Michigan Avenue', 'Chicago', 'IL', '60601', 'USA', TRUE),

-- Customer addresses
('CUSTOMER', 1, 'BILLING', '123 Technology Drive', 'San Francisco', 'CA', '94102', 'USA', TRUE),
('CUSTOMER', 1, 'SHIPPING', '124 Technology Drive', 'San Francisco', 'CA', '94102', 'USA', TRUE),
('CUSTOMER', 2, 'PRIMARY', '456 Business Avenue', 'New York', 'NY', '10001', 'USA', TRUE),
('CUSTOMER', 3, 'PRIMARY', '789 Innovation Boulevard', 'Austin', 'TX', '73301', 'USA', TRUE);

-- =====================================================
-- PERFORMANCE TEST DATA
-- =====================================================
INSERT INTO performance_test (test_name, execution_time_ms, memory_usage_mb, user_id) VALUES
                                                                                          ('SELECT * FROM employees', 15, 2.5, 'SithuHan-SithuHan'),
                                                                                          ('Complex JOIN query', 45, 8.2, 'SithuHan-SithuHan'),
                                                                                          ('Aggregation with GROUP BY', 28, 4.1, 'SithuHan-SithuHan'),
                                                                                          ('Subquery performance test', 67, 12.3, 'SithuHan-SithuHan'),
                                                                                          ('Window function test', 52, 7.8, 'SithuHan-SithuHan');

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify data integrity
SELECT
    'Departments' as table_name, COUNT(*) as record_count FROM departments
UNION ALL
SELECT 'Employees', COUNT(*) FROM employees
UNION ALL
SELECT 'Projects', COUNT(*) FROM projects
UNION ALL
SELECT 'Employee_Projects', COUNT(*) FROM employee_projects
UNION ALL
SELECT 'Customers', COUNT(*) FROM customers
UNION ALL
SELECT 'Orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Order_Details', COUNT(*) FROM order_details
UNION ALL
SELECT 'Person', COUNT(*) FROM Person
UNION ALL
SELECT 'Address', COUNT(*) FROM Address
UNION ALL
SELECT 'Addresses', COUNT(*) FROM addresses;

-- Sample verification query for relationships
SELECT
    d.department_name,
    COUNT(e.id) as employee_count,
    AVG(e.salary) as avg_salary
FROM departments d
         LEFT JOIN employees e ON d.id = e.department_id AND e.is_active = TRUE
GROUP BY d.id, d.department_name
ORDER BY employee_count DESC;