-- ============================================================
-- TRULY BARE — Admin User Creation Script
-- ============================================================
-- Run this once after first application startup.
-- The password below is BCrypt hash of: Admin@123
-- Generate your own at: https://bcrypt-generator.com/ (cost 10)
-- ============================================================

-- 1. Create the admin user
INSERT INTO users (
    full_name,
    email,
    phone,
    password,
    role,
    terms_accepted,
    is_active,
    loyalty_points,
    total_orders,
    customer_status,
    first_order_completed,
    created_at,
    updated_at
) VALUES (
    'Admin',
    'admin@trulybare.com',
    '0000000000',
    '$2a$10$XzLp1.kCjgJ3VJsqOjINz.QFiB1Hqvl0TRfhq5VKgp.2hB4ySA3ym',  -- Admin@123
    'ADMIN',
    1,
    1,
    0,
    0,
    'Platinum',
    0,
    NOW(),
    NOW()
);

-- ============================================================
-- Verify insertion
-- ============================================================
SELECT user_id, full_name, email, role, is_active
FROM users
WHERE role = 'ADMIN';


-- ============================================================
-- Optional: Add a second admin with a different password
-- Password: SecurePass@456
-- ============================================================
-- INSERT INTO users (full_name, email, phone, password, role, terms_accepted, is_active,
--                    loyalty_points, total_orders, customer_status, first_order_completed,
--                    created_at, updated_at)
-- VALUES ('Manager', 'manager@trulybare.com', '0000000001',
--         '$2a$10$...', 'ADMIN', 1, 1, 0, 0, 'Platinum', 0, NOW(), NOW());


-- ============================================================
-- SLIDES: Sample homepage banners
-- ============================================================
INSERT INTO slides (title, subtitle, image_url, button_text, button_link, is_active, display_order, created_at, updated_at)
VALUES
  ('Pure Botanical Skincare', 'Rituals rooted in nature. Results backed by science.', '/assets/images/skin care 1 .jpeg', 'Shop Skin Care', '/products/category/2', 1, 1, NOW(), NOW()),
  ('Revitalize Your Hair', 'Nourish every strand with our hair care collection.', '/assets/images/haircre 2 .jpeg', 'Shop Hair Care', '/products/category/3', 1, 2, NOW(), NOW()),
  ('Pain Relief Formulas', 'Ancient wisdom, modern relief.', '/assets/images/sanjeevi pain realif .jpeg', 'Explore Now', '/products', 1, 3, NOW(), NOW());


-- ============================================================
-- NOTE: review_status column is added automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update).
-- If you need to backfill old reviews:
-- ============================================================
-- UPDATE reviews SET review_status = 'APPROVED', is_approved = 1 WHERE is_approved = 1;
-- UPDATE reviews SET review_status = 'PENDING',  is_approved = 0 WHERE is_approved = 0;
