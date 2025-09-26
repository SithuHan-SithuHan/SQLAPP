-- Sample Data for SQL Learning Application
-- Comprehensive realistic data for all practice exercises
-- Date: 2025-09-26 05:02:00 UTC
-- User: SithuHan-SithuHan

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

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
('SAL002', 'Daniel', 'Jackson', 'daniel.jackson@company.com', '555-0302', 85000.00, '2020-09-12', '1991-01-07', 3, 10, 'Senior Sales Representative', 'ACTIVE'),
('SAL003', 'Rachel', 'Garcia', 'rachel.garcia@company.com', '555-0303', 78000.00, '2021-07-25', '1993-05-12', 3, 10, 'Sales Representative', 'ACTIVE'),
('SAL004', 'Kevin', 'Martinez', 'kevin.martinez@company.com', '555-0304', 72000.00, '2022-03-10', '1995-12-03', 3, 10, 'Sales Associate', 'ACTIVE'),

-- Human Resources (ID: 4)
('HR001', 'David', 'Wilson', 'david.wilson@company.com', '555-0401', 105000.00, '2019-01-10', '1980-04-28', 4, NULL, 'HR Director', 'ACTIVE'),
('HR002', 'Michelle', 'Thompson', 'michelle.thompson@company.com', '555-0402', 72000.00, '2021-04-18', '1990-09-15', 4, 14, 'HR Specialist', 'ACTIVE'),
('HR003', 'Andrew', 'White', 'andrew.white@company.com', '555-0403', 68000.00, '2022-02-28', '1994-07-08', 4, 14, 'Recruiter', 'ACTIVE'),

-- Finance (ID: 5)
('FIN001', 'Jennifer', 'Martinez', 'jennifer.martinez@company.com', '555-0501', 120000.00, '2018-09-01', '1981-11-16', 5, NULL, 'Finance Director', 'ACTIVE'),
('FIN002', 'Robert', 'Johnson', 'robert.johnson@company.com', '555-0502', 88000.00, '2020-12-05', '1988-03-25', 5, 17, 'Financial Analyst', 'ACTIVE'),
('FIN003', 'Laura', 'Davis', 'laura.davis@company.com', '555-0503', 79000.00, '2021-08-18', '1992-01-11', 5, 17, 'Accounting Specialist', 'ACTIVE'),

-- Operations (ID: 6)
('OPS001', 'Robert', 'Thompson', 'robert.thompson@company.com', '555-0601', 108000.00, '2020-02-01', '1984-06-09', 6, NULL, 'Operations Manager', 'ACTIVE'),
('OPS002', 'Maria', 'Hernandez', 'maria.hernandez@company.com', '555-0602', 74000.00, '2021-06-12', '1991-12-22', 6, 20, 'Operations Coordinator', 'ACTIVE'),

-- Customer Support (ID: 7)
('SUP001', 'Amanda', 'White', 'amanda.white@company.com', '555-0701', 95000.00, '2020-05-15', '1986-02-14', 7, NULL, 'Support Manager', 'ACTIVE'),
('SUP002', 'Thomas', 'Miller', 'thomas.miller@company.com', '555-0702', 58000.00, '2021-09-20', '1993-08-05', 7, 22, 'Senior Support Specialist', 'ACTIVE'),
('SUP003', 'Nicole', 'Wilson', 'nicole.wilson@company.com', '555-0703', 52000.00, '2022-01-15', '1995-04-18', 7, 22, 'Support Specialist', 'ACTIVE'),

-- R&D (ID: 8)
('RD001', 'James', 'Lee', 'james.lee@company.com', '555-0801', 135000.00, '2019-08-01', '1978-10-30', 8, NULL, 'Research Director', 'ACTIVE'),
('RD002', 'Patricia', 'Anderson', 'patricia.anderson@company.com', '555-0802', 102000.00, '2020-10-10', '1985-12-07', 8, 25, 'Senior Research Scientist', 'ACTIVE');

