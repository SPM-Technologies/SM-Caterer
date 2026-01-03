-- ============================================
-- V1.0.3: Add Missing Audit Columns
-- Fix tables that were missed in V1.0.2
-- ============================================

-- ===================================
-- 1. RECIPE_ITEMS - Missing created_by, updated_by
-- ===================================
ALTER TABLE recipe_items ADD COLUMN created_by BIGINT;
ALTER TABLE recipe_items ADD COLUMN updated_by BIGINT;

-- ===================================
-- 2. ORDER_MENU_ITEMS - Missing created_by, updated_by, deleted_at, updated_at
-- ===================================
ALTER TABLE order_menu_items ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE order_menu_items ADD COLUMN created_by BIGINT;
ALTER TABLE order_menu_items ADD COLUMN updated_by BIGINT;
ALTER TABLE order_menu_items ADD COLUMN deleted_at TIMESTAMP NULL;

-- ===================================
-- 3. ORDER_UTILITIES - Missing created_by, updated_by, deleted_at, updated_at
-- ===================================
ALTER TABLE order_utilities ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE order_utilities ADD COLUMN created_by BIGINT;
ALTER TABLE order_utilities ADD COLUMN updated_by BIGINT;
ALTER TABLE order_utilities ADD COLUMN deleted_at TIMESTAMP NULL;

-- ===================================
-- 4. UPI_QR_CODES - Missing created_by, updated_by, updated_at
-- ===================================
ALTER TABLE upi_qr_codes ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE upi_qr_codes ADD COLUMN created_by BIGINT;
ALTER TABLE upi_qr_codes ADD COLUMN updated_by BIGINT;

-- ===================================
-- 5. PAYMENTS - Missing updated_at
-- ===================================
ALTER TABLE payments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- ===================================
-- 6. ADD INDEXES FOR NEW COLUMNS
-- ===================================
CREATE INDEX idx_recipe_items_deleted_at ON recipe_items(deleted_at);
CREATE INDEX idx_order_menu_items_deleted_at ON order_menu_items(deleted_at);
CREATE INDEX idx_order_utilities_deleted_at ON order_utilities(deleted_at);
