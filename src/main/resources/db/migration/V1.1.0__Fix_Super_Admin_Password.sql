-- ============================================
-- Fix Super Admin password to 'test123'
-- Previous password was 'Pass@54321'
-- ============================================
UPDATE users
SET password = '$2a$10$zOzw7yvRP1CDd.uceWNJ3.TiGrBvJqnaC/foyIvBfJ3zvwQ3M6Kmy'
WHERE username = 'SM_2026_SADMIN' AND role = 'SUPER_ADMIN';