-- =====================================================
-- PROJECTS DATA
-- =====================================================
INSERT INTO projects (project_code, project_name, description, start_date, end_date, budget, status, priority, department_id, project_manager_id) VALUES
                                                                                                                                                      ('PRJ001', 'Website Redesign', 'Complete overhaul of company website with modern design', '2023-01-01', '2023-06-30', 250000.00, 'COMPLETED', 'HIGH', 1, 1),
                                                                                                                                                      ('PRJ002', 'Mobile App Development', 'Native mobile app for iOS and Android platforms', '2023-02-15', '2024-02-15', 500000.00, 'IN_PROGRESS', 'CRITICAL', 1, 2),
                                                                                                                                                      ('PRJ003', 'CRM System Integration', 'Integration of new CRM system with existing tools', '2023-03-01', '2023-09-30', 180000.00, 'IN_PROGRESS', 'MEDIUM', 2, 6),
                                                                                                                                                      ('PRJ004', 'Sales Training Program', 'Comprehensive sales training initiative', '2023-04-01', '2023-12-31', 75000.00, 'IN_PROGRESS', 'MEDIUM', 3, 10),
                                                                                                                                                      ('PRJ005', 'HR System Upgrade', 'Modernization of HR management system', '2023-05-01', '2024-01-31', 120000.00, 'PLANNING', 'LOW', 4, 14),
                                                                                                                                                      ('PRJ006', 'Financial Reporting Automation', 'Automate monthly financial reporting process', '2023-06-01', '2023-11-30', 95000.00, 'IN_PROGRESS', 'HIGH', 5, 17),
                                                                                                                                                      ('PRJ007', 'Cloud Migration', 'Migration of on-premise infrastructure to cloud', '2023-07-01', '2024-06-30', 800000.00, 'PLANNING', 'CRITICAL', 6, 20),
                                                                                                                                                      ('PRJ008', 'AI Research Initiative', 'Research into AI applications for business processes', '2023-08-01', '2024-12-31', 1200000.00, 'IN_PROGRESS', 'HIGH', 8, 25);

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
(7, 3, 'Marketing Analyst', '2023-03-01', '2023-09-30', 25.00, 45.00, TRUE);

-- =====================================================
-- CUSTOMERS DATA
-- =====================================================
INSERT INTO customers (customer_code, company_name, contact_person, email, phone, address, city, state, country, postal_code, credit_limit, is_active) VALUES
                                                                                                                                                           ('CUST001', 'TechCorp Industries', 'Alice Cooper', 'alice@techcorp.com', '555-1001', '123 Technology Drive', 'San Francisco', 'CA', 'USA', '94102', 50000.00, TRUE),
                                                                                                                                                           ('CUST002', 'Global Solutions LLC', 'Bob Martinez', 'bob@globalsol.com', '555-1002', '456 Business Avenue', 'New York', 'NY', 'USA', '10001', 75000.00, TRUE),
                                                                                                                                                           ('CUST003', 'Innovation Labs Inc', 'Carol Kim', 'carol@innovlabs.com', '555-1003', '789 Innovation Boulevard', 'Austin', 'TX', 'USA', '73301', 100000.00, TRUE),
                                                                                                                                                           ('CUST004', 'Digital Dynamics', 'Daniel Park', 'daniel@digidyn.com', '555-1004', '321 Digital Street', 'Seattle', 'WA', 'USA', '98101', 60000.00, TRUE),
                                                                                                                                                           ('CUST005', 'Future Systems Corp', 'Eva Rodriguez', 'eva@futuresys.com', '555-1005', '654 Future Road', 'Boston', 'MA', 'USA', '02101', 80000.00, TRUE);

-- =====================================================
-- PRODUCTS DATA
-- =====================================================
INSERT INTO products (product_code, product_name, category, unit_price, units_in_stock, reorder_level, discontinued, supplier_id) VALUES
                                                                                                                                      ('PROD001', 'Enterprise Software License', 'Software', 2999.99, 100, 10, FALSE, NULL),
                                                                                                                                      ('PROD002', 'Cloud Storage - Premium', 'Cloud Services', 299.99, 500, 50, FALSE, NULL),
                                                                                                                                      ('PROD003', 'Mobile App Development Kit', 'Development Tools', 1499.99, 75, 15, FALSE, NULL),
                                                                                                                                      ('PROD004', 'Database Management System', 'Software', 4999.99, 25, 5, FALSE, NULL),
                                                                                                                                      ('PROD005', 'Security Suite - Professional', 'Security', 899.99, 150, 20, FALSE, NULL);

