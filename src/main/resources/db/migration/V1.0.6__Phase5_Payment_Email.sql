-- =====================================================
-- Phase 5: Payment & Email - Flyway Migration V1.0.6
-- =====================================================
-- File: V1.0.6__Phase5_Payment_Email.sql
-- Purpose: Add email settings to tenants, payment_number to payments, create email_logs table
-- IMPORTANT: Uses idempotent procedures to safely handle re-runs
-- =====================================================

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

-- =====================================================
-- 1. ADD FEATURE TOGGLES & SETTINGS TO TENANTS TABLE
-- =====================================================
-- Feature Toggles
CALL AddColumnIfNotExists('tenants', 'payment_enabled', 'BOOLEAN DEFAULT TRUE');
CALL AddColumnIfNotExists('tenants', 'email_enabled', 'BOOLEAN DEFAULT FALSE');

-- Email/SMTP Settings
CALL AddColumnIfNotExists('tenants', 'smtp_host', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_port', 'INT NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_username', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_password', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_from_email', 'VARCHAR(255) NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_from_name', 'VARCHAR(100) NULL');
CALL AddColumnIfNotExists('tenants', 'smtp_use_tls', 'BOOLEAN DEFAULT TRUE');

-- UPI/Payment Settings
CALL AddColumnIfNotExists('tenants', 'default_upi_id', 'VARCHAR(100) NULL');
CALL AddColumnIfNotExists('tenants', 'upi_payee_name', 'VARCHAR(200) NULL');

-- =====================================================
-- 2. ADD PAYMENT_NUMBER TO PAYMENTS TABLE
-- =====================================================
CALL AddColumnIfNotExists('payments', 'payment_number', 'VARCHAR(50) NULL');
CALL AddColumnIfNotExists('payments', 'receipt_path', 'VARCHAR(500) NULL');
CALL AddColumnIfNotExists('payments', 'email_sent', 'BOOLEAN DEFAULT FALSE');

CALL AddIndexIfNotExists('payments', 'idx_payments_payment_number', 'payment_number');

-- =====================================================
-- 3. CREATE EMAIL_LOGS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    to_name VARCHAR(200) NULL,
    subject VARCHAR(500) NOT NULL,
    body LONGTEXT NULL,
    reference_id BIGINT NULL,
    reference_type VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    error_message TEXT NULL,
    retry_count INT DEFAULT 0,
    attachment_path VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    deleted_at TIMESTAMP NULL,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_email_logs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_email_logs_tenant_id (tenant_id),
    INDEX idx_email_logs_status (status),
    INDEX idx_email_logs_sent_at (sent_at),
    INDEX idx_email_logs_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. CLEANUP HELPER PROCEDURES
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;
DROP PROCEDURE IF EXISTS AddIndexIfNotExists;

-- =====================================================
-- END OF MIGRATION V1.0.6
-- =====================================================
