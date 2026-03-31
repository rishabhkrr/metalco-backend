-- ============================================================
-- MetalCO ERP - Complete Database Schema
-- Database: metalco (Microsoft SQL Server)
-- Generated: 2026-03-30 14:54:24
-- Tables: 163
-- ============================================================

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'metalco')
BEGIN
    CREATE DATABASE metalco;
END
GO
USE metalco;
GO

-- ------------------------------------------------------------
-- Table: address_details
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'address_details')
BEGIN
    CREATE TABLE [address_details] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [address] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [country] varchar(255) NULL,
        [is_primary] bit NOT NULL,
        [map_location] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [supplier_area] varchar(255) NULL,
        [supplier_id] bigint NULL,
        CONSTRAINT [PK_address_details] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: adjustments
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'adjustments')
BEGIN
    CREATE TABLE [adjustments] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [quantity_diff] bigint NULL,
        [ref_no] varchar(255) NULL,
        [sku_id] bigint NULL,
        [sku_name] varchar(255) NULL,
        [status] bigint NULL,
        [stock_id] bigint NULL,
        [store_id] bigint NULL,
        [store_name] varchar(255) NULL,
        [type] varchar(255) NULL,
        CONSTRAINT [PK_adjustments] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: audit_logs
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'audit_logs')
BEGIN
    CREATE TABLE [audit_logs] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [action] varchar(30) NOT NULL,
        [description] varchar(200) NULL,
        [entity_id] bigint NULL,
        [entity_ref] varchar(100) NULL,
        [entity_type] varchar(100) NOT NULL,
        [module_name] varchar(50) NOT NULL,
        [new_value] nvarchar(MAX) NULL,
        [performed_at] datetime2 NOT NULL,
        [performed_by] varchar(100) NOT NULL,
        [previous_value] nvarchar(MAX) NULL,
        [unit_code] varchar(50) NULL,
        CONSTRAINT [PK_audit_logs] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: bank_details
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bank_details')
BEGIN
    CREATE TABLE [bank_details] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [account_number] varchar(255) NULL,
        [account_type] varchar(255) NULL,
        [bank_country] varchar(255) NULL,
        [bank_name] varchar(255) NULL,
        [beneficiary_name] varchar(255) NULL,
        [branch_address] varchar(255) NULL,
        [ifsc_code] varchar(255) NULL,
        [is_primary] bit NULL,
        [micr_code] varchar(255) NULL,
        [swift_code] varchar(255) NULL,
        [upi_id] varchar(255) NULL,
        [supplier_id] bigint NULL,
        CONSTRAINT [PK_bank_details] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: bill_stock_return
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bill_stock_return')
BEGIN
    CREATE TABLE [bill_stock_return] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [amount] numeric(38,2) NULL,
        [brand] varchar(255) NULL,
        [cgst] numeric(38,2) NULL,
        [created_at] datetime2 NULL,
        [created_by] varchar(255) NULL,
        [customer_billing_address] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_shipping_address] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [igst] numeric(38,2) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [return_quantity_kg] numeric(38,2) NULL,
        [return_quantity_no] numeric(38,2) NULL,
        [sales_return_number] varchar(255) NULL,
        [sgst] numeric(38,2) NULL,
        [so_number] varchar(255) NULL,
        [stock_selection] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [total_amount] numeric(38,2) NULL,
        [transportation_charges] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [approval_status] varchar(255) NULL,
        [dc_debit_note_number] varchar(255) NULL,
        [dc_number] varchar(255) NULL,
        [dispatch_quantity_kg] numeric(38,2) NULL,
        [dispatch_quantity_no] numeric(38,2) NULL,
        [e_way_bill] varchar(255) NULL,
        [e_way_bill_number_return] varchar(255) NULL,
        [vehicle_number] varchar(255) NULL,
        [vehicle_number_return] varchar(255) NULL,
        CONSTRAINT [PK_bill_stock_return] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: billing_summary
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'billing_summary')
BEGIN
    CREATE TABLE [billing_summary] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [amount] numeric(38,2) NULL,
        [billing_status] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [cgst] numeric(38,2) NULL,
        [coc] varchar(255) NULL,
        [credit_period_days] int NULL,
        [customer_billing_address] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_shipping_address] varchar(255) NULL,
        [cutting_charges] numeric(38,2) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [igst] numeric(38,2) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [lamination_charges] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_charges] numeric(38,2) NULL,
        [packing_status] varchar(255) NULL,
        [price] numeric(38,2) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [sgst] numeric(38,2) NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [total_amount] numeric(38,2) NULL,
        [transportation_charges] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_billing_summary] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: bin_damage
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bin_damage')
BEGIN
    CREATE TABLE [bin_damage] (
        [combination_id] bigint IDENTITY(1,1) NOT NULL,
        [bin_name] varchar(255) NULL,
        [created_date] datetime2 NULL,
        [part_number] varchar(255) NULL,
        [quantity] int NOT NULL,
        CONSTRAINT [PK_bin_damage] PRIMARY KEY ([combination_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: bins
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bins')
BEGIN
    CREATE TABLE [bins] (
        [bin_id] bigint IDENTITY(1,1) NOT NULL,
        [bin_location] varchar(255) NULL,
        [bin_name] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        [store_id] bigint NULL,
        CONSTRAINT [PK_bins] PRIMARY KEY ([bin_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: blocked_product
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'blocked_product')
BEGIN
    CREATE TABLE [blocked_product] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [available_quantity_kg] numeric(38,2) NULL,
        [item_description] varchar(255) NULL,
        [blocked_quantity_id] bigint NULL,
        CONSTRAINT [PK_blocked_product] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: blocked_quantity
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'blocked_quantity')
BEGIN
    CREATE TABLE [blocked_quantity] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [created_at] datetime2 NULL,
        [customer_name] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [pdf_link] varchar(255) NULL,
        [quotation_no] varchar(255) NULL,
        CONSTRAINT [PK_blocked_quantity] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: brands
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'brands')
BEGIN
    CREATE TABLE [brands] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand_name] varchar(255) NOT NULL,
        CONSTRAINT [PK_brands] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: category
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'category')
BEGIN
    CREATE TABLE [category] (
        [category_id] bigint IDENTITY(1,1) NOT NULL,
        [category_name] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        CONSTRAINT [PK_category] PRIMARY KEY ([category_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: certificate_of_confidence
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'certificate_of_confidence')
BEGIN
    CREATE TABLE [certificate_of_confidence] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [coc_number] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [customer_billing_address] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customerpodate] datetime2 NULL,
        [customerponumber] varchar(255) NULL,
        [customerpoquantity] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [customer_shipping_address] varchar(255) NULL,
        [declaration] text NULL,
        [dispatched_quantity] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [updated_at] datetime2 NULL,
        CONSTRAINT [PK_certificate_of_confidence] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: coc_line_items
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'coc_line_items')
BEGIN
    CREATE TABLE [coc_line_items] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [coc_number] varchar(255) NULL,
        [coc_timestamp] datetime2 NULL,
        [created_at] datetime2 NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [updated_at] datetime2 NULL,
        [coc_id] bigint NULL,
        CONSTRAINT [PK_coc_line_items] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: contact_details
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'contact_details')
BEGIN
    CREATE TABLE [contact_details] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [designation] varchar(255) NULL,
        [email_id] varchar(255) NULL,
        [is_primary] bit NOT NULL,
        [name] varchar(255) NULL,
        [phone_number] varchar(255) NULL,
        [supplier_id] bigint NULL,
        CONSTRAINT [PK_contact_details] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: credit_debit_note
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'credit_debit_note')
BEGIN
    CREATE TABLE [credit_debit_note] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [cgst] numeric(18,2) NULL,
        [created_at] datetime2 NULL,
        [created_by] varchar(255) NULL,
        [customer_billing_address] varchar(2000) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_shipping_address] varchar(2000) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [igst] numeric(18,2) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(500) NULL,
        [item_price] numeric(18,2) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [receiver] varchar(255) NULL,
        [sgst] numeric(18,2) NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [total_amount] numeric(18,2) NULL,
        [transaction_amount] numeric(18,2) NULL,
        [transaction_number] varchar(255) NULL,
        [transaction_type] varchar(255) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_credit_debit_note] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: customer_address
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_address')
BEGIN
    CREATE TABLE [customer_address] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [address] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [country] varchar(255) NULL,
        [map_location] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [supplier_area] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_customer_address] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: customer_bank_detail
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_bank_detail')
BEGIN
    CREATE TABLE [customer_bank_detail] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [account_number] varchar(255) NULL,
        [account_type] varchar(255) NULL,
        [bank_country] varchar(255) NULL,
        [bank_name] varchar(255) NULL,
        [beneficiary_name] varchar(255) NULL,
        [branch_address] varchar(255) NULL,
        [ifsc_code] varchar(255) NULL,
        [micr_code] varchar(255) NULL,
        [swift_code] varchar(255) NULL,
        [upi_id] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_customer_bank_detail] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: customer_contact
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_contact')
BEGIN
    CREATE TABLE [customer_contact] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [designation] varchar(255) NULL,
        [email_id] varchar(255) NULL,
        [name] varchar(255) NULL,
        [phone_number] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_customer_contact] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: customer_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_master')