-- =====================================================
-- ORDERS DATA
-- =====================================================
INSERT INTO orders (order_number, customer_id, order_date, required_date, shipped_date, total_amount, discount_amount, tax_amount, status, priority, notes) VALUES
                                                                                                                                                                ('ORD001', 1, '2025-01-15', '2025-02-15', '2025-01-20', 8999.97, 500.00, 679.98, 'DELIVERED', 'HIGH', 'Rush order for Q1 implementation'),
                                                                                                                                                                ('ORD002', 2, '2025-02-01', '2025-03-01', '2025-02-05', 15999.94, 1000.00, 1199.99, 'DELIVERED', 'NORMAL', 'Annual license renewal'),
                                                                                                                                                                ('ORD003', 3, '2025-02-15', '2025-03-15', NULL, 12499.95, 750.00, 937.50, 'PROCESSING', 'HIGH', 'New client setup package'),
                                                                                                                                                                ('ORD004', 4, '2025-03-01', '2025-04-01', '2025-03-10', 5999.98, 300.00, 419.99, 'DELIVERED', 'NORMAL', 'Standard business package'),
                                                                                                                                                                ('ORD005', 5, '2025-03-15', '2025-04-15', NULL, 22999.92, 2000.00, 1679.99, 'SHIPPED', 'URGENT', 'Enterprise upgrade package');

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

-- Order 3 (ORD003)
(3, 3, 5, 1499.99, 0.12),
(3, 1, 1, 2999.99, 0.08);

-- =====================================================
-- PERSON AND ADDRESS DATA (for JOIN exercises)
-- =====================================================
INSERT INTO Person (personId, lastName, firstName) VALUES
                                                       (1, 'Smith', 'John'),
                                                       (2, 'Johnson', 'Mary'),
                                                       (3, 'Williams', 'Robert'),
                                                       (4, 'Brown', 'Patricia'),
                                                       (5, 'Davis', 'Michael');

INSERT INTO Address (addressId, personId, city, state) VALUES
                                                           (1, 1, 'San Francisco', 'California'),
                                                           (2, 2, 'New York', 'New York'),
                                                           (3, 3, 'Chicago', 'Illinois'),
                                                           (4, 5, 'Boston', 'Massachusetts');

-- =====================================================
-- ADDRESSES DATA (for normalization exercises)
-- =====================================================
INSERT INTO addresses (entity_type, entity_id, address_type, street_address, city, state, postal_code, country, is_active) VALUES
                                                                                                                               ('EMPLOYEE', 1, 'PRIMARY', '123 Main Street', 'San Francisco', 'CA', '94102', 'USA', TRUE),
                                                                                                                               ('EMPLOYEE', 2, 'PRIMARY', '456 Oak Avenue', 'San Francisco', 'CA', '94103', 'USA', TRUE),
                                                                                                                               ('CUSTOMER', 1, 'BILLING', '123 Technology Drive', 'San Francisco', 'CA', '94102', 'USA', TRUE),
                                                                                                                               ('CUSTOMER', 2, 'PRIMARY', '456 Business Avenue', 'New York', 'NY', '10001', 'USA', TRUE);

-- =====================================================
-- PERFORMANCE TEST DATA
-- =====================================================
INSERT INTO performance_test (test_name, execution_time_ms, memory_usage_mb, user_id) VALUES
                                                                                          ('SELECT * FROM employees', 15, 2.5, 'SithuHan-SithuHan'),
                                                                                          ('Complex JOIN query', 45, 8.2, 'SithuHan-SithuHan'),
                                                                                          ('Aggregation with GROUP BY', 28, 4.1, 'SithuHan-SithuHan');