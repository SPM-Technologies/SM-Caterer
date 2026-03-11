-- ============================================
-- Add test users for MANAGER, STAFF, VIEWER roles
-- Password for all: test123
-- BCrypt hash: $2a$10$OTOyU6dX9mdKx6w8YfwMf.ZYmqNNZnf5tr..A/k1bId1zjL7sPQ1y
-- ============================================

-- MANAGER user
INSERT INTO users (
    tenant_id, username, email, password,
    first_name, last_name, role, status, language_preference
) VALUES (
    1,
    'testmanager',
    'manager@smtech.com',
    '$2a$10$OTOyU6dX9mdKx6w8YfwMf.ZYmqNNZnf5tr..A/k1bId1zjL7sPQ1y',
    'Test',
    'Manager',
    'MANAGER',
    'ACTIVE',
    'en'
);

-- STAFF user
INSERT INTO users (
    tenant_id, username, email, password,
    first_name, last_name, role, status, language_preference
) VALUES (
    1,
    'teststaff',
    'staff@smtech.com',
    '$2a$10$OTOyU6dX9mdKx6w8YfwMf.ZYmqNNZnf5tr..A/k1bId1zjL7sPQ1y',
    'Test',
    'Staff',
    'STAFF',
    'ACTIVE',
    'en'
);

-- VIEWER user
INSERT INTO users (
    tenant_id, username, email, password,
    first_name, last_name, role, status, language_preference
) VALUES (
    1,
    'testviewer',
    'viewer@smtech.com',
    '$2a$10$OTOyU6dX9mdKx6w8YfwMf.ZYmqNNZnf5tr..A/k1bId1zjL7sPQ1y',
    'Test',
    'Viewer',
    'VIEWER',
    'ACTIVE',
    'en'
);