BEGIN
    CREATE TABLE [customer_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [application_of_industry] varchar(255) NULL,
        [credit_limit_amount] float NULL,
        [credit_limit_days] int NULL,
        [customer_category] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_nickname] varchar(255) NULL,
        [gst_certificate_link] varchar(255) NULL,
        [gst_or_uin] varchar(255) NULL,
        [gst_registration_type] varchar(255) NULL,
        [gst_state_code] varchar(255) NULL,
        [iec_code] varchar(255) NULL,
        [interest_calculation] varchar(255) NULL,
        [is_iec_available] varchar(255) NULL,
        [is_tan_available] varchar(255) NULL,
        [is_udyam_available] varchar(255) NULL,
        [lock_period_days] int NULL,
        [mailing_billing_name] varchar(255) NULL,
        [multiple_address] bit NULL,
        [other_documents] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [pan_file_upload] varchar(255) NULL,
        [rate_of_interest] float NULL,
        [related_status] varchar(255) NULL,
        [status] varchar(255) NULL,
        [tan_number] varchar(255) NULL,
        [type_of_entity] varchar(255) NULL,
        [type_of_industry] varchar(255) NULL,
        [udyam_file_upload] varchar(255) NULL,
        [udyam_number] varchar(255) NULL,
        CONSTRAINT [PK_customer_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: customer_registration_verification
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'customer_registration_verification')
BEGIN
    CREATE TABLE [customer_registration_verification] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_remarks] varchar(255) NULL,
        [alt_email] varchar(255) NULL,
        [alt_phone] varchar(255) NULL,
        [country] varchar(255) NULL,
        [credit_days] int NULL,
        [credit_limit] float NULL,
        [customer_address] varchar(255) NULL,
        [customer_area] varchar(255) NULL,
        [customer_category] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone_no] varchar(255) NULL,
        [gst_certificate] varchar(255) NULL,
        [gst_uin] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [remarks] varchar(255) NULL,
        [state] varchar(255) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_customer_registration_verification] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: cutting_machine_config
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'cutting_machine_config')
BEGIN
    CREATE TABLE [cutting_machine_config] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [ideal_blade_speed] int NULL,
        [ideal_cutting_feed] int NULL,
        [max_cutting_length] int NULL,
        [max_cutting_thickness] int NULL,
        [min_cutting_length] int NULL,
        [min_cutting_thickness] int NULL,
        [machine_id] bigint NULL,
        CONSTRAINT [PK_cutting_machine_config] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: dealers
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dealers')
BEGIN
    CREATE TABLE [dealers] (
        [dealer_id] bigint IDENTITY(1,1) NOT NULL,
        [appointed_by] varchar(255) NULL,
        [appointed_date] datetime2 NULL,
        [datetime] datetime2 NULL,
        [dealer_name] varchar(255) NULL,
        [dealer_phone] varchar(255) NULL,
        [status] int NULL,
        CONSTRAINT [PK_dealers] PRIMARY KEY ([dealer_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: delivery_challan_creation_iumt
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'delivery_challan_creation_iumt')
BEGIN
    CREATE TABLE [delivery_challan_creation_iumt] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [dcnumber] varchar(255) NULL,
        [amount] numeric(38,2) NULL,
        [brand] varchar(255) NULL,
        [destination] varchar(255) NULL,
        [dispatch_through] varchar(255) NULL,
        [eway_bill_number] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [other_reference] varchar(255) NULL,
        [packing_list_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [remarks] varchar(255) NULL,
        [requesting_billing_address] varchar(255) NULL,
        [requesting_code] varchar(255) NULL,
        [requesting_name] varchar(255) NULL,
        [requesting_shipping_address] varchar(255) NULL,
        [status] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [terms_of_delivery] varchar(255) NULL,
        [timestamp] datetimeoffset NULL,
        [total_amount] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [vehicle_number_packing_and_dispatch] varchar(255) NULL,
        [vehicle_out_status_packing_and_dispatch] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grn_status] varchar(255) NULL,
        [hsn_code] varchar(255) NULL,
        [igst_amount] numeric(38,2) NULL,
        [igst_percent] numeric(38,2) NULL,
        [pan_number] varchar(255) NULL,
        [requesting_unit_gst] varchar(255) NULL,
        [requesting_unit_state] varchar(255) NULL,
        [sender_unit_gst] varchar(255) NULL,
        [sender_unit_state] varchar(255) NULL,
        [sub_total_amount] numeric(38,2) NULL,
        CONSTRAINT [PK_delivery_challan_creation_iumt] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: delivery_challan_jw
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'delivery_challan_jw')
BEGIN
    CREATE TABLE [delivery_challan_jw] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [amount] float NULL,
        [brand] varchar(255) NULL,
        [destination] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [dispatch_through] varchar(255) NULL,
        [eway_bill_number] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [medc_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [other_reference] varchar(255) NULL,
        [packing_list_number] varchar(255) NULL,
        [packing_status] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [remarks] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [sub_contractor_billing_address] varchar(255) NULL,
        [sub_contractor_code] varchar(255) NULL,
        [sub_contractor_name] varchar(255) NULL,
        [sub_contractor_shipping_address] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [terms_of_delivery] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [total_amount] float NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [vehicle_number_packing_and_dispatch] varchar(255) NULL,
        CONSTRAINT [PK_delivery_challan_jw] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: departments
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'departments')
BEGIN
    CREATE TABLE [departments] (
        [transfer_id] bigint IDENTITY(1,1) NOT NULL,
        [added_quantity_net_weight] float NULL,
        [added_quantity_net_weight_uom] varchar(255) NULL,
        [added_quantity_no] int NULL,
        [added_quantity_no_uom] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        [current_store] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [soft_delete] int NULL,
        [delete_remark] varchar(255) NULL,
        [dispatch_date] datetime2 NULL,
        [from_store] bigint NULL,
        [grade] varchar(255) NULL,
        [grn_quantity_net_weight] float NULL,
        [grn_quantity_net_weight_uom] varchar(255) NULL,
        [grn_quantity_no] int NULL,
        [grn_quantity_no_uom] varchar(255) NULL,
        [grn_ref_number] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [number_of_bundles] int NULL,
        [product_category] varchar(255) NULL,
        [rack_column_bin_number] varchar(255) NULL,
        [recieving_date] datetime2 NULL,
        [recipient_store] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [status] int NULL,
        [storage_area] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [to_store] bigint NULL,
        [transfer_number] varchar(255) NULL,
        [transfer_quantity] varchar(255) NULL,
        [transfer_stage] varchar(255) NULL,
        [transfer_type] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [input_type] varchar(255) NULL,
        [material_acceptance] varchar(255) NULL,
        CONSTRAINT [PK_departments] PRIMARY KEY ([transfer_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: employees
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'employees')
BEGIN
    CREATE TABLE [employees] (
        [employee_id] bigint IDENTITY(1,1) NOT NULL,
        [datetime] datetime2 NULL,
        [employee_name] varchar(255) NULL,
        [employee_phone] varchar(255) NULL,
        [status] int NULL,
        CONSTRAINT [PK_employees] PRIMARY KEY ([employee_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: expense_category
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'expense_category')
BEGIN
    CREATE TABLE [expense_category] (
        [cat_id] bigint IDENTITY(1,1) NOT NULL,
        [cat_name] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        CONSTRAINT [PK_expense_category] PRIMARY KEY ([cat_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_entry_packing_and_dispatch
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_entry_packing_and_dispatch')
BEGIN
    CREATE TABLE [gate_entry_packing_and_dispatch] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [driver_name_packing_and_dispatch] varchar(255) NULL,
        [eway_bill_number_packing_and_dispatch] varchar(255) NULL,
        [gate_entry_ref_no] varchar(255) NULL,
        [invoice_number_packing_and_dispatch] varchar(255) NULL,
        [invoice_scan_packing_and_dispatch] varchar(255) NULL,
        [medc_number_packing_and_dispatch] varchar(255) NULL,
        [medc_scan] varchar(255) NULL,
        [medci_number_packing_and_dispatch] varchar(255) NULL,
        [medci_scan] varchar(255) NULL,
        [medcp_number_packing_and_dispatch] varchar(255) NULL,
        [medcp_scan] varchar(255) NULL,
        [mode_packing_and_dispatch] varchar(255) NULL,
        [purpose_packing_and_dispatch] varchar(255) NULL,
        [time_stamp_packing_and_dispatch] datetime2 NULL,
        [unit_packing_and_dispatch] varchar(255) NULL,
        [vehicle_documents_scan_packing_and_dispatch] varchar(255) NULL,
        [vehicle_number_packing_and_dispatch] varchar(255) NULL,
        [vehicle_out_status_packing_and_dispatch] varchar(255) NULL,
        [vehicle_weighment_status_packing_and_dispatch] varchar(255) NULL,
        CONSTRAINT [PK_gate_entry_packing_and_dispatch] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward')
BEGIN
    CREATE TABLE [gate_inward] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [dc_number] varchar(255) NULL,
        [driver_name] varchar(255) NULL,
        [e_way_bill_number] varchar(255) NULL,
        [gate_out_time] datetime2 NULL,
        [gate_pass_ref_number] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [material_unloading_status] varchar(255) NULL,
        [medc_number] varchar(255) NULL,
        [medci_number] varchar(255) NULL,
        [mode] varchar(255) NULL,
        [purpose] varchar(255) NULL,
        [status] varchar(255) NULL,
        [time_stamp] datetime2 NULL,
        [unit_code] varchar(255) NULL,
        [vehicle_number] varchar(255) NULL,
        [vehicle_out_status] varchar(255) NULL,
        [vehicle_weighment_status] varchar(255) NULL,
        CONSTRAINT [PK_gate_inward] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_dc_document_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_dc_document_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_dc_document_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [dc_document_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_e_way_bill_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_e_way_bill_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_e_way_bill_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [e_way_bill_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_invoice_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_invoice_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_invoice_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [invoice_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_material_unloading_status_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_material_unloading_status_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_material_unloading_status_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [material_unloading_status_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_medc_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_medc_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_medc_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [medc_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_medci_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_medci_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_medci_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [medci_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_po_numbers
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_po_numbers')
BEGIN
    CREATE TABLE [gate_inward_entity_po_numbers] (
        [gate_inward_entity_id] bigint NOT NULL,
        [po_numbers] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_test_certificate_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_test_certificate_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_test_certificate_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [test_certificate_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_test_certificates
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_test_certificates')
BEGIN
    CREATE TABLE [gate_inward_entity_test_certificates] (
        [gate_inward_entity_id] bigint NOT NULL,
        [pdf_link] varchar(255) NULL,
        [tc_number] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_vehicle_documents_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_vehicle_documents_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_vehicle_documents_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [vehicle_documents_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: gate_inward_entity_vehicle_weighment_scan_urls
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'gate_inward_entity_vehicle_weighment_scan_urls')
BEGIN
    CREATE TABLE [gate_inward_entity_vehicle_weighment_scan_urls] (
        [gate_inward_entity_id] bigint NOT NULL,
        [vehicle_weighment_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: grade_density
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grade_density')
BEGIN
    CREATE TABLE [grade_density] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [density] numeric(20,10) NULL,
        [grade] varchar(255) NULL,
        CONSTRAINT [PK_grade_density] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grades
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grades')
BEGIN
    CREATE TABLE [grades] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [grade_value] varchar(255) NOT NULL,
        CONSTRAINT [PK_grades] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn')
BEGIN
    CREATE TABLE [grn] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [bin_status] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [eway_bill_document] varchar(255) NULL,
        [eway_bill_number] varchar(255) NULL,
        [gate_entry_ref_no] varchar(255) NULL,
        [grn_ref_number] varchar(255) NULL,
        [invoice_document] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [material_unloading_notes] varchar(255) NULL,
        [material_unloading_status] varchar(255) NULL,
        [medc_number] varchar(255) NULL,
        [mode] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [test_certificate_document] varchar(255) NULL,
        [test_certificate_numbers] varbinary(255) NULL,
        [time_stamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [updated_at] datetime2 NULL,
        [vehicle_documents] varchar(255) NULL,
        [vehicle_empty_weight_kg] float NULL,
        [vehicle_load_weight_kg] float NULL,
        [vehicle_number] varchar(255) NULL,
        [weighment_quantity] float NULL,
        [weighment_ref_number] varchar(255) NULL,
        CONSTRAINT [PK_grn] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_inter_unit
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_inter_unit')
BEGIN
    CREATE TABLE [grn_inter_unit] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [created_at] datetime2 NULL,
        [eway_bill_document] varchar(255) NULL,
        [eway_bill_number] varchar(255) NULL,
        [gate_entry_ref_no] varchar(255) NULL,
        [grn_inter_unit_ref_number] varchar(255) NULL,
        [invoice_document] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [medc_number] varchar(255) NULL,
        [mode] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [supplier_unit] varchar(255) NULL,
        [test_certificate_document] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [vehicle_documents] varchar(255) NULL,
        [vehicle_empty_weight_kg] float NULL,
        [vehicle_load_weight_kg] float NULL,
        [vehicle_number] varchar(255) NULL,
        [weighment_quantity] float NULL,
        [weighment_ref_number] varchar(255) NULL,
        [approved_at] datetime2 NULL,
        [approved_by] varchar(255) NULL,
        [rejection_remarks] varchar(1000) NULL,
        [sender_unit] varchar(255) NULL,
        [status] varchar(255) NULL,
        CONSTRAINT [PK_grn_inter_unit] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_inter_unit_items
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_inter_unit_items')
BEGIN
    CREATE TABLE [grn_inter_unit_items] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] float NULL,
        [received_net_weight] float NULL,
        [received_no] int NULL,
        [temper] varchar(255) NULL,
        [test_certificate_number] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [grn_inter_unit_id] bigint NULL,
        [batch_number] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [heat_number] varchar(255) NULL,
        [item_price] float NULL,
        [lot_number] varchar(255) NULL,
        [quantity_no] int NULL,
        [scan_status] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        CONSTRAINT [PK_grn_inter_unit_items] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_item')
BEGIN
    CREATE TABLE [grn_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [heat_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [lot_number] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [po_quantity_kg] float NULL,
        [product_category] varchar(255) NULL,
        [rate] float NULL,
        [received_gross_weight] float NULL,
        [received_net_weight] float NULL,
        [received_no] int NULL,
        [section_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [test_certificate_number] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [value] float NULL,
        [grn_id] bigint NULL,
        CONSTRAINT [PK_grn_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_jobwork
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_jobwork')
BEGIN
    CREATE TABLE [grn_jobwork] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [e_way_bill_number] varchar(255) NULL,
        [empty_weight] float NULL,
        [gate_pass_ref_number] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [grn_dimension] varchar(255) NULL,
        [grn_ref_number] varchar(255) NULL,
        [grn_uom_kg] varchar(255) NULL,
        [grn_uom_no] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [jobwork_rate] float NULL,
        [jobwork_value] float NULL,
        [load_weight] float NULL,
        [material_unloading_status] varchar(255) NULL,
        [medc_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] float NULL,
        [quantity_no] int NULL,
        [received_quantity_kg] float NULL,
        [received_quantity_nos] int NULL,
        [scrap_quantity_kg] float NULL,
        [sub_contractor_code] varchar(255) NULL,
        [sub_contractor_name] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [vehicle_number] varchar(255) NULL,
        [weightment_ref_number] varchar(255) NULL,
        [amount_as_sent] float NULL,
        [dc_number] varchar(255) NULL,
        [item_pricej] float NULL,
        [item_price_medc] float NULL,
        [master_approval_status] varchar(255) NULL,
        [scrap_quantity_no] int NULL,
        CONSTRAINT [PK_grn_jobwork] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_jobwork_dc_scans
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_jobwork_dc_scans')
BEGIN
    CREATE TABLE [grn_jobwork_dc_scans] (
        [grn_id] bigint NOT NULL,
        [dc_document_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_jobwork_eway_scans
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_jobwork_eway_scans')
BEGIN
    CREATE TABLE [grn_jobwork_eway_scans] (
        [grn_id] bigint NOT NULL,
        [e_way_bill_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_jobwork_invoice_scans
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_jobwork_invoice_scans')
BEGIN
    CREATE TABLE [grn_jobwork_invoice_scans] (
        [grn_id] bigint NOT NULL,
        [invoice_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_jobwork_vehicle_scans
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_jobwork_vehicle_scans')
BEGIN
    CREATE TABLE [grn_jobwork_vehicle_scans] (
        [grn_id] bigint NOT NULL,
        [vehicle_documents_scan_urls] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: grn_line_items
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'grn_line_items')
BEGIN
    CREATE TABLE [grn_line_items] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [after_stock_transfer_qty_kg] numeric(38,2) NULL,
        [after_stock_transfer_qty_no] int NULL,
        [brand] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        [created_date] datetime2 NULL,
        [current_store] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [grn_id] bigint NULL,
        [grn_number] varchar(255) NULL,
        [input_type] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [material_acceptance] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [qr_code] varchar(500) NULL,
        [qr_code_image_url] varchar(MAX) NULL,
        [qr_generated] bit NULL,
        [rack_column_bin_number] varchar(255) NULL,
        [rack_status] varchar(255) NULL,
        [recipient_store] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [sl_no] varchar(255) NULL,
        [status] varchar(255) NULL,
        [stock_transfer_id] bigint NULL,
        [storage_area] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [transfer_number] varchar(255) NULL,
        [unit_id] varchar(255) NULL,
        [uom_net_weight] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [updated_by] varchar(255) NULL,
        [updated_date] datetime2 NULL,
        [user_id] varchar(255) NULL,
        [weighment] varchar(255) NULL,
        [weightment_quantity_kg] numeric(38,2) NULL,
        [weightment_quantity_no] int NULL,
        [allocation_status] varchar(255) NULL,
        [batch_number] varchar(255) NULL,
        [heat_number] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [lot_number] varchar(255) NULL,
        [stock_type] varchar(255) NULL,
        CONSTRAINT [PK_grn_line_items] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: hindalco_price
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'hindalco_price')
BEGIN
    CREATE TABLE [hindalco_price] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [price] float NULL,
        [price_date] datetime2 NULL,
        [price_pdf_path] varchar(255) NULL,
        [uom] varchar(255) NULL,
        CONSTRAINT [PK_hindalco_price] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: hsn_code_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'hsn_code_master')
BEGIN
    CREATE TABLE [hsn_code_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [effective_date] date NULL,
        [gst_effective_date] date NULL,
        [gst_rate] varchar(255) NULL,
        [hsn_code] varchar(255) NULL,
        [lastgst_rate] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [previous_hsn_code] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [status] varchar(255) NULL,
        [description] varchar(255) NULL,
        CONSTRAINT [PK_hsn_code_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: item_enquiry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_enquiry')
BEGIN
    CREATE TABLE [item_enquiry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [address] varchar(255) NULL,
        [approval_status] varchar(255) NULL,
        [blocked_pdflinked] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [cgst] numeric(38,2) NULL,
        [country] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [current_selling_price] float NULL,
        [customer_address] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [customer_type] varchar(255) NULL,
        [cutting_charges] numeric(38,2) NULL,
        [delivery_days] int NOT NULL,
        [freight_charges] numeric(38,2) NULL,
        [hamali_charges] numeric(38,2) NULL,
        [igst] numeric(38,2) NULL,
        [is_locked] bit NULL,
        [lamination_charges] numeric(38,2) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [note1] varchar(255) NULL,
        [note2] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_charges] numeric(38,2) NULL,
        [payment_terms] varchar(255) NULL,
        [pdf_link] varchar(255) NULL,
        [pincode] int NULL,
        [product_detail_note] varchar(255) NULL,
        [pvc_applicable] bit NOT NULL,
        [quotation_no] varchar(255) NULL,
        [remarks] varchar(255) NULL,
        [sgst] numeric(38,2) NULL,
        [state] varchar(255) NULL,
        [status] varchar(255) NULL,
        [sub_total_amount] numeric(38,2) NULL,
        [supplier_area] varchar(255) NULL,
        [taxes] varchar(255) NULL,
        [total_amount] numeric(38,2) NULL,
        [unit_address] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_email] varchar(255) NULL,
        [unit_gst_number] varchar(255) NULL,
        [unit_id] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        [updated_at] datetime2 NULL,
        [updated_by] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        [validity_hours] int NOT NULL,
        CONSTRAINT [PK_item_enquiry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: item_enquiry_additional_charges
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_enquiry_additional_charges')
BEGIN
    CREATE TABLE [item_enquiry_additional_charges] (
        [item_enquiry_id] bigint NOT NULL,
        [additional_charges] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: item_enquiry_moq
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_enquiry_moq')
BEGIN
    CREATE TABLE [item_enquiry_moq] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [item] varchar(255) NULL,
        [min_qty] int NOT NULL,
        [enquiry_id] bigint NULL,
        CONSTRAINT [PK_item_enquiry_moq] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: item_enquiry_product
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_enquiry_product')
BEGIN
    CREATE TABLE [item_enquiry_product] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [category] varchar(255) NULL,
        [current_selling_price] float NULL,
        [description] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [length] float NOT NULL,
        [material_type] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [price] float NOT NULL,
        [product_selected_id] int NULL,
        [quantity] float NOT NULL,
        [quantity_in_no] int NULL,
        [required_brand] varchar(255) NULL,
        [required_category] varchar(255) NULL,
        [required_grade] varchar(255) NULL,
        [required_length] float NULL,
        [required_quantity] float NULL,
        [required_temper] varchar(255) NULL,
        [required_thickness] float NULL,
        [required_uom] varchar(255) NULL,
        [required_width] float NULL,
        [temper] varchar(255) NULL,
        [thickness] float NOT NULL,
        [uom] varchar(255) NULL,
        [width] float NOT NULL,
        [enquiry_id] bigint NULL,
        CONSTRAINT [PK_item_enquiry_product] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: item_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'item_master')
BEGIN
    CREATE TABLE [item_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [alt_uom] varchar(255) NULL,
        [alt_uom_applicable] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [dimension1] varchar(255) NULL,
        [dimension2] varchar(255) NULL,
        [dimension3] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [gst_applicable] varchar(255) NULL,
        [gst_rate] numeric(38,2) NULL,
        [hsn_code] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [lead_time_days] int NULL,
        [material_type] varchar(255) NULL,
        [moq] numeric(38,2) NULL,
        [narration] varchar(255) NULL,
        [opening_stock_in_kgs] numeric(38,2) NULL,
        [opening_stock_in_nos] numeric(38,2) NULL,
        [primary_uom] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [reporting_uom] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [sku_description] varchar(255) NULL,
        [status] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_item_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: lamination_machine_config
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'lamination_machine_config')
BEGIN
    CREATE TABLE [lamination_machine_config] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [conveyor_feed] int NULL,
        [ideal_roller_speed] int NULL,
        [max_sheet_thickness] int NULL,
        [min_sheet_thickness] int NULL,
        [machine_id] bigint NULL,
        CONSTRAINT [PK_lamination_machine_config] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: ledger
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ledger')
BEGIN
    CREATE TABLE [ledger] (
        [ledger_id] bigint IDENTITY(1,1) NOT NULL,
        [credit] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [entity_id] bigint NULL,
        [debit] varchar(255) NULL,
        [expense_name] varchar(255) NULL,
        [expense_user] varchar(255) NULL,
        [file_url] varchar(255) NULL,
        [receipt_no] varchar(255) NULL,
        [ref_no] varchar(255) NULL,
        [store] bigint NULL,
        [transaction_type] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        CONSTRAINT [PK_ledger] PRIMARY KEY ([ledger_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: low_stock_alert
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'low_stock_alert')
BEGIN
    CREATE TABLE [low_stock_alert] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [pr_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [reorder_level] numeric(38,2) NULL,
        [reorder_quantity] numeric(38,2) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_low_stock_alert] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: machine_maintenance_activity
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'machine_maintenance_activity')
BEGIN
    CREATE TABLE [machine_maintenance_activity] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [breakdown_minutes] bigint NULL,
        [breakdown_reason] varchar(255) NULL,
        [end_time] datetime2 NULL,
        [machine_name] varchar(255) NULL,
        [remarks] varchar(255) NULL,
        [start_time] datetime2 NULL,
        [status] varchar(255) NULL,
        CONSTRAINT [PK_machine_maintenance_activity] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: machine_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'machine_master')
BEGIN
    CREATE TABLE [machine_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [machine_id] varchar(255) NULL,
        [machine_name] varchar(255) NULL,
        [machine_specifications] varchar(255) NULL,
        [machine_type] varchar(255) NULL,
        [manufacturer] varchar(255) NULL,
        [model_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_machine_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: manual_cash_in_hand
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'manual_cash_in_hand')
BEGIN
    CREATE TABLE [manual_cash_in_hand] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [cash_in_hand] bigint NULL,
        [creation_date] datetime2 NULL,
        [day_date] varchar(255) NULL,
        [store_id] bigint NULL,
        CONSTRAINT [PK_manual_cash_in_hand] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: margin_rate
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'margin_rate')
BEGIN
    CREATE TABLE [margin_rate] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [days] varchar(255) NOT NULL,
        [margin_percent] float NOT NULL,
        [product_margin_id] bigint NULL,
        CONSTRAINT [PK_margin_rate] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: material_request_header
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'material_request_header')
BEGIN
    CREATE TABLE [material_request_header] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [delivery_address] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [requesting_unit] varchar(255) NULL,
        [requesting_unit_unit_code] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_material_request_header] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: material_request_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'material_request_item')
BEGIN
    CREATE TABLE [material_request_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [required_quantity] int NULL,
        [status] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [header_id] bigint NULL,
        CONSTRAINT [PK_material_request_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: material_request_summary_header
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'material_request_summary_header')
BEGIN
    CREATE TABLE [material_request_summary_header] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [status] varchar(255) NULL,
        [delivery_address] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [requesting_unit] varchar(255) NULL,
        [requesting_unit_unit_code] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_material_request_summary_header] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: material_request_summary_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'material_request_summary_item')
BEGIN
    CREATE TABLE [material_request_summary_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [required_quantity] int NULL,
        [status] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [summary_header_id] bigint NULL,
        CONSTRAINT [PK_material_request_summary_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: material_type
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'material_type')
BEGIN
    CREATE TABLE [material_type] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [name] varchar(255) NULL,
        CONSTRAINT [PK_material_type] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: nalco_price
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nalco_price')
BEGIN
    CREATE TABLE [nalco_price] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [date] date NULL,
        [nalco_price] numeric(38,2) NULL,
        [price_pdf] varchar(255) NULL,
        [uom] varchar(255) NULL,
        CONSTRAINT [PK_nalco_price] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_customer
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_customer')
BEGIN
    CREATE TABLE [new_customer] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [application_of_industry] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [credit_limit_amount] float NULL,
        [credit_limit_days] int NULL,
        [customer_based] varchar(255) NULL,
        [customer_category] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_nickname] varchar(255) NULL,
        [gst_certificate_link] varchar(255) NULL,
        [gst_or_uin] varchar(255) NULL,
        [gst_registration_type] varchar(255) NULL,
        [gst_state_code] varchar(255) NULL,
        [iec_code] varchar(255) NULL,
        [interest_calculation] varchar(255) NULL,
        [is_iec_available] varchar(255) NULL,
        [is_tan_available] varchar(255) NULL,
        [is_udyam_available] varchar(255) NULL,
        [lock_period_days] int NULL,
        [mailing_billing_name] varchar(255) NULL,
        [multiple_address] bit NULL,
        [other_documents] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [rate_of_interest] float NULL,
        [status] varchar(255) NULL,
        [tan_number] varchar(255) NULL,
        [type_of_industry] varchar(255) NULL,
        [udyam_number] varchar(255) NULL,
        CONSTRAINT [PK_new_customer] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_customer_address
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_customer_address')
BEGIN
    CREATE TABLE [new_customer_address] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [address] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [country] varchar(255) NULL,
        [map_location] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [supplier_area] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_new_customer_address] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_customer_bank_detail
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_customer_bank_detail')
BEGIN
    CREATE TABLE [new_customer_bank_detail] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [account_number] varchar(255) NULL,
        [account_type] varchar(255) NULL,
        [bank_country] varchar(255) NULL,
        [bank_name] varchar(255) NULL,
        [beneficiary_name] varchar(255) NULL,
        [branch_address] varchar(255) NULL,
        [ifsc_code] varchar(255) NULL,
        [micr_code] varchar(255) NULL,
        [swift_code] varchar(255) NULL,
        [upi_id] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_new_customer_bank_detail] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_customer_contact
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_customer_contact')
BEGIN
    CREATE TABLE [new_customer_contact] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [designation] varchar(255) NULL,
        [email_id] varchar(255) NULL,
        [name] varchar(255) NULL,
        [phone_number] varchar(255) NULL,
        [customer_id] bigint NULL,
        CONSTRAINT [PK_new_customer_contact] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_packing_instruction
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_packing_instruction')
BEGIN
    CREATE TABLE [new_packing_instruction] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_remarks] varchar(255) NULL,
        [type_of_packing] varchar(255) NULL,
        [weight_instructions] varchar(255) NULL,
        [new_sales_order_id] bigint NULL,
        CONSTRAINT [PK_new_packing_instruction] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: new_sales_order
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'new_sales_order')
BEGIN
    CREATE TABLE [new_sales_order] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [acknowledgement_sent] bit NOT NULL,
        [approval_link_sent] bit NOT NULL,
        [billing_address] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [credit_period] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [customer_po_file] varchar(255) NULL,
        [customer_po_no] varchar(255) NULL,
        [management_authority] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [packing_required] bit NOT NULL,
        [pdf_link] varchar(255) NULL,
        [quotation_no] varchar(255) NULL,
        [remark] varchar(255) NULL,
        [same_as_billing_address] bit NULL,
        [shipping_address] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [target_date_of_dispatch] date NULL,
        [unit] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_new_sales_order] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: notifications
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'notifications')
BEGIN
    CREATE TABLE [notifications] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [created_at] datetime2 NOT NULL,
        [entity_id] bigint NULL,
        [entity_type] varchar(100) NULL,
        [is_read] bit NOT NULL,
        [message] varchar(1000) NOT NULL,
        [module_name] varchar(50) NULL,
        [read_at] datetime2 NULL,
        [recipient_role] varchar(50) NULL,
        [recipient_user_id] bigint NOT NULL,
        [title] varchar(200) NOT NULL,
        [type] varchar(50) NOT NULL,
        [unit_code] varchar(50) NULL,
        CONSTRAINT [PK_notifications] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_batch_detail
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_batch_detail')
BEGIN
    CREATE TABLE [packing_batch_detail] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [date_of_inward] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_dimensions] varchar(255) NULL,
        [qty_kg] numeric(38,2) NULL,
        [qty_no] int NULL,
        [packing_submission_id] bigint NOT NULL,
        CONSTRAINT [PK_packing_batch_detail] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_instruction
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_instruction')
BEGIN
    CREATE TABLE [packing_instruction] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_remarks] varchar(255) NULL,
        [type_of_packing] varchar(255) NULL,
        [weight_instructions] varchar(255) NULL,
        [new_sales_order_id] bigint NULL,
        [sales_order_id] bigint NULL,
        CONSTRAINT [PK_packing_instruction] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_list_creation_iumt
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_list_creation_iumt')
BEGIN
    CREATE TABLE [packing_list_creation_iumt] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [billing_address] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing] bit NULL,
        [packing_list_no] varchar(255) NULL,
        [packing_status] varchar(255) NULL,
        [packing_type] varchar(255) NULL,
        [plan_date] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [requesting_unit] varchar(255) NULL,
        [requesting_unit_unit_code] varchar(255) NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [return_store] varchar(255) NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [sl_no] bigint NULL,
        [target_date_of_dispatch] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetimeoffset NULL,
        [unit] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [weighment_quantity_no] int NULL,
        CONSTRAINT [PK_packing_list_creation_iumt] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_list_jobwork
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_list_jobwork')
BEGIN
    CREATE TABLE [packing_list_jobwork] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_list_number] varchar(255) NOT NULL,
        [packing_status] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_packing_list_jobwork] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_list_transfer
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_list_transfer')
BEGIN
    CREATE TABLE [packing_list_transfer] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_list_number] varchar(255) NULL,
        [packing_status] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [transfer_status] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_packing_list_transfer] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_scheduler
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_scheduler')
BEGIN
    CREATE TABLE [packing_scheduler] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_instructions] varchar(255) NULL,
        [packing_status] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [target_date_of_dispatch] date NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_packing_scheduler] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: packing_submission
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'packing_submission')
BEGIN
    CREATE TABLE [packing_submission] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing_id] varchar(255) NULL,
        [packing_instructions] varchar(255) NULL,
        [packing_status] varchar(255) NULL,
        [pdf] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_packing_submission] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: pgm
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pgm')
BEGIN
    CREATE TABLE [pgm] (
        [pgm_id] bigint IDENTITY(1,1) NOT NULL,
        [appointed_by] varchar(255) NULL,
        [appointed_date] datetime2 NULL,
        [datetime] datetime2 NULL,
        [pgm_name] varchar(255) NULL,
        [pgm_phone] varchar(255) NULL,
        [status] int NULL,
        CONSTRAINT [PK_pgm] PRIMARY KEY ([pgm_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: pick_list_scheduler
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pick_list_scheduler')
BEGIN
    CREATE TABLE [pick_list_scheduler] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [rack_column_shelf_number] varchar(255) NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [selected_bundles_json] text NULL,
        [selection_reason] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [storage_area] varchar(255) NULL,
        [storage_type] varchar(255) NULL,
        [store] varchar(255) NULL,
        [total_bundles_selected] int NULL,
        [total_selected_quantity_kg] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_pick_list_scheduler] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: picklist
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'picklist')
BEGIN
    CREATE TABLE [picklist] (
        [pick_id] bigint IDENTITY(1,1) NOT NULL,
        [bin_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [ordered_quantity] bigint NULL,
        [picked] varchar(255) NULL,
        [quantity] bigint NULL,
        [ref_no] bigint NULL,
        [sku_id] bigint NULL,
        [status] int NULL,
        [stockin_id] bigint NULL,
        [store_id] bigint NULL,
        [type] varchar(255) NULL,
        CONSTRAINT [PK_picklist] PRIMARY KEY ([pick_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: po_generation
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'po_generation')
BEGIN
    CREATE TABLE [po_generation] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [billing_address] varchar(255) NULL,
        [dispatch_through] varchar(255) NULL,
        [other_reference] varchar(255) NULL,
        [pdflink] varchar(255) NULL,
        [po_generated_by] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [po_status] varchar(255) NULL,
        [po_validity] date NULL,
        [remarks] varchar(255) NULL,
        [rm_receipt_status] varchar(255) NULL,
        [shipping_address] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [time_stamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_po_generation] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: po_generation_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'po_generation_item')
BEGIN
    CREATE TABLE [po_generation_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [delivery_address] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [pr_created_by] varchar(255) NULL,
        [pr_number] varchar(255) NULL,
        [pr_type_and_reason_verifiaction] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [received_net_weight] float NULL,
        [required_quantity] float NULL,
        [rm_receipt_status] varchar(255) NULL,
        [section_no] varchar(255) NULL,
        [so_line_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [po_generation_id] bigint NULL,
        CONSTRAINT [PK_po_generation_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: po_product
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'po_product')
BEGIN
    CREATE TABLE [po_product] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [required_quantity] float NULL,
        [section_no] varchar(255) NULL,
        [selected] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [po_request_id] bigint NULL,
        CONSTRAINT [PK_po_product] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: po_request
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'po_request')
BEGIN
    CREATE TABLE [po_request] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [order_type] varchar(255) NULL,
        [pr_created_by] varchar(255) NULL,
        [pr_number] varchar(255) NOT NULL,
        [reason_for_request] varchar(255) NULL,
        [so_number_line_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [time_stamp] datetime2 NOT NULL,
        [unit] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        CONSTRAINT [PK_po_request] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: pogeneration_entity_terms_of_delivery
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pogeneration_entity_terms_of_delivery')
BEGIN
    CREATE TABLE [pogeneration_entity_terms_of_delivery] (
        [pogeneration_entity_id] bigint NOT NULL,
        [terms_of_delivery] varchar(255) NULL
    );
END
GO

-- ------------------------------------------------------------
-- Table: pomanagement_approval_entity
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pomanagement_approval_entity')
BEGIN
    CREATE TABLE [pomanagement_approval_entity] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [billing_address] varchar(255) NULL,
        [order_date] date NULL,
        [po_generated_by] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity] float NULL,
        [remarks] varchar(255) NULL,
        [shipping_address] varchar(255) NULL,
        [status] varchar(255) NULL,
        [supplier] varchar(255) NULL,
        [supplier_lead_time] int NULL,
        [suppliermoq] int NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_pomanagement_approval_entity] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: pomanagement_approval_item_entity
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pomanagement_approval_item_entity')
BEGIN
    CREATE TABLE [pomanagement_approval_item_entity] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [delivery_address] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [pr_created_by] varchar(255) NULL,
        [pr_number] varchar(255) NULL,
        [pr_type_and_reason_verifiaction] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [required_quantity] float NULL,
        [section_no] varchar(255) NULL,
        [so_line_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [approval_id] bigint NULL,
        CONSTRAINT [PK_pomanagement_approval_item_entity] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: price_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'price_master')
BEGIN
    CREATE TABLE [price_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [dealer] varchar(255) NULL,
        [pgm] varchar(255) NULL,
        [retail] varchar(255) NULL,
        [service] varchar(255) NULL,
        [sku_id] bigint NULL,
        CONSTRAINT [PK_price_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: process_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'process_entry')
BEGIN
    CREATE TABLE [process_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_processes] varchar(255) NULL,
        [mode] varchar(255) NULL,
        [operation_type] varchar(255) NULL,
        [packing_style] varchar(255) NULL,
        [packing_type] varchar(255) NULL,
        [process_type] varchar(255) NULL,
        CONSTRAINT [PK_process_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: product_category
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'product_category')
BEGIN
    CREATE TABLE [product_category] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [name] varchar(255) NULL,
        CONSTRAINT [PK_product_category] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: product_margin
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'product_margin')
BEGIN
    CREATE TABLE [product_margin] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [material_type] varchar(255) NOT NULL,
        [product_category] varchar(255) NOT NULL,
        [status] varchar(255) NOT NULL,
        [timestamp] datetime2 NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_product_margin] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: production_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'production_entry')
BEGIN
    CREATE TABLE [production_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [calculated_weight] numeric(38,2) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [end_time] varchar(255) NULL,
        [generated_qr_image] varchar(MAX) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [machine_break_down] bit NULL,
        [machine_name] varchar(255) NULL,
        [next_production_process] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing] bit NULL,
        [produced_qty_kg] numeric(38,2) NULL,
        [produced_qty_no] int NULL,
        [product_category] varchar(255) NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [start_time] varchar(255) NULL,
        [target_blade_speed] float NULL,
        [target_dispatch_date] date NULL,
        [target_feed] float NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetimeoffset NULL,
        [total_metres_cut] float NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_production_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: production_entry_end_piece
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'production_entry_end_piece')
BEGIN
    CREATE TABLE [production_entry_end_piece] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [calculated_weight_kg] numeric(38,2) NULL,
        [end_piece_dimension] varchar(255) NULL,
        [end_piece_quantity_kg] numeric(38,2) NULL,
        [end_piece_quantity_no] numeric(38,2) NULL,
        [end_piece_type] varchar(255) NULL,
        [length] numeric(38,2) NULL,
        [generated_qr_image] varchar(MAX) NULL,
        [qr_generate] bit NULL,
        [thickness] numeric(38,2) NULL,
        [width] numeric(38,2) NULL,
        [production_entry_id] bigint NULL,
        CONSTRAINT [PK_production_entry_end_piece] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: production_idle_time_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'production_idle_time_entry')
BEGIN
    CREATE TABLE [production_idle_time_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [end_time] varchar(255) NULL,
        [idle_minutes] int NULL,
        [idle_reason] varchar(255) NULL,
        [machine_name] varchar(255) NULL,
        [remarks] varchar(255) NULL,
        [start_time] varchar(255) NULL,
        [timestamp] datetimeoffset NULL,
        [production_entry_id] bigint NULL,
        CONSTRAINT [PK_production_idle_time_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: production_schedule
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'production_schedule')
BEGIN
    CREATE TABLE [production_schedule] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [end_time] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [machine_name] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [next_production_process] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing] bit NULL,
        [product_category] varchar(255) NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [rm_quantity_kg] numeric(38,2) NULL,
        [so_number] varchar(255) NULL,
        [start_time] varchar(255) NULL,
        [status] varchar(255) NULL DEFAULT ('PENDING'),
        [target_blade_speed] float NULL,
        [target_dispatch_date] date NULL,
        [target_feed] float NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_production_schedule] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: purchase_credit_debit_note
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'purchase_credit_debit_note')
BEGIN
    CREATE TABLE [purchase_credit_debit_note] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [cgst] numeric(18,2) NULL,
        [created_at] datetime2 NULL,
        [created_by] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [igst] numeric(18,2) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(18,2) NULL,
        [po_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [receiver] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [sgst] numeric(18,2) NULL,
        [supplier_billing_address] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [supplier_shipping_address] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [total_amount] numeric(18,2) NULL,
        [transaction_amount] numeric(18,2) NULL,
        [transaction_number] varchar(255) NULL,
        [transaction_type] varchar(255) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_purchase_credit_debit_note] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: purchase_follow_up
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'purchase_follow_up')
BEGIN
    CREATE TABLE [purchase_follow_up] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [created_at] datetime2 NULL,
        [item_description] varchar(255) NULL,
        [line_item_number] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [po_order_date] datetime2 NULL,
        [po_status] varchar(255) NULL,
        [required_quantity] float NULL,
        [sales_order_number] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [unit] varchar(255) NULL,
        CONSTRAINT [PK_purchase_follow_up] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: purchase_follow_up_v2
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'purchase_follow_up_v2')
BEGIN
    CREATE TABLE [purchase_follow_up_v2] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [billing_address] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [follow_up_status] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_item_number] varchar(255) NULL,
        [line_item_number2] varchar(255) NULL,
        [order_date] datetime2 NULL,
        [po_generated_by] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [po_quantity_kg] float NULL,
        [pr_created_by] varchar(255) NULL,
        [pr_number] varchar(255) NULL,
        [pr_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [required_quantity] int NULL,
        [sales_order_number] varchar(255) NULL,
        [section_no] varchar(255) NULL,
        [shipping_address] varchar(255) NULL,
        [supplier] varchar(255) NULL,
        [target_dispatch_date] date NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom] varchar(255) NULL,
        CONSTRAINT [PK_purchase_follow_up_v2] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: purchase_return
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'purchase_return')
BEGIN
    CREATE TABLE [purchase_return] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [created_by] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [purchase_return_number] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [section_number] varchar(255) NULL,
        [supplier_code] varchar(255) NULL,
        [supplier_name] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        CONSTRAINT [PK_purchase_return] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: rack_bin_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'rack_bin_master')
