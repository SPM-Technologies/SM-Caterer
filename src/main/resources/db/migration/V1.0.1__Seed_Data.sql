-- ============================================
-- CloudCaters Seed Data v1.0.1
-- Minimal data for testing database setup
-- ============================================

-- 1. INSERT TEST TENANT
INSERT INTO tenants (
    tenant_code, business_name, contact_person,
    email, phone, status,
    subscription_start_date, subscription_end_date
) VALUES (
    'TEST001',
    'Test Caterers',
    'Test Manager',
    'test@smtech.com',
    '9999999999',
    'ACTIVE',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
);

-- 2. INSERT TEST USER
-- Password: test123 (BCrypt hash)
INSERT INTO users (
    tenant_id, username, email, password,
    first_name, last_name, role, status, language_preference
) VALUES (
    1,
    'testuser',
    'testuser@smtech.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO',
    'Test',
    'User',
    'TENANT_ADMIN',
    'ACTIVE',
    'en'
);
