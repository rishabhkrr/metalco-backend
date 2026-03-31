-- =====================================================
-- MetalCO ERP - Seed Data Script
-- Seeds all essential master data for testing flows
-- =====================================================

-- 1. UNIT MASTER
INSERT INTO unit_master (unit_code, unit_name, address, city, state, pincode, gstin, status)
VALUES 
('UNIT-01', 'MetalCo Unit 1 - Mumbai', 'Plot 5, MIDC Industrial Area', 'Mumbai', 'Maharashtra', '400001', '27AABCU9603R1ZM', 'approved');

-- 2. SUPPLIER MASTER (3 suppliers)
INSERT INTO supplier_master (supplier_code, supplier_name, email, phone, material_type, status, unit_code, created_date) 
VALUES 
('SUP-001', 'NALCO Aluminium Ltd', 'nalco@supplier.com', '9876543210', 'Aluminium', 'approved', 'UNIT-01', GETDATE()),
('SUP-002', 'HINDALCO Industries', 'hindalco@supplier.com', '9876543211', 'Aluminium', 'approved', 'UNIT-01', GETDATE()),
('SUP-003', 'Vedanta Aluminium', 'vedanta@supplier.com', '9876543212', 'Aluminium', 'approved', 'UNIT-01', GETDATE());

-- 3. CUSTOMER MASTER (3 customers)
INSERT INTO customer_master (customer_code, company_name, customer_name, email, phone, credit_limit, credit_period, status, unit_code, created_date)
VALUES
('CUST-001', 'AutoParts India Ltd', 'Rajesh Kumar', 'rajesh@autoparts.com', '9898989898', 500000, 30, 'approved', 'UNIT-01', GETDATE()),
('CUST-002', 'BuildTech Solutions', 'Priya Sharma', 'priya@buildtech.com', '9797979797', 750000, 45, 'approved', 'UNIT-01', GETDATE()),
('CUST-003', 'MetalWorks Pvt Ltd', 'Amit Patel', 'amit@metalworks.com', '9696969696', 1000000, 60, 'approved', 'UNIT-01', GETDATE());

-- 4. ITEM MASTER (5 items covering different product categories)
INSERT INTO item_master (item_code, item_description, product_category, material_type, brand, grade, temper, dimension, uom, status, unit_code)
VALUES
('ALU-SHEET-001', 'Aluminium Sheet 1mm', 'Sheet', 'Aluminium', 'NALCO', '1100', 'H14', '1mm x 1250mm x 2500mm', 'KGS', 'approved', 'UNIT-01'),
('ALU-COIL-001', 'Aluminium Coil 0.5mm', 'Coil', 'Aluminium', 'HINDALCO', '3003', 'O', '0.5mm x 1000mm', 'KGS', 'approved', 'UNIT-01'),
('ALU-PLATE-001', 'Aluminium Plate 6mm', 'Plate', 'Aluminium', 'NALCO', '6061', 'T6', '6mm x 1250mm x 2500mm', 'KGS', 'approved', 'UNIT-01'),
('ALU-FOIL-001', 'Aluminium Foil 0.02mm', 'Foil', 'Aluminium', 'HINDALCO', '8011', 'O', '0.02mm x 500mm', 'KGS', 'approved', 'UNIT-01'),
('ALU-ROD-001', 'Aluminium Rod 25mm', 'Rod', 'Aluminium', 'NALCO', '6063', 'T5', '25mm diameter', 'KGS', 'approved', 'UNIT-01');

-- 5. HSN CODE MASTER
INSERT INTO hsn_code_master (hsn_code, description, gst_rate, effective_date, status, unit_code)
VALUES
('7606', 'Aluminium plates, sheets and strip of thickness exceeding 0.2 mm', 18.00, '2024-01-01', 'approved', 'UNIT-01'),
('7607', 'Aluminium foil of a thickness not exceeding 0.2 mm', 18.00, '2024-01-01', 'approved', 'UNIT-01'),
('7604', 'Aluminium bars, rods and profiles', 18.00, '2024-01-01', 'approved', 'UNIT-01');

-- 6. PROCESS FLOW
INSERT INTO process_entry (process_name, status, unit_code)
VALUES
('Cutting', 'approved', 'UNIT-01'),
('Slitting', 'approved', 'UNIT-01'),
('Lamination', 'approved', 'UNIT-01'),
('Anodizing', 'approved', 'UNIT-01');

