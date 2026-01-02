-- ============================================
-- Phase 1: Schema Updates for JPA Entities
-- Add version, soft delete, and audit columns
-- ============================================

-- ===================================
-- 1. ADD VERSION COLUMN (Optimistic Locking)
-- ===================================
ALTER TABLE tenants ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE users ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE material_groups ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE material_group_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE units ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE unit_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE materials ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE material_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE event_types ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE event_type_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE menus ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE menu_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE recipe_items ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE utilities ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE utility_translations ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE customers ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE orders ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE order_menu_items ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE order_utilities ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE payments ADD COLUMN version BIGINT DEFAULT 0;
ALTER TABLE upi_qr_codes ADD COLUMN version BIGINT DEFAULT 0;

-- ===================================
-- 2. ADD SOFT DELETE COLUMN
-- ===================================
ALTER TABLE tenants ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE material_groups ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE units ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE materials ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE event_types ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE menus ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE recipe_items ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE utilities ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE customers ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE orders ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE payments ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE upi_qr_codes ADD COLUMN deleted_at TIMESTAMP NULL;

-- ===================================
-- 3. ADD AUDIT COLUMNS (created_by, updated_by)
-- ===================================
ALTER TABLE tenants ADD COLUMN created_by BIGINT;
ALTER TABLE tenants ADD COLUMN updated_by BIGINT;

ALTER TABLE users ADD COLUMN created_by BIGINT;
ALTER TABLE users ADD COLUMN updated_by BIGINT;

ALTER TABLE material_groups ADD COLUMN created_by BIGINT;
ALTER TABLE material_groups ADD COLUMN updated_by BIGINT;

ALTER TABLE units ADD COLUMN created_by BIGINT;
ALTER TABLE units ADD COLUMN updated_by BIGINT;

ALTER TABLE materials ADD COLUMN created_by BIGINT;
ALTER TABLE materials ADD COLUMN updated_by BIGINT;

ALTER TABLE event_types ADD COLUMN created_by BIGINT;
ALTER TABLE event_types ADD COLUMN updated_by BIGINT;

ALTER TABLE menus ADD COLUMN created_by BIGINT;
ALTER TABLE menus ADD COLUMN updated_by BIGINT;

ALTER TABLE utilities ADD COLUMN created_by BIGINT;
ALTER TABLE utilities ADD COLUMN updated_by BIGINT;

ALTER TABLE customers ADD COLUMN created_by BIGINT;
ALTER TABLE customers ADD COLUMN updated_by BIGINT;

-- orders already has created_by, just add updated_by
ALTER TABLE orders ADD COLUMN updated_by BIGINT;

-- payments already has created_by, just add updated_by
ALTER TABLE payments ADD COLUMN updated_by BIGINT;

-- ===================================
-- 4. ADD INDEXES FOR SOFT DELETE QUERIES
-- ===================================
CREATE INDEX idx_tenants_deleted_at ON tenants(deleted_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_material_groups_deleted_at ON material_groups(deleted_at);
CREATE INDEX idx_units_deleted_at ON units(deleted_at);
CREATE INDEX idx_materials_deleted_at ON materials(deleted_at);
CREATE INDEX idx_event_types_deleted_at ON event_types(deleted_at);
CREATE INDEX idx_menus_deleted_at ON menus(deleted_at);
CREATE INDEX idx_utilities_deleted_at ON utilities(deleted_at);
CREATE INDEX idx_customers_deleted_at ON customers(deleted_at);
CREATE INDEX idx_orders_deleted_at ON orders(deleted_at);
CREATE INDEX idx_payments_deleted_at ON payments(deleted_at);

-- ===================================
-- 5. ADD INDEXES FOR AUDIT COLUMNS
-- ===================================
CREATE INDEX idx_tenants_created_by ON tenants(created_by);
CREATE INDEX idx_users_created_by ON users(created_by);
CREATE INDEX idx_materials_created_by ON materials(created_by);
CREATE INDEX idx_orders_created_by ON orders(created_by);
CREATE INDEX idx_payments_created_by ON payments(created_by);
