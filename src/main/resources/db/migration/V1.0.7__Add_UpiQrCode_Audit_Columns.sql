-- =====================================================
-- Phase 5: Fix - Add Audit Columns to UPI_QR_CODES
-- =====================================================
-- File: V1.0.7__Add_UpiQrCode_Audit_Columns.sql
-- Purpose: Add audit columns to upi_qr_codes table (TenantBaseEntity refactor)
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

-- =====================================================
-- ADD AUDIT COLUMNS TO UPI_QR_CODES TABLE
-- =====================================================
-- UpiQrCode now extends TenantBaseEntity, needs audit columns
CALL AddColumnIfNotExists('upi_qr_codes', 'created_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists('upi_qr_codes', 'updated_at', 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL AddColumnIfNotExists('upi_qr_codes', 'deleted_at', 'TIMESTAMP NULL');
CALL AddColumnIfNotExists('upi_qr_codes', 'version', 'BIGINT DEFAULT 0');
CALL AddColumnIfNotExists('upi_qr_codes', 'created_by', 'BIGINT NULL');
CALL AddColumnIfNotExists('upi_qr_codes', 'updated_by', 'BIGINT NULL');

-- =====================================================
-- CLEANUP HELPER PROCEDURES
-- =====================================================
DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

-- =====================================================
-- END OF MIGRATION V1.0.7
-- =====================================================