-- 7. MACHINE MASTER (3 machines)
INSERT INTO machine_master (machine_code, machine_name, machine_type, process_name, capacity, status, unit_code)
VALUES
('MCH-001', 'CNC Cutting Machine', 'Cutting', 'Cutting', 'Sheet: Max 1250mm width', 'approved', 'UNIT-01'),
('MCH-002', 'Hydraulic Slitter', 'Slitting', 'Slitting', 'Coil: Max 1000mm width', 'approved', 'UNIT-01'),
('MCH-003', 'Lamination Press', 'Lamination', 'Lamination', 'Sheet: Max 2500mm length', 'approved', 'UNIT-01');

-- 8. SALESMAN MASTER (2 salesmen)
INSERT INTO salesman_master (salesman_code, salesman_name, email, phone, status, unit_code, incentive_percentage)
VALUES
('SM-001', 'Vikram Singh', 'vikram@metalco.com', '9191919191', 'approved', 'UNIT-01', 2.50),
('SM-002', 'Deepak Joshi', 'deepak@metalco.com', '9292929292', 'approved', 'UNIT-01', 3.00);

-- 9. NALCO PRICE (today's price)
INSERT INTO nalco_price (price, effective_date, status, unit_code)
VALUES
(235.50, CAST(GETDATE() AS DATE), 'confirmed', 'UNIT-01');

-- 10. HINDALCO PRICE (today's price)
INSERT INTO hindalco_price (price, effective_date, status, unit_code)
VALUES
(242.75, CAST(GETDATE() AS DATE), 'confirmed', 'UNIT-01');

-- 11. RACK AND BIN MASTER (Default 7 + extras per FRD)
INSERT INTO rack_bin_master (store_name, storage_area, rack_number, column_number, bin_number, status, unit_code, capacity)
VALUES
('RM Inward', 'Area-A', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('RM Inward', 'Area-A', 'R01', 'C01', 'B02', 'available', 'UNIT-01', 'Standard'),
('RM Inward', 'Area-A', 'R01', 'C02', 'B01', 'available', 'UNIT-01', 'Standard'),
('RM Inward', 'Area-B', 'R02', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('QC Hold', 'Area-QC', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('QC Reject', 'Area-QC', 'R02', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('Scrap', 'Area-SC', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('Machine', 'Area-MC', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('FG Store', 'Area-FG', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard'),
('FG Store', 'Area-FG', 'R01', 'C02', 'B01', 'available', 'UNIT-01', 'Standard'),
('Returnable', 'Area-RT', 'R01', 'C01', 'B01', 'available', 'UNIT-01', 'Standard');

-- 12. SUB-CONTRACTOR MASTER
INSERT INTO sub_contractor_master (sub_contractor_code, sub_contractor_name, email, phone, process_type, status, unit_code, created_date)
VALUES
('SC-001', 'Premium Anodizing Works', 'anodize@works.com', '9393939393', 'Anodizing', 'approved', 'UNIT-01', GETDATE());

-- 13. PRODUCT MARGIN
INSERT INTO product_margin (material_type, product_category, credit_period_tier, margin_percentage, status, unit_code)
SELECT 'Aluminium', 'Sheet', '0-30 Days', 8.50, 'approved', 'UNIT-01'
WHERE NOT EXISTS (SELECT 1 FROM product_margin WHERE material_type = 'Aluminium' AND product_category = 'Sheet');

INSERT INTO product_margin (material_type, product_category, credit_period_tier, margin_percentage, status, unit_code)
SELECT 'Aluminium', 'Coil', '0-30 Days', 9.00, 'approved', 'UNIT-01'
WHERE NOT EXISTS (SELECT 1 FROM product_margin WHERE material_type = 'Aluminium' AND product_category = 'Coil');

INSERT INTO product_margin (material_type, product_category, credit_period_tier, margin_percentage, status, unit_code)
SELECT 'Aluminium', 'Plate', '0-30 Days', 7.50, 'approved', 'UNIT-01'
WHERE NOT EXISTS (SELECT 1 FROM product_margin WHERE material_type = 'Aluminium' AND product_category = 'Plate');

PRINT 'Seed data inserted successfully!';
