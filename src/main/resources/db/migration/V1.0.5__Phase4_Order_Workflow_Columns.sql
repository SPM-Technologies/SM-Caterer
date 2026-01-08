-- Phase 4: Order Management - Add workflow and pricing columns to orders table
-- Migration: V1.0.5 (replaces failed V1.0.4)
-- Note: This migration is idempotent using stored procedures
-- Columns version, deleted_at, updated_by already exist from V1.0.2

-- Helper procedure to add column if not exists
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN columnDef VARCHAR(255)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tableName
      AND column_name = columnName;
    IF col_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', columnName, ' ', columnDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- Helper procedure to add index if not exists
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
CREATE PROCEDURE AddIndexIfNotExists(
    IN tableName VARCHAR(64),
    IN indexName VARCHAR(64),
    IN columnName VARCHAR(64)
)
BEGIN
    DECLARE idx_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = tableName
      AND index_name = indexName;
    IF idx_exists = 0 THEN
        SET @ddl = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, '(', columnName, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- Helper procedure to add foreign key if not exists
DROP PROCEDURE IF EXISTS AddFKIfNotExists;
CREATE PROCEDURE AddFKIfNotExists(
    IN tableName VARCHAR(64),
    IN constraintName VARCHAR(64),
    IN columnName VARCHAR(64),
    IN refTable VARCHAR(64),
    IN refColumn VARCHAR(64)
)
BEGIN
    DECLARE fk_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO fk_exists
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = tableName
      AND constraint_name = constraintName;
    IF fk_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', tableName, ' ADD CONSTRAINT ', constraintName,
                          ' FOREIGN KEY (', columnName, ') REFERENCES ', refTable, '(', refColumn, ') ON DELETE SET NULL');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

-- Add PENDING status to orders status enum
ALTER TABLE orders MODIFY COLUMN status ENUM('DRAFT', 'PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT';

-- Add workflow audit columns
CALL AddColumnIfNotExists('orders', 'submitted_at', 'TIMESTAMP NULL');
CALL AddColumnIfNotExists('orders', 'submitted_by', 'BIGINT NULL');
CALL AddColumnIfNotExists('orders', 'approved_at', 'TIMESTAMP NULL');
CALL AddColumnIfNotExists('orders', 'approved_by', 'BIGINT NULL');
CALL AddColumnIfNotExists('orders', 'cancelled_at', 'TIMESTAMP NULL');
CALL AddColumnIfNotExists('orders', 'cancelled_by', 'BIGINT NULL');
CALL AddColumnIfNotExists('orders', 'cancellation_reason', 'VARCHAR(500) NULL');
CALL AddColumnIfNotExists('orders', 'completed_at', 'TIMESTAMP NULL');
CALL AddColumnIfNotExists('orders', 'completed_by', 'BIGINT NULL');

-- Add pricing breakdown columns
CALL AddColumnIfNotExists('orders', 'menu_subtotal', 'DECIMAL(12,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'utility_subtotal', 'DECIMAL(12,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'discount_percent', 'DECIMAL(5,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'discount_amount', 'DECIMAL(12,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'tax_percent', 'DECIMAL(5,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'tax_amount', 'DECIMAL(12,2) DEFAULT 0.00');
CALL AddColumnIfNotExists('orders', 'grand_total', 'DECIMAL(12,2) DEFAULT 0.00');

-- Add foreign key constraints
CALL AddFKIfNotExists('orders', 'fk_orders_submitted_by', 'submitted_by', 'users', 'id');
CALL AddFKIfNotExists('orders', 'fk_orders_approved_by', 'approved_by', 'users', 'id');
CALL AddFKIfNotExists('orders', 'fk_orders_cancelled_by', 'cancelled_by', 'users', 'id');
CALL AddFKIfNotExists('orders', 'fk_orders_completed_by', 'completed_by', 'users', 'id');

-- Add index for customer lookup
CALL AddIndexIfNotExists('orders', 'idx_orders_customer', 'customer_id');

-- Clean up helper procedures
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;
DROP PROCEDURE IF EXISTS AddFKIfNotExists;

-- Update existing orders for data consistency
UPDATE orders SET grand_total = total_amount WHERE (grand_total IS NULL OR grand_total = 0) AND total_amount > 0;
UPDATE orders SET balance_amount = grand_total - COALESCE(advance_amount, 0) WHERE (balance_amount IS NULL OR balance_amount = 0) AND grand_total > 0;
