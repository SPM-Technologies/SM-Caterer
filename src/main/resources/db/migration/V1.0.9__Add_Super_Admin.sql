-- ============================================
-- Add Super Admin User
-- ============================================

-- Password: Pass@54321 (BCrypt hash)
INSERT INTO users (
    tenant_id, username, email, password,
    first_name, last_name, role, status, language_preference
) VALUES (
    1,
    'SM_2026_SADMIN',
    'superadmin@smtech.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqvqWJLP5C.5mYbz8gWvF0ThJ7gDa',
    'Super',
    'Admin',
    'SUPER_ADMIN',
    'ACTIVE',
    'en'
);
