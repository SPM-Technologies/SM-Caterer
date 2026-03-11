-- ============================================
-- V1.0.8: Add Tenant Branding Configuration
-- Adds logo, display name, tagline, and color
-- ============================================

-- Add branding columns to tenants table
ALTER TABLE tenants
    ADD COLUMN display_name VARCHAR(200) NULL COMMENT 'Display name for branding (shown on UI)',
    ADD COLUMN logo_path VARCHAR(500) NULL COMMENT 'Path to uploaded logo file',
    ADD COLUMN tagline VARCHAR(200) NULL COMMENT 'Company tagline/slogan',
    ADD COLUMN primary_color VARCHAR(7) NULL COMMENT 'Primary brand color in hex format';

-- Update existing tenant with default display name
UPDATE tenants SET display_name = business_name WHERE display_name IS NULL;
