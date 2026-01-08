-- Phase 4: Order Management - Add workflow and pricing columns to orders table
-- Migration: V1.0.4

-- Add PENDING status to orders status enum
ALTER TABLE orders MODIFY COLUMN status ENUM('DRAFT', 'PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT';

-- Add workflow audit columns
ALTER TABLE orders ADD COLUMN submitted_at TIMESTAMP NULL AFTER notes;
ALTER TABLE orders ADD COLUMN submitted_by BIGINT NULL AFTER submitted_at;
ALTER TABLE orders ADD COLUMN approved_at TIMESTAMP NULL AFTER submitted_by;
ALTER TABLE orders ADD COLUMN approved_by BIGINT NULL AFTER approved_at;
ALTER TABLE orders ADD COLUMN cancelled_at TIMESTAMP NULL AFTER approved_by;
ALTER TABLE orders ADD COLUMN cancelled_by BIGINT NULL AFTER cancelled_at;
ALTER TABLE orders ADD COLUMN cancellation_reason VARCHAR(500) NULL AFTER cancelled_by;
ALTER TABLE orders ADD COLUMN completed_at TIMESTAMP NULL AFTER cancellation_reason;
ALTER TABLE orders ADD COLUMN completed_by BIGINT NULL AFTER completed_at;

-- Add pricing breakdown columns
ALTER TABLE orders ADD COLUMN menu_subtotal DECIMAL(12, 2) DEFAULT 0.00 AFTER guest_count;
ALTER TABLE orders ADD COLUMN utility_subtotal DECIMAL(12, 2) DEFAULT 0.00 AFTER menu_subtotal;
ALTER TABLE orders ADD COLUMN discount_percent DECIMAL(5, 2) DEFAULT 0.00 AFTER total_amount;
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(12, 2) DEFAULT 0.00 AFTER discount_percent;
ALTER TABLE orders ADD COLUMN tax_percent DECIMAL(5, 2) DEFAULT 0.00 AFTER discount_amount;
ALTER TABLE orders ADD COLUMN tax_amount DECIMAL(12, 2) DEFAULT 0.00 AFTER tax_percent;
ALTER TABLE orders ADD COLUMN grand_total DECIMAL(12, 2) DEFAULT 0.00 AFTER tax_amount;

-- Add soft delete and versioning columns
ALTER TABLE orders ADD COLUMN deleted_at TIMESTAMP NULL AFTER updated_at;
ALTER TABLE orders ADD COLUMN updated_by BIGINT NULL AFTER created_by;
ALTER TABLE orders ADD COLUMN version BIGINT DEFAULT 0 AFTER deleted_at;

-- Add foreign key constraints for audit columns
ALTER TABLE orders ADD CONSTRAINT fk_orders_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_completed_by FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add indexes for common queries
CREATE INDEX idx_orders_deleted_at ON orders(deleted_at);
CREATE INDEX idx_orders_customer ON orders(customer_id);

-- Update existing orders: set grand_total = total_amount for data consistency
UPDATE orders SET grand_total = total_amount WHERE grand_total = 0 AND total_amount > 0;
UPDATE orders SET balance_amount = grand_total - advance_amount WHERE balance_amount = 0 AND grand_total > 0;