BEGIN
    CREATE TABLE [rack_bin_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [automated] bit NOT NULL,
        [bin_capacity] varchar(255) NULL,
        [bin_no] varchar(255) NULL,
        [column_no] varchar(255) NULL,
        [current_storage] float NULL,
        [distance] float NULL,
        [item_category] varchar(255) NULL,
        [qr] varchar(255) NULL,
        [rack_no] varchar(255) NULL,
        [status] varchar(255) NULL,
        [storage_area] varchar(255) NULL,
        [storage_area_order] int NULL,
        [storage_type] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_rack_bin_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: raw_material_qr
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'raw_material_qr')
BEGIN
    CREATE TABLE [raw_material_qr] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [bin_no] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [column_no] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [grn_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [rack_column_bin] varchar(255) NULL,
        [rack_no] varchar(255) NULL,
        [raw_material_qr_id] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [sl_no] varchar(255) NULL,
        [storage_area] varchar(255) NULL,
        [store] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [item_code] varchar(255) NULL,
        [location_status] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        CONSTRAINT [PK_raw_material_qr] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: requisition
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'requisition')
BEGIN
    CREATE TABLE [requisition] (
        [requisition_id] bigint IDENTITY(1,1) NOT NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [receipt_url] varchar(255) NULL,
        [status] int NULL,
        [store_name] varchar(255) NULL,
        CONSTRAINT [PK_requisition] PRIMARY KEY ([requisition_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: return_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'return_entry')
BEGIN
    CREATE TABLE [return_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [return_store] varchar(255) NULL,
        [sl_no] int NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [parent_id] bigint NOT NULL,
        CONSTRAINT [PK_return_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sales_authorities
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sales_authorities')
BEGIN
    CREATE TABLE [sales_authorities] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [name] varchar(255) NULL,
        CONSTRAINT [PK_sales_authorities] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sales_order
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sales_order')
BEGIN
    CREATE TABLE [sales_order] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [acknowledgement_sent] bit NULL,
        [approval_link_sent] bit NULL,
        [approval_remarks] varchar(255) NULL,
        [billing_address] varchar(255) NULL,
        [cgst] numeric(38,2) NULL,
        [created_at] datetime2 NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_overdue] bit NULL,
        [customer_phone] varchar(255) NULL,
        [customer_po_file] varchar(255) NULL,
        [customer_po_no] varchar(255) NULL,
        [cutting_charges] numeric(38,2) NULL,
        [freight_charges] numeric(38,2) NULL,
        [hamali_charges] numeric(38,2) NULL,
        [igst] numeric(38,2) NULL,
        [lamination_charges] numeric(38,2) NULL,
        [management_authority] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [packing_charges] numeric(38,2) NULL,
        [packing_required] bit NULL,
        [pdflink] varchar(255) NULL,
        [quotation_no] varchar(255) NULL,
        [same_as_billing_address] bit NULL,
        [sgst] numeric(38,2) NULL,
        [shipping_address] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [sub_total_amount] numeric(38,2) NULL,
        [target_dispatch_date] date NULL,
        [total_amount] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_sales_order] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sales_order_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sales_order_item')
BEGIN
    CREATE TABLE [sales_order_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [credit_period_days] int NULL,
        [current_price] float NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [order_mode] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [production_strategy] varchar(255) NULL,
        [quantity_kg] int NULL,
        [quantity_nos] int NULL,
        [target_dispatch_date] date NULL,
        [temper] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_nos] varchar(255) NULL,
        [sales_order_id] bigint NULL,
        CONSTRAINT [PK_sales_order_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sales_order_line_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sales_order_line_item')
BEGIN
    CREATE TABLE [sales_order_line_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [credit_period] int NULL,
        [current_price] float NOT NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [order_mode] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing] bit NULL,
        [price_snapshot] varchar(MAX) NULL,
        [product_category] varchar(255) NULL,
        [production_strategy] varchar(255) NULL,
        [quantity_kg] float NOT NULL,
        [quantity_nos] float NOT NULL,
        [sl_no] int NULL,
        [status] varchar(255) NULL,
        [stock_summary] varchar(MAX) NULL,
        [target_dispatch_date] date NULL,
        [temper] varchar(255) NULL,
        [total_price] numeric(38,2) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_nos] varchar(255) NULL,
        [sales_order_id] bigint NULL,
        CONSTRAINT [PK_sales_order_line_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sales_order_scheduler
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sales_order_scheduler')
BEGIN
    CREATE TABLE [sales_order_scheduler] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [completed_time] datetime2 NULL,
        [customer_category] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [packing] bit NULL,
        [plan_date] date NULL,
        [prime_customer] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [production_strategy] varchar(255) NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [retrieval_status] varchar(255) NULL,
        [sl_no] int NULL,
        [so_number] varchar(255) NULL,
        [target_date_of_dispatch] date NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [pick_list_id] bigint NULL,
        [stock_transfer_id] bigint NULL,
        CONSTRAINT [PK_sales_order_scheduler] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: salesman_incentive_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'salesman_incentive_entry')
BEGIN
    CREATE TABLE [salesman_incentive_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [amount] numeric(38,2) NULL,
        [amount_received] numeric(38,2) NULL,
        [credit_days] int NULL,
        [customer_code] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [date_of_payment] date NULL,
        [dimension] varchar(255) NULL,
        [dispatch_date] date NULL,
        [dispatch_quantity_kg] numeric(38,2) NULL,
        [dispatch_quantity_no] int NULL,
        [final_incentive_amount] numeric(38,2) NULL,
        [grade] varchar(255) NULL,
        [incentive_amount] numeric(38,2) NULL,
        [incentive_rate] numeric(38,2) NULL,
        [item_description] varchar(255) NULL,
        [lapse_interest_amount] numeric(38,2) NULL,
        [lapse_interest_rate] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [number_of_days_lapse] int NULL,
        [order_quantity_kg] numeric(38,2) NULL,
        [order_quantity_no] int NULL,
        [order_type] varchar(255) NULL,
        [payment_status] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [target_date_of_payment] date NULL,
        [temper] varchar(255) NULL,
        [total_amount] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_salesman_incentive_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: salesman_incentive_rates
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'salesman_incentive_rates')
BEGIN
    CREATE TABLE [salesman_incentive_rates] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [effective_date] date NULL,
        [lapse_interest_rate] float NULL,
        [material_grade_and_temper] varchar(255) NULL,
        [rate_per_kg] float NULL,
        [salesman_id] bigint NULL,
        CONSTRAINT [PK_salesman_incentive_rates] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: salesman_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'salesman_master')
BEGIN
    CREATE TABLE [salesman_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [date_of_ending] date NULL,
        [date_of_joining] date NULL,
        [department] varchar(255) NULL,
        [designation] varchar(255) NULL,
        [modules_with_access] varchar(255) NULL,
        [status] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        [user_id_status] varchar(255) NULL,
        [user_name] varchar(255) NULL,
        CONSTRAINT [PK_salesman_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: scheduler_packing_instruction
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'scheduler_packing_instruction')
BEGIN
    CREATE TABLE [scheduler_packing_instruction] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_remarks] varchar(255) NULL,
        [type_of_packing] varchar(255) NULL,
        [weight_instructions] varchar(255) NULL,
        [scheduler_id] bigint NULL,
        CONSTRAINT [PK_scheduler_packing_instruction] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: scrap_summary
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'scrap_summary')
BEGIN
    CREATE TABLE [scrap_summary] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [length] numeric(38,2) NULL,
        [line_number] varchar(255) NULL,
        [machine_name] varchar(255) NULL,
        [produced_qty_kg] numeric(38,2) NULL,
        [produced_qty_no] int NULL,
        [product_category] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [thickness] numeric(38,2) NULL,
        [timestamp] datetimeoffset NULL,
        [unit] varchar(255) NULL,
        [width] numeric(38,2) NULL,
        CONSTRAINT [PK_scrap_summary] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sku
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sku')
BEGIN
    CREATE TABLE [sku] (
        [sku_id] bigint IDENTITY(1,1) NOT NULL,
        [category_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [dealer] varchar(255) NULL,
        [sku_description] varchar(255) NULL,
        [pgm] varchar(255) NULL,
        [retail] varchar(255) NULL,
        [service] varchar(255) NULL,
        [sku_class] varchar(255) NULL,
        [sku_code] varchar(255) NULL,
        [sku_image] varchar(255) NULL,
        [sku_lead_time] varchar(255) NULL,
        [sku_max] bigint NULL,
        [sku_min] bigint NULL,
        [sku_moq] varchar(255) NULL,
        [sku_name] varchar(255) NULL,
        [sku_qrcode] varchar(255) NULL,
        [sku_quantity] bigint NULL,
        [sku_reorder_point] bigint NULL,
        [sku_supplier] varchar(255) NULL,
        [sku_unit_cost] bigint NULL,
        [sku_uom] varchar(255) NULL,
        [status] int NULL,
        [sub_category_id] bigint NULL,
        CONSTRAINT [PK_sku] PRIMARY KEY ([sku_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_schedule_pick_list
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_schedule_pick_list')
BEGIN
    CREATE TABLE [so_schedule_pick_list] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [delivery_address] varchar(255) NULL,
        [generate_qr] bit NULL,
        [generated_qr_image] varchar(MAX) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_status] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [material_type] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [qr_code] text NULL,
        [requesting_unit] varchar(255) NULL,
        [requesting_unit_unit_code] varchar(255) NULL,
        [required_quantity] int NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [status] varchar(255) NULL,
        [storage_area] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [timestamp] datetimeoffset NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        [uom] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [weighment_quantity_no] int NULL,
        CONSTRAINT [PK_so_schedule_pick_list] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_summary
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_summary')
BEGIN
    CREATE TABLE [so_summary] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone_no] varchar(255) NULL,
        [customer_po_no] varchar(255) NULL,
        [management_authority] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [packing_status] bit NULL,
        [quotation_no] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [timestamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_so_summary] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_summary_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_summary_item')
BEGIN
    CREATE TABLE [so_summary_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [amount] numeric(38,2) NULL,
        [brand] varchar(255) NULL,
        [credit_days] int NULL,
        [dimension] varchar(255) NULL,
        [dispatch_date] date NULL,
        [dispatch_quantity_kg] numeric(38,2) NULL,
        [dispatch_quantity_no] int NULL,
        [grade] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [lr_number_updation] varchar(255) NULL,
        [order_quantity_kg] numeric(38,2) NULL,
        [order_quantity_no] int NULL,
        [order_type] varchar(255) NULL,
        [price] numeric(38,2) NULL,
        [product_category] varchar(255) NULL,
        [production_strategy] varchar(255) NULL,
        [so_status] varchar(255) NULL,
        [target_dispatch_date] date NULL,
        [temper] varchar(255) NULL,
        [total_amount] numeric(38,2) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [summary_id] bigint NULL,
        CONSTRAINT [PK_so_summary_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_update
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_update')
BEGIN
    CREATE TABLE [so_update] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [billing_address] varchar(255) NULL,
        [created_at] datetime2 NULL,
        [credit_period] varchar(255) NULL,
        [customer_code] varchar(255) NULL,
        [customer_email] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [customer_po_no] varchar(255) NULL,
        [management_authority] varchar(255) NULL,
        [marketing_executive_name] varchar(255) NULL,
        [packing_required] bit NOT NULL,
        [pdf_link] varchar(255) NULL,
        [quotation_no] varchar(255) NULL,
        [remark] varchar(255) NULL,
        [shipping_address] varchar(255) NULL,
        [so_number] varchar(255) NULL,
        [status] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        CONSTRAINT [PK_so_update] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_update_item
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_update_item')
BEGIN
    CREATE TABLE [so_update_item] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [credit_period_days] int NULL,
        [current_price] float NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [order_mode] varchar(255) NULL,
        [order_type] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [production_strategy] varchar(255) NULL,
        [quantity_kg] int NULL,
        [quantity_nos] int NULL,
        [target_dispatch_date] date NULL,
        [temper] varchar(255) NULL,
        [uom_kg] varchar(255) NULL,
        [uom_nos] varchar(255) NULL,
        [so_update_id] bigint NULL,
        CONSTRAINT [PK_so_update_item] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: so_update_packing_instruction
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'so_update_packing_instruction')
BEGIN
    CREATE TABLE [so_update_packing_instruction] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [additional_remarks] varchar(255) NULL,
        [type_of_packing] varchar(255) NULL,
        [weight_instructions] varchar(255) NULL,
        [so_update_id] bigint NULL,
        CONSTRAINT [PK_so_update_packing_instruction] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_returns
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_returns')
BEGIN
    CREATE TABLE [stock_returns] (
        [return_id] bigint IDENTITY(1,1) NOT NULL,
        [advance_payment] varchar(255) NULL,
        [bin_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [payment_terms] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [soft_delete] int NULL,
        [delete_remark] varchar(255) NULL,
        [discount_amount] varchar(255) NULL,
        [job_card_no] varchar(255) NULL,
        [labour_charges] varchar(255) NULL,
        [labour_charges_desp] varchar(255) NULL,
        [payment_status] varchar(255) NULL,
        [payment_type] varchar(255) NULL,
        [ref_no] varchar(255) NULL,
        [return_due] varchar(255) NULL,
        [return_type] varchar(255) NULL,
        [seq_no] varchar(255) NULL,
        [sku_quantity] bigint NULL,
        [status] int NULL,
        [store_id] bigint NULL,
        [store_name] varchar(255) NULL,
        [tax_percentage] varchar(255) NULL,
        [total_bill] varchar(255) NULL,
        [transaction_type] varchar(255) NULL,
        [transfer_stage] varchar(255) NULL,
        [transfer_type] varchar(255) NULL,
        CONSTRAINT [PK_stock_returns] PRIMARY KEY ([return_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_summary
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_summary')
BEGIN
    CREATE TABLE [stock_summary] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [brand] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [grn_numbers] text NULL,
        [item_description] varchar(255) NULL,
        [item_group] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [length] numeric(38,2) NULL,
        [material_type] varchar(255) NULL,
        [pick_list_locked] bit NULL,
        [product_category] varchar(255) NULL,
        [qr_code] varchar(255) NULL,
        [quantity_kg] numeric(38,2) NULL,
        [quantity_no] int NULL,
        [rack_column_shelf_number] varchar(255) NULL,
        [reprint_qr] bit NULL,
        [section_no] varchar(255) NULL,
        [storage_area] varchar(255) NULL,
        [store] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [thickness] numeric(38,2) NULL,
        [unit] varchar(255) NULL,
        [width] numeric(38,2) NULL,
        CONSTRAINT [PK_stock_summary] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_summary_bundles
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_summary_bundles')
BEGIN
    CREATE TABLE [stock_summary_bundles] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [created_by] varchar(255) NULL,
        [created_date] datetime2 NULL,
        [current_store] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [grade] varchar(255) NULL,
        [grn_id] bigint NULL,
        [grn_number] varchar(255) NULL,
        [heat_no] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [item_price] numeric(38,2) NULL,
        [lot_no] varchar(255) NULL,
        [material_acceptance] varchar(255) NULL,
        [po_number] varchar(255) NULL,
        [product_category] varchar(255) NULL,
        [qr_code_url] varchar(255) NULL,
        [rack_column_bin_number] varchar(255) NULL,
        [rack_status] varchar(255) NULL,
        [recipient_store] varchar(255) NULL,
        [section_number] varchar(255) NULL,
        [sl_no] varchar(255) NULL,
        [status] varchar(255) NULL,
        [stock_transfer_id] bigint NULL,
        [storage_area] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [test_certificate] varchar(255) NULL,
        [transfer_number] varchar(255) NULL,
        [transfer_type] varchar(255) NULL,
        [unit_id] varchar(255) NULL,
        [uom_net_weight] varchar(255) NULL,
        [uom_no] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        [weighment] varchar(255) NULL,
        [weightment_quantity_kg] numeric(38,2) NULL,
        [weightment_quantity_no] int NULL,
        [stock_summary_id] bigint NOT NULL,
        CONSTRAINT [PK_stock_summary_bundles] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_transfer_warehouse
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_transfer_warehouse')
BEGIN
    CREATE TABLE [stock_transfer_warehouse] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [generate_qr] bit NULL,
        [generated_qr_image] varchar(MAX) NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [qr_code] text NULL,
        [required_quantity] numeric(38,2) NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [weighment_quantity_no] int NULL,
        CONSTRAINT [PK_stock_transfer_warehouse] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_transfer_wh_retrieval_qty_entry
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_transfer_wh_retrieval_qty_entry')
BEGIN
    CREATE TABLE [stock_transfer_wh_retrieval_qty_entry] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [warehouse_id] bigint NULL,
        CONSTRAINT [PK_stock_transfer_wh_retrieval_qty_entry] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_transfer_wh_return
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_transfer_wh_return')
BEGIN
    CREATE TABLE [stock_transfer_wh_return] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [line_number] varchar(255) NULL,
        [mr_number] varchar(255) NULL,
        CONSTRAINT [PK_stock_transfer_wh_return] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stock_trn_sku
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stock_trn_sku')
BEGIN
    CREATE TABLE [stock_trn_sku] (
        [sku_trn_id] bigint IDENTITY(1,1) NOT NULL,
        [datetime] datetime2 NULL,
        [dispatch_quantity] varchar(255) NULL,
        [hold_quantity] varchar(255) NULL,
        [picked] varchar(255) NULL,
        [recieved_quantity] varchar(255) NULL,
        [returned_quantity] varchar(255) NULL,
        [sku_price] varchar(255) NULL,
        [sku_total] varchar(255) NULL,
        [transfer_number] varchar(255) NULL,
        [transfer_quantity] varchar(255) NULL,
        [transfer_sku_code] varchar(255) NULL,
        [transfer_sku_name] varchar(255) NULL,
        CONSTRAINT [PK_stock_trn_sku] PRIMARY KEY ([sku_trn_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stockin
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stockin')
BEGIN
    CREATE TABLE [stockin] (
        [stock_id] bigint IDENTITY(1,1) NOT NULL,
        [bin_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [soft_delete] int NULL,
        [delete_remark] varchar(255) NULL,
        [prev_bin_id] bigint NULL,
        [prev_store_id] varchar(255) NULL,
        [recieving_date] datetime2 NULL,
        [sku_quantity_hold] bigint NULL,
        [sku_id] bigint NULL,
        [sku_name] varchar(255) NULL,
        [sku_quantity] bigint NULL,
        [status] int NULL,
        [sto_id] varchar(255) NULL,
        [store_id] bigint NULL,
        [store_name] varchar(255) NULL,
        [transfer_number] varchar(255) NULL,
        [type] varchar(255) NULL,
        CONSTRAINT [PK_stockin] PRIMARY KEY ([stock_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stockout
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stockout')
BEGIN
    CREATE TABLE [stockout] (
        [stock_id] bigint IDENTITY(1,1) NOT NULL,
        [advance_payment] varchar(255) NULL,
        [bin_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [payment_terms] varchar(255) NULL,
        [customer_name] varchar(255) NULL,
        [customer_phone] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [soft_delete] int NULL,
        [delete_remark] varchar(255) NULL,
        [discount_amount] varchar(255) NULL,
        [invoice_remark] varchar(255) NULL,
        [job_card_no] varchar(255) NULL,
        [labour_charges] varchar(255) NULL,
        [labour_charges_desp] varchar(255) NULL,
        [payment_status] varchar(255) NULL,
        [payment_type] varchar(255) NULL,
        [progress_state] varchar(255) NULL,
        [sku_id] bigint NULL,
        [sku_name] varchar(255) NULL,
        [sku_quantity] bigint NULL,
        [status] int NULL,
        [store_id] bigint NULL,
        [store_name] varchar(255) NULL,
        [tax_percentage] varchar(255) NULL,
        [total_bill] varchar(255) NULL,
        [transaction_type] varchar(255) NULL,
        [transfer_stage] varchar(255) NULL,
        [transfer_type] varchar(255) NULL,
        [type] varchar(255) NULL,
        CONSTRAINT [PK_stockout] PRIMARY KEY ([stock_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: storage_area_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'storage_area_master')
BEGIN
    CREATE TABLE [storage_area_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [storage_area_name] varchar(255) NULL,
        [storage_area_order] int NULL,
        CONSTRAINT [PK_storage_area_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: store_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'store_master')
BEGIN
    CREATE TABLE [store_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [store_name] varchar(255) NULL,
        CONSTRAINT [PK_store_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: stores
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stores')
BEGIN
    CREATE TABLE [stores] (
        [store_id] bigint IDENTITY(1,1) NOT NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        [store_code] varchar(255) NULL,
        [store_name] varchar(255) NULL,
        CONSTRAINT [PK_stores] PRIMARY KEY ([store_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_category
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_category')
BEGIN
    CREATE TABLE [sub_category] (
        [sub_id] bigint IDENTITY(1,1) NOT NULL,
        [category_id] bigint NULL,
        [created_by] varchar(255) NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        [sub_name] varchar(255) NULL,
        CONSTRAINT [PK_sub_category] PRIMARY KEY ([sub_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_contractor_address
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_contractor_address')
BEGIN
    CREATE TABLE [sub_contractor_address] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [address] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [country] varchar(255) NULL,
        [map_location] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [supplier_area] varchar(255) NULL,
        [sub_contractor_id] bigint NULL,
        CONSTRAINT [PK_sub_contractor_address] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_contractor_bank
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_contractor_bank')
BEGIN
    CREATE TABLE [sub_contractor_bank] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [account_number] varchar(255) NULL,
        [account_type] varchar(255) NULL,
        [bank_country] varchar(255) NULL,
        [bank_name] varchar(255) NULL,
        [beneficiary_name] varchar(255) NULL,
        [branch_address] varchar(255) NULL,
        [ifsc_code] varchar(255) NULL,
        [micr_code] varchar(255) NULL,
        [swift_code] varchar(255) NULL,
        [upi_id] varchar(255) NULL,
        [sub_contractor_id] bigint NULL,
        CONSTRAINT [PK_sub_contractor_bank] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_contractor_contact
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_contractor_contact')
BEGIN
    CREATE TABLE [sub_contractor_contact] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [designation] varchar(255) NULL,
        [email_id] varchar(255) NULL,
        [name] varchar(255) NULL,
        [phone_number] varchar(255) NULL,
        [sub_contractor_id] bigint NULL,
        CONSTRAINT [PK_sub_contractor_contact] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_contractor_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_contractor_master')
BEGIN
    CREATE TABLE [sub_contractor_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [gst_certificate_path] varchar(255) NULL,
        [gst_or_uin] varchar(255) NULL,
        [gst_registration_type] varchar(255) NULL,
        [gst_state_code] varchar(255) NULL,
        [iec_code] varchar(255) NULL,
        [interest_calculation] varchar(255) NULL,
        [is_iec_available] varchar(255) NULL,
        [is_tan_available] varchar(255) NULL,
        [is_udyam_available] varchar(255) NULL,
        [mailing_billing_name] varchar(255) NULL,
        [multiple_addresses] bit NULL,
        [other_documents_path] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [pan_file_upload] varchar(255) NULL,
        [rate_of_interest] float NULL,
        [related_status] varchar(255) NULL,
        [status] varchar(255) NULL,
        [sub_contractor_code] varchar(255) NOT NULL,
        [sub_contractor_name] varchar(255) NULL,
        [sub_contractor_type] varchar(255) NULL,
        [supplier_category] varchar(255) NULL,
        [supplier_nickname] varchar(255) NULL,
        [tan_number] varchar(255) NULL,
        [type_of_entity] varchar(255) NULL,
        [udyam_file_upload] varchar(255) NULL,
        [udyam_number] varchar(255) NULL,
        CONSTRAINT [PK_sub_contractor_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: sub_module_access
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sub_module_access')
BEGIN
    CREATE TABLE [sub_module_access] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [approve_access] bit NULL,
        [create_access] bit NULL,
        [delete_access] bit NULL,
        [edit_access] bit NULL,
        [view_access] bit NULL,
        [sub_module_name] varchar(255) NULL,
        [user_id] bigint NULL,
        CONSTRAINT [PK_sub_module_access] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: supplier_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'supplier_master')
BEGIN
    CREATE TABLE [supplier_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [gst_certificate_path] varchar(255) NULL,
        [gst_or_uin] varchar(255) NULL,
        [gst_registration_type] varchar(255) NULL,
        [gst_state_code] varchar(255) NULL,
        [iec_code] varchar(255) NULL,
        [interest_calculation] varchar(255) NULL,
        [is_iec_available] varchar(255) NULL,
        [is_tan_available] varchar(255) NULL,
        [is_udyam_available] varchar(255) NULL,
        [mailing_billing_name] varchar(255) NULL,
        [multiple_address] bit NULL,
        [other_documents_path] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [pan_file_upload] varchar(255) NULL,
        [rate_of_interest] float NULL,
        [related_status] varchar(255) NULL,
        [status] varchar(255) NULL,
        [supplier_category] varchar(255) NULL,
        [supplier_code] varchar(255) NOT NULL,
        [supplier_name] varchar(255) NULL,
        [supplier_nickname] varchar(255) NULL,
        [supplier_type] varchar(255) NULL,
        [tan_number] varchar(255) NULL,
        [type_of_entity] varchar(255) NULL,
        [udyam_file_upload] varchar(255) NULL,
        [udyam_number] varchar(255) NULL,
        CONSTRAINT [PK_supplier_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: suppliers
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'suppliers')
BEGIN
    CREATE TABLE [suppliers] (
        [supplier_id] bigint IDENTITY(1,1) NOT NULL,
        [datetime] datetime2 NULL,
        [status] int NULL,
        [supplier_name] varchar(255) NULL,
        CONSTRAINT [PK_suppliers] PRIMARY KEY ([supplier_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: tempers
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tempers')
BEGIN
    CREATE TABLE [tempers] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [temper_value] varchar(255) NOT NULL,
        CONSTRAINT [PK_tempers] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: unit_bank_details
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'unit_bank_details')
BEGIN
    CREATE TABLE [unit_bank_details] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [account_number] varchar(255) NULL,
        [account_type] varchar(255) NULL,
        [bank_country] varchar(255) NULL,
        [bank_name] varchar(255) NULL,
        [beneficiary_name] varchar(255) NULL,
        [branch_address] varchar(255) NULL,
        [ifsc_code] varchar(255) NULL,
        [micr_code] varchar(255) NULL,
        [swift_code] varchar(255) NULL,
        [upi_id] varchar(255) NULL,
        [unit_id] bigint NULL,
        CONSTRAINT [PK_unit_bank_details] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: unit_contact_details
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'unit_contact_details')
BEGIN
    CREATE TABLE [unit_contact_details] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [designation] varchar(255) NULL,
        [email_id] varchar(255) NULL,
        [name] varchar(255) NULL,
        [phone_number] varchar(255) NULL,
        [unit_id] bigint NULL,
        CONSTRAINT [PK_unit_contact_details] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: unit_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'unit_master')
BEGIN
    CREATE TABLE [unit_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [area] varchar(255) NULL,
        [country] varchar(255) NULL,
        [gst_certificate] varchar(255) NULL,
        [gst_or_uin] varchar(255) NULL,
        [gst_registration_type] varchar(255) NULL,
        [gst_state_code] varchar(255) NULL,
        [iec_code] varchar(255) NULL,
        [is_iec_available] varchar(255) NULL,
        [is_tan_available] varchar(255) NULL,
        [is_udyam_available] varchar(255) NULL,
        [map_location] varchar(255) NULL,
        [other_documents] varchar(255) NULL,
        [pan] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [status] varchar(255) NULL,
        [tan_number] varchar(255) NULL,
        [type_of_entity] varchar(255) NULL,
        [udyam_number] varchar(255) NULL,
        [unit_address] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        CONSTRAINT [PK_unit_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: unit_master_address
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'unit_master_address')
BEGIN
    CREATE TABLE [unit_master_address] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [is_primary] bit NULL,
        [address] varchar(255) NULL,
        [branch_name] varchar(255) NULL,
        [country] varchar(255) NULL,
        [map_location] varchar(255) NULL,
        [pincode] varchar(255) NULL,
        [state] varchar(255) NULL,
        [supplier_area] varchar(255) NULL,
        [unit_id] bigint NULL,
        CONSTRAINT [PK_unit_master_address] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: user_master
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_master')
BEGIN
    CREATE TABLE [user_master] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [date_of_ending] date NULL,
        [date_of_joining] date NULL,
        [department] varchar(255) NULL,
        [designation] varchar(255) NULL,
        [is_salesman_created] bit NULL,
        [modules_with_access] varchar(255) NULL,
        [password] varchar(255) NULL,
        [status] varchar(255) NULL,
        [unit_code] varchar(255) NULL,
        [unit_name] varchar(255) NULL,
        [user_id] varchar(255) NULL,
        [user_name] varchar(255) NULL,
        CONSTRAINT [PK_user_master] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: user_roles
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_roles')
BEGIN
    CREATE TABLE [user_roles] (
        [role_id] bigint IDENTITY(1,1) NOT NULL,
        [datetime] datetime2 NULL,
        [role_key] varchar(255) NULL,
        [role_name] varchar(255) NULL,
        CONSTRAINT [PK_user_roles] PRIMARY KEY ([role_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: users
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE [users] (
        [user_id] bigint IDENTITY(1,1) NOT NULL,
        [datetime] datetime2 NULL,
        [password] varchar(255) NULL,
        [role_id] bigint NULL,
        [store_id] bigint NULL,
        [user_email] varchar(255) NULL,
        [user_name] varchar(255) NULL,
        [warehouse_id] bigint NULL,
        CONSTRAINT [PK_users] PRIMARY KEY ([user_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: vehicle_weighment
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vehicle_weighment')
BEGIN
    CREATE TABLE [vehicle_weighment] (
        [weighment_id] bigint IDENTITY(1,1) NOT NULL,
        [created_at] datetime2 NULL,
        [dc_number] varchar(255) NULL,
        [empty_weight] float NULL,
        [gate_entry_ref_no] varchar(255) NULL,
        [invoice_number] varchar(255) NULL,
        [load_weight] float NULL,
        [medc_or_dc_numbers] text NULL,
        [medci_numbers] text NULL,
        [medcp_number] varchar(255) NULL,
        [mode] varchar(255) NULL,
        [po_numbers] text NULL,
        [purpose] varchar(255) NULL,
        [time_stamp] datetime2 NULL,
        [unit] varchar(255) NULL,
        [updated_at] datetime2 NULL,
        [user_id] varchar(255) NULL,
        [vehicle_number] varchar(255) NULL,
        [vehicle_photo_empty] varchar(255) NULL,
        [vehicle_photo_with_load] varchar(255) NULL,
        [verified] bit NULL,
        [weightment_ref_number] varchar(255) NULL,
        CONSTRAINT [PK_vehicle_weighment] PRIMARY KEY ([weighment_id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: warehouse_stock_retrieval
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'warehouse_stock_retrieval')
BEGIN
    CREATE TABLE [warehouse_stock_retrieval] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [stock_transfer_id] bigint NULL,
        CONSTRAINT [PK_warehouse_stock_retrieval] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: warehouse_stock_retrieval_scheduler
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'warehouse_stock_retrieval_scheduler')
BEGIN
    CREATE TABLE [warehouse_stock_retrieval_scheduler] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [batch_number] varchar(255) NULL,
        [date_of_inward] varchar(255) NULL,
        [dimension] varchar(255) NULL,
        [generate_print_qrfg] text NULL,
        [qr_coderm] text NULL,
        [rack_column_bin] varchar(255) NULL,
        [retrieval_quantity_kg] numeric(38,2) NULL,
        [retrieval_quantity_no] int NULL,
        [retrieved_quantity_kg] numeric(38,2) NULL,
        [retrieved_quantity_no] int NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [scan_qr_code] varchar(255) NULL,
        [source_bundle_id] bigint NULL,
        [source_stock_summary_id] bigint NULL,
        [storage_area] varchar(255) NULL,
        [store] varchar(255) NULL,
        [weighed_quantity_kg] numeric(38,2) NULL,
        [weighed_quantity_no] int NULL,
        [stock_transfer_id] bigint NULL,
        CONSTRAINT [PK_warehouse_stock_retrieval_scheduler] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: warehouse_stock_transfer
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'warehouse_stock_transfer')
BEGIN
    CREATE TABLE [warehouse_stock_transfer] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [generate_qr] bit NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [weighment_quantity_no] int NULL,
        CONSTRAINT [PK_warehouse_stock_transfer] PRIMARY KEY ([id])
    );
END
GO

-- ------------------------------------------------------------
-- Table: warehouse_stock_transfer_scheduler
-- ------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'warehouse_stock_transfer_scheduler')
BEGIN
    CREATE TABLE [warehouse_stock_transfer_scheduler] (
        [id] bigint IDENTITY(1,1) NOT NULL,
        [brand] varchar(255) NULL,
        [generate_qr] bit NULL,
        [grade] varchar(255) NULL,
        [item_description] varchar(255) NULL,
        [line_number] varchar(255) NULL,
        [next_process] varchar(255) NULL,
        [qr_code] text NULL,
        [required_quantity_kg] numeric(38,2) NULL,
        [required_quantity_no] int NULL,
        [retrieval_entries_json] text NULL,
        [returnable_quantity_kg] numeric(38,2) NULL,
        [returnable_quantity_no] int NULL,
        [so_number] varchar(255) NULL,
        [temper] varchar(255) NULL,
        [unit] varchar(255) NULL,
        [weighment_quantity_kg] numeric(38,2) NULL,
        [weighment_quantity_no] int NULL,
        [scrap_quantity_kg] numeric(38,2) NULL,
        [scrap_quantity_no] int NULL,
        CONSTRAINT [PK_warehouse_stock_transfer_scheduler] PRIMARY KEY ([id])
    );
END
GO

-- ============================================================
-- Foreign Key Constraints
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK28revka8o2ah58ya5c1kfxp3f')
    ALTER TABLE [address_details] ADD CONSTRAINT [FK28revka8o2ah58ya5c1kfxp3f] FOREIGN KEY ([supplier_id]) REFERENCES [supplier_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKocpkgjvm7s6nq2nk8f3y103t9')
    ALTER TABLE [bank_details] ADD CONSTRAINT [FKocpkgjvm7s6nq2nk8f3y103t9] FOREIGN KEY ([supplier_id]) REFERENCES [supplier_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKjhb4xdxpmbcpme2uhcp7h9vyx')
    ALTER TABLE [blocked_product] ADD CONSTRAINT [FKjhb4xdxpmbcpme2uhcp7h9vyx] FOREIGN KEY ([blocked_quantity_id]) REFERENCES [blocked_quantity] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKhei1ssd96cdhasd0p1rfbfvrf')
    ALTER TABLE [coc_line_items] ADD CONSTRAINT [FKhei1ssd96cdhasd0p1rfbfvrf] FOREIGN KEY ([coc_id]) REFERENCES [certificate_of_confidence] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKkg6uar0on2jlvb3lp4f0967n4')
    ALTER TABLE [contact_details] ADD CONSTRAINT [FKkg6uar0on2jlvb3lp4f0967n4] FOREIGN KEY ([supplier_id]) REFERENCES [supplier_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKo8gklkbu5e57yo2ihrvhwsnd5')
    ALTER TABLE [customer_address] ADD CONSTRAINT [FKo8gklkbu5e57yo2ihrvhwsnd5] FOREIGN KEY ([customer_id]) REFERENCES [customer_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKimqs6dypmltte5ntm6135v6se')
    ALTER TABLE [customer_bank_detail] ADD CONSTRAINT [FKimqs6dypmltte5ntm6135v6se] FOREIGN KEY ([customer_id]) REFERENCES [customer_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKracduh8tnvm7xyfdy7xmcirt')
    ALTER TABLE [customer_contact] ADD CONSTRAINT [FKracduh8tnvm7xyfdy7xmcirt] FOREIGN KEY ([customer_id]) REFERENCES [customer_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKe0qq5c6b9gjed3q0wac4e6h9g')
    ALTER TABLE [cutting_machine_config] ADD CONSTRAINT [FKe0qq5c6b9gjed3q0wac4e6h9g] FOREIGN KEY ([machine_id]) REFERENCES [machine_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKo8dot0wlgfjv7kkv8yfhe2oan')
    ALTER TABLE [gate_inward_entity_dc_document_scan_urls] ADD CONSTRAINT [FKo8dot0wlgfjv7kkv8yfhe2oan] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKfjv4id8g3e63ieordawldk36p')
    ALTER TABLE [gate_inward_entity_e_way_bill_scan_urls] ADD CONSTRAINT [FKfjv4id8g3e63ieordawldk36p] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKh65rb6jjllp50ro6bfb8138pc')
    ALTER TABLE [gate_inward_entity_invoice_scan_urls] ADD CONSTRAINT [FKh65rb6jjllp50ro6bfb8138pc] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKmkg5myr3naek15qcib3bbluay')
    ALTER TABLE [gate_inward_entity_material_unloading_status_urls] ADD CONSTRAINT [FKmkg5myr3naek15qcib3bbluay] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKh5mo0efmujwsk3gsfw4bld2sp')
    ALTER TABLE [gate_inward_entity_medc_scan_urls] ADD CONSTRAINT [FKh5mo0efmujwsk3gsfw4bld2sp] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK2xhuun440rnp5xrgyxslpch8d')
    ALTER TABLE [gate_inward_entity_medci_scan_urls] ADD CONSTRAINT [FK2xhuun440rnp5xrgyxslpch8d] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKkho0ol2mp97f2qjaxnb5o8vfd')
    ALTER TABLE [gate_inward_entity_po_numbers] ADD CONSTRAINT [FKkho0ol2mp97f2qjaxnb5o8vfd] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKe9e2p2jop2ryjd7pqe3sd4x8r')
    ALTER TABLE [gate_inward_entity_test_certificate_scan_urls] ADD CONSTRAINT [FKe9e2p2jop2ryjd7pqe3sd4x8r] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKnvw2ae6ejkig4ie0eed949')
    ALTER TABLE [gate_inward_entity_test_certificates] ADD CONSTRAINT [FKnvw2ae6ejkig4ie0eed949] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKg27odagk35yrkd58xabvgkv48')
    ALTER TABLE [gate_inward_entity_vehicle_documents_scan_urls] ADD CONSTRAINT [FKg27odagk35yrkd58xabvgkv48] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK45l2vctpgxy3762jmxwu5m17m')
    ALTER TABLE [gate_inward_entity_vehicle_weighment_scan_urls] ADD CONSTRAINT [FK45l2vctpgxy3762jmxwu5m17m] FOREIGN KEY ([gate_inward_entity_id]) REFERENCES [gate_inward] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK7k86xwypcxanujdhjkh9bsyy4')
    ALTER TABLE [grn_inter_unit_items] ADD CONSTRAINT [FK7k86xwypcxanujdhjkh9bsyy4] FOREIGN KEY ([grn_inter_unit_id]) REFERENCES [grn_inter_unit] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK75lv2xfix570xhfrt1pq3ymo8')
    ALTER TABLE [grn_item] ADD CONSTRAINT [FK75lv2xfix570xhfrt1pq3ymo8] FOREIGN KEY ([grn_id]) REFERENCES [grn] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK386l2ald8br1tnu5er883du8g')
    ALTER TABLE [grn_jobwork_dc_scans] ADD CONSTRAINT [FK386l2ald8br1tnu5er883du8g] FOREIGN KEY ([grn_id]) REFERENCES [grn_jobwork] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK4phjmosf9y4vq2l4xepc63s24')
    ALTER TABLE [grn_jobwork_eway_scans] ADD CONSTRAINT [FK4phjmosf9y4vq2l4xepc63s24] FOREIGN KEY ([grn_id]) REFERENCES [grn_jobwork] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKkxxdwpa7ckr4oqjtmmpid9mg6')
    ALTER TABLE [grn_jobwork_invoice_scans] ADD CONSTRAINT [FKkxxdwpa7ckr4oqjtmmpid9mg6] FOREIGN KEY ([grn_id]) REFERENCES [grn_jobwork] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKdvjoy4iyqovg1oxycymrlaxio')
    ALTER TABLE [grn_jobwork_vehicle_scans] ADD CONSTRAINT [FKdvjoy4iyqovg1oxycymrlaxio] FOREIGN KEY ([grn_id]) REFERENCES [grn_jobwork] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK5qg6ay6j1qac5ywb0r225rskb')
    ALTER TABLE [item_enquiry_additional_charges] ADD CONSTRAINT [FK5qg6ay6j1qac5ywb0r225rskb] FOREIGN KEY ([item_enquiry_id]) REFERENCES [item_enquiry] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKru8154i9arkgmr5b596eu9852')
    ALTER TABLE [item_enquiry_moq] ADD CONSTRAINT [FKru8154i9arkgmr5b596eu9852] FOREIGN KEY ([enquiry_id]) REFERENCES [item_enquiry] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKgxt76ka4uets3ynu3hd0eprvq')
    ALTER TABLE [item_enquiry_product] ADD CONSTRAINT [FKgxt76ka4uets3ynu3hd0eprvq] FOREIGN KEY ([enquiry_id]) REFERENCES [item_enquiry] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKphn3vyl3i7dfgod2kf9rfw7ol')
    ALTER TABLE [lamination_machine_config] ADD CONSTRAINT [FKphn3vyl3i7dfgod2kf9rfw7ol] FOREIGN KEY ([machine_id]) REFERENCES [machine_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK6pgcyp0334o3cv4e15h493p0t')
    ALTER TABLE [margin_rate] ADD CONSTRAINT [FK6pgcyp0334o3cv4e15h493p0t] FOREIGN KEY ([product_margin_id]) REFERENCES [product_margin] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK9ooyxwpeibg61cc3vnq1nj50y')
    ALTER TABLE [material_request_item] ADD CONSTRAINT [FK9ooyxwpeibg61cc3vnq1nj50y] FOREIGN KEY ([header_id]) REFERENCES [material_request_header] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKo96pof3mumreakh7ydmtw4aes')
    ALTER TABLE [material_request_summary_item] ADD CONSTRAINT [FKo96pof3mumreakh7ydmtw4aes] FOREIGN KEY ([summary_header_id]) REFERENCES [material_request_summary_header] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK49l61wm7t4wvp7kbl55tm2tb3')
    ALTER TABLE [new_customer_address] ADD CONSTRAINT [FK49l61wm7t4wvp7kbl55tm2tb3] FOREIGN KEY ([customer_id]) REFERENCES [new_customer] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK3mu8ixaguxhjaeeiopyfkaxhb')
    ALTER TABLE [new_customer_bank_detail] ADD CONSTRAINT [FK3mu8ixaguxhjaeeiopyfkaxhb] FOREIGN KEY ([customer_id]) REFERENCES [new_customer] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKdy5vs9p8v2uychrf1f88f9u8m')
    ALTER TABLE [new_customer_contact] ADD CONSTRAINT [FKdy5vs9p8v2uychrf1f88f9u8m] FOREIGN KEY ([customer_id]) REFERENCES [new_customer] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKl3cbsn7yl87ofg6tu2pl27vgc')
    ALTER TABLE [new_packing_instruction] ADD CONSTRAINT [FKl3cbsn7yl87ofg6tu2pl27vgc] FOREIGN KEY ([new_sales_order_id]) REFERENCES [new_sales_order] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK7k69rf3t2jdsufojrw9bco7f2')
    ALTER TABLE [packing_batch_detail] ADD CONSTRAINT [FK7k69rf3t2jdsufojrw9bco7f2] FOREIGN KEY ([packing_submission_id]) REFERENCES [packing_submission] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK8g8yuqskygri7r8lcmuqm68gu')
    ALTER TABLE [packing_instruction] ADD CONSTRAINT [FK8g8yuqskygri7r8lcmuqm68gu] FOREIGN KEY ([new_sales_order_id]) REFERENCES [new_sales_order] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK4nb58f0qoaejjt2kyqsx0gbxs')
    ALTER TABLE [packing_instruction] ADD CONSTRAINT [FK4nb58f0qoaejjt2kyqsx0gbxs] FOREIGN KEY ([sales_order_id]) REFERENCES [sales_order] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK1jg78qtuyaw5bbb3c2w7x9k3e')
    ALTER TABLE [po_generation_item] ADD CONSTRAINT [FK1jg78qtuyaw5bbb3c2w7x9k3e] FOREIGN KEY ([po_generation_id]) REFERENCES [po_generation] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKkorsrlfctfahrt1i7fvg7e0ix')
    ALTER TABLE [po_product] ADD CONSTRAINT [FKkorsrlfctfahrt1i7fvg7e0ix] FOREIGN KEY ([po_request_id]) REFERENCES [po_request] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKy51if5jtfhcib91nrovw5mvc')
    ALTER TABLE [pogeneration_entity_terms_of_delivery] ADD CONSTRAINT [FKy51if5jtfhcib91nrovw5mvc] FOREIGN KEY ([pogeneration_entity_id]) REFERENCES [po_generation] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKlu1ad8cpxdpbq2wdn9efshs1c')
    ALTER TABLE [pomanagement_approval_item_entity] ADD CONSTRAINT [FKlu1ad8cpxdpbq2wdn9efshs1c] FOREIGN KEY ([approval_id]) REFERENCES [pomanagement_approval_entity] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKgmyih2l1jaflfqss5owq3gsj9')
    ALTER TABLE [production_entry_end_piece] ADD CONSTRAINT [FKgmyih2l1jaflfqss5owq3gsj9] FOREIGN KEY ([production_entry_id]) REFERENCES [production_entry] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK208gkli9cqtc23ymfwi143yto')
    ALTER TABLE [production_idle_time_entry] ADD CONSTRAINT [FK208gkli9cqtc23ymfwi143yto] FOREIGN KEY ([production_entry_id]) REFERENCES [production_entry] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK4fslq2sip4qwnatbu8n9e7m7t')
    ALTER TABLE [return_entry] ADD CONSTRAINT [FK4fslq2sip4qwnatbu8n9e7m7t] FOREIGN KEY ([parent_id]) REFERENCES [stock_transfer_wh_return] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKf0twpeivhmgvxk6ito52ymcdk')
    ALTER TABLE [sales_order_item] ADD CONSTRAINT [FKf0twpeivhmgvxk6ito52ymcdk] FOREIGN KEY ([sales_order_id]) REFERENCES [new_sales_order] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK1lxcnttw9odc2qja8gs6qykpo')
    ALTER TABLE [sales_order_line_item] ADD CONSTRAINT [FK1lxcnttw9odc2qja8gs6qykpo] FOREIGN KEY ([sales_order_id]) REFERENCES [sales_order] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKplhxjl25efis47599kwubf3lq')
    ALTER TABLE [sales_order_scheduler] ADD CONSTRAINT [FKplhxjl25efis47599kwubf3lq] FOREIGN KEY ([pick_list_id]) REFERENCES [pick_list_scheduler] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKfgewe83jk3sdjmycakb4vo84m')
    ALTER TABLE [sales_order_scheduler] ADD CONSTRAINT [FKfgewe83jk3sdjmycakb4vo84m] FOREIGN KEY ([stock_transfer_id]) REFERENCES [warehouse_stock_transfer_scheduler] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK87bforgxrquol19u7lb9vjyke')
    ALTER TABLE [salesman_incentive_rates] ADD CONSTRAINT [FK87bforgxrquol19u7lb9vjyke] FOREIGN KEY ([salesman_id]) REFERENCES [salesman_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK8ir2odlymyvryy5e4nj6k7qle')
    ALTER TABLE [scheduler_packing_instruction] ADD CONSTRAINT [FK8ir2odlymyvryy5e4nj6k7qle] FOREIGN KEY ([scheduler_id]) REFERENCES [sales_order_scheduler] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKdjekkwosp4b3tui2ogboryocn')
    ALTER TABLE [so_summary_item] ADD CONSTRAINT [FKdjekkwosp4b3tui2ogboryocn] FOREIGN KEY ([summary_id]) REFERENCES [so_summary] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKh4ffpae1r798hu8egpe6kmchb')
    ALTER TABLE [so_update_item] ADD CONSTRAINT [FKh4ffpae1r798hu8egpe6kmchb] FOREIGN KEY ([so_update_id]) REFERENCES [so_update] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK2i472w3f7fwifhnu30ji7oymi')
    ALTER TABLE [so_update_packing_instruction] ADD CONSTRAINT [FK2i472w3f7fwifhnu30ji7oymi] FOREIGN KEY ([so_update_id]) REFERENCES [so_update] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKq722up4ce116mmh7b3tg7nsm2')
    ALTER TABLE [stock_summary_bundles] ADD CONSTRAINT [FKq722up4ce116mmh7b3tg7nsm2] FOREIGN KEY ([stock_summary_id]) REFERENCES [stock_summary] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKa1m1yqd7aasi48m8hnikaxlmx')
    ALTER TABLE [stock_transfer_wh_retrieval_qty_entry] ADD CONSTRAINT [FKa1m1yqd7aasi48m8hnikaxlmx] FOREIGN KEY ([warehouse_id]) REFERENCES [stock_transfer_warehouse] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKjvowuuw4bmelftu6ljetamgbs')
    ALTER TABLE [sub_contractor_address] ADD CONSTRAINT [FKjvowuuw4bmelftu6ljetamgbs] FOREIGN KEY ([sub_contractor_id]) REFERENCES [sub_contractor_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKogu0m9rl7q3irxojh9jqcxm1x')
    ALTER TABLE [sub_contractor_bank] ADD CONSTRAINT [FKogu0m9rl7q3irxojh9jqcxm1x] FOREIGN KEY ([sub_contractor_id]) REFERENCES [sub_contractor_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK6nhba0qbhqy24ww8se67vsr8c')
    ALTER TABLE [sub_contractor_contact] ADD CONSTRAINT [FK6nhba0qbhqy24ww8se67vsr8c] FOREIGN KEY ([sub_contractor_id]) REFERENCES [sub_contractor_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKq8rvnjlf4mge9ugymy9ej7yaf')
    ALTER TABLE [sub_module_access] ADD CONSTRAINT [FKq8rvnjlf4mge9ugymy9ej7yaf] FOREIGN KEY ([user_id]) REFERENCES [user_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKm5xlute6tsgql0ygt3qv79r4h')
    ALTER TABLE [unit_bank_details] ADD CONSTRAINT [FKm5xlute6tsgql0ygt3qv79r4h] FOREIGN KEY ([unit_id]) REFERENCES [unit_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKfykvr82tgtuogixygu6fptpe3')
    ALTER TABLE [unit_contact_details] ADD CONSTRAINT [FKfykvr82tgtuogixygu6fptpe3] FOREIGN KEY ([unit_id]) REFERENCES [unit_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK1x3s6ylkm645b9nia4rqxjqx6')
    ALTER TABLE [unit_master_address] ADD CONSTRAINT [FK1x3s6ylkm645b9nia4rqxjqx6] FOREIGN KEY ([unit_id]) REFERENCES [unit_master] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKsct9yunxg40qb4r7m9lrguey4')
    ALTER TABLE [warehouse_stock_retrieval] ADD CONSTRAINT [FKsct9yunxg40qb4r7m9lrguey4] FOREIGN KEY ([stock_transfer_id]) REFERENCES [warehouse_stock_transfer] ([id]);
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKhr20ptf6qklbctf6xj5iynmfe')
    ALTER TABLE [warehouse_stock_retrieval_scheduler] ADD CONSTRAINT [FKhr20ptf6qklbctf6xj5iynmfe] FOREIGN KEY ([stock_transfer_id]) REFERENCES [warehouse_stock_transfer_scheduler] ([id]);
GO

