-- Truly Bare - Complete Database Initialization Script
-- Generated for MySQL

DROP DATABASE IF EXISTS truly_bare;
CREATE DATABASE truly_bare;
USE truly_bare;

-- Disable foreign key checks to allow clean table recreation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS return_items;
DROP TABLE IF EXISTS returns;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS slides;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Users Table
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    is_active BIT(1) DEFAULT 1,
    terms_accepted BIT(1) DEFAULT 0,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,
    customer_status VARCHAR(50) DEFAULT 'Silver',
    loyalty_points INT DEFAULT 0,
    total_orders INT DEFAULT 0,
    first_order_completed BIT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Categories Table
CREATE TABLE categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    image_url VARCHAR(255),
    is_active BIT(1) DEFAULT 1,
    parent_category_id BIGINT,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- 3. Products Table
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    subtitle VARCHAR(200),
    description TEXT,
    ingredients TEXT,
    benefits TEXT,
    usage_instructions TEXT,
    warnings TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 100,
    category_id BIGINT,
    image_url LONGTEXT,
    is_featured BIT(1) DEFAULT 0,
    is_best_seller BIT(1) DEFAULT 0,
    is_new BIT(1) DEFAULT 0,
    is_classic BIT(1) DEFAULT 0,
    is_active BIT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- 4. Addresses Table
CREATE TABLE addresses (
    address_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    is_default BIT(1) DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Carts and Items
CREATE TABLE carts (
    cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    session_id VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- 6. Orders and Items
CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2),
    shipping_fees DECIMAL(10,2),
    discount_amount DECIMAL(10,2) DEFAULT 0,
    points_used INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_status VARCHAR(50) DEFAULT 'PENDING',
    shipping_address TEXT NOT NULL,
    cancel_reason TEXT,
    return_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE order_items (
    order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 7. Payments
CREATE TABLE payments (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    razorpay_signature VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- 8. Reviews
CREATE TABLE reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_verified_purchase BIT(1) DEFAULT 0,
    is_approved BIT(1) DEFAULT 0,
    review_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 9. Returns
CREATE TABLE returns (
    return_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    return_reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    admin_remarks TEXT,
    refund_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE return_items (
    return_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT,
    reason VARCHAR(255),
    FOREIGN KEY (return_id) REFERENCES returns(return_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 10. Slides / Banners Table
CREATE TABLE slides (
    slide_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    image_url VARCHAR(255) NOT NULL,
    button_text VARCHAR(100),
    button_link VARCHAR(255),
    is_active BIT(1) DEFAULT 1,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed Slides
INSERT INTO slides (title, subtitle, image_url, button_text, button_link, is_active, display_order) VALUES
('Pure Botanical Skincare', 'Rituals rooted in nature. Results backed by science.', '/assets/images/skin care 1 .jpeg', 'Shop Skin Care', '/products/category/2', 1, 1),
('Revitalize Your Hair', 'Nourish every strand with our hair care collection.', '/assets/images/haircre 2 .jpeg', 'Shop Hair Care', '/products/category/3', 1, 2),
('Pain Relief Formulas', 'Ancient wisdom, modern relief.', '/assets/images/sanjeevi pain realif .jpeg', 'Explore Now', '/products', 1, 3);

-- ---------------------------------------------------------
-- DUMPING DATA
-- ---------------------------------------------------------

-- Users (Password: password123 [BCrypt])
INSERT INTO users (user_id, full_name, email, phone, password, role, is_active, terms_accepted, created_at) VALUES
(1, 'Manual Test', 'manual@test.com', '9876543210', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'USER', 1, 1, NOW()),
(2, 'Truly Bare Admin', 'Trulybare@admin', '0000000000', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'ADMIN', 1, 1, NOW()),
(3, 'Nisha', 'vegambymuttikulangarai@gmail.com', '8012954109', '$2a$10$sIni/wsts/EblY1Uh8byfOe5ll4AxI0iTDgj0g.fmN.Ma4SSjkBZS', 'USER', 1, 1, NOW()),
(4, 'Isabella Thorne', 'user1@gmail.com', '9988776655', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'USER', 1, 1, '2026-04-10 10:00:00'),
(5, 'Alternative Admin', 'admin@trulybare.com', '1111111111', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'ADMIN', 1, 1, NOW()),
(6, 'SIVASURYA S', 'sivasurya.official.8@gmail.com', '9344075887', '$2a$10$aygqtdXJdekVNdq4cZLhquT7.dA8UV/IQemN7Ru2WsfRcuNmlt1Ru', 'ADMIN', 1, 1, NOW());

-- Categories
INSERT INTO categories (category_id, name, description, parent_category_id) VALUES
(8, 'HOUSE OF VEGAM', 'Traditional Ayurvedic and clinical formulations from the House of Vegam Pain Relief Clinic.', NULL),
(5, 'PAIN RELIEF', 'Ancient wisdom meeting modern relief for chronic and acute pain management.', 8),
(9, 'TRULY BARE', 'Luxury botanical wellness and aesthetics formulations for the discerning individual.', NULL),
(1, 'SUPPLEMENTS', 'Precision-crafted formulas for strength, nerve support and metabolic balance.', 9),
(2, 'WEIGHT LOSS', 'Botanical blends for metabolic balance and a lighter, more energised you.', 9),
(3, 'SKN CARE', 'Pure, effective rituals designed to nourish the skin barrier and restore innate radiance using high-potency botanicals.', 9),
(4, 'HAIR CARE', 'Advanced scalp and strand therapies that bridge traditional wisdom with cellular science for unparalleled hair health.', 9);

-- Products
INSERT INTO products (product_id, name, category_id, description, subtitle, price, stock_quantity) VALUES
(1, 'Sanjeevi Oil', 5, 'Traditional healing oil for internal balance. (100ml)', 'Ancient Healing Ritual', 899.00, 30),
(2, 'Unleash Gel', 5, 'Fast-acting topical formulation for pain management. (50grams)', 'Instant Recovery Ritual', 1299.00, 30),
(3, 'Hair Oil', 5, 'Scalp nourishing herbal hair oil for strength and growth. (100ml)', 'Clinical Hair Health', 699.00, 30),
(4, 'Neuro Flo', 5, 'Circulation support for localized relief. (18ml)', 'Neurological Relief', 1499.00, 30),
(5, 'Joint Strength', 1, 'Advanced support for joint mobility and comfort. (30 tablets)', 'Mobility & Resilience', 2499.00, 30),
(6, 'Muscle Strength', 1, 'Premium protein formulation for optimal muscle recovery. (30 tablets)', 'Power & Recovery', 3299.00, 30),
(7, 'Nerve Support', 1, 'Specialized nutrients for healthy neurological function. (30 tablets)', 'Neurological Vitality', 2799.00, 30),
(8, 'Fitblend', 2, 'Scientific formulation for metabolic balance and weight management. (200grams)', 'Metabolic Ritual', 1999.00, 30),
(9, 'Face Wash (SKN CARE NO:1)', 3, 'Deeply purifying botanical cleanser. (100grams)', 'Purifying Ritual', 399.00, 30),
(10, 'Serum (SKN CARE NO:2)', 3, 'Intensive radiance boosting treatment.', 'Illuminating Ritual', 799.00, 30),
(11, 'Moisturizor (SKN CARE NO:3)', 3, 'Deep hydration and barrier protection.', 'Hydration Ritual', 649.00, 30),
(12, 'Sunscreen (SKN CARE NO:4)', 3, 'SPF 50 Mineral-based daily shield.', 'Protective Ritual', 549.00, 30),
(13, 'Shampoo (HAIR CARE NO:1)', 4, 'Scalp balancing and hair revitalizing. (200ML)', 'Cleansing Ritual', 499.00, 30),
(14, 'Cream (HAIR CARE NO:2)', 4, 'Smoothing and nourishing hair cream.', 'Nourishing Ritual', 599.00, 30),
(15, 'Serum (HAIR CARE NO:3)', 4, 'Gloss boosting and damage repair serum.', 'Revitalizing Ritual', 699.00, 30),
(16, 'Conditioner (HAIR CARE NO:4)', 4, 'Hydrating botanical conditioner.', 'Smoothing Ritual', 449.00, 30),
(17, 'Hair Mask (HAIR CARE NO:5)', 4, 'Intensive weekly repair ritual.', 'Repair Ritual', 899.00, 30);

-- Seed Dynamic Tags & Images
UPDATE products SET image_url = '/assets/images/sanjeevi pain realif .jpeg', is_featured = 1, is_best_seller = 1 WHERE name = 'Sanjeevi Oil';
UPDATE products SET image_url = '/assets/images/panin realif gell.jpeg' WHERE name = 'Unleash Gel';
UPDATE products SET image_url = '/assets/images/hair oil .jpeg' WHERE name = 'Hair Oil';
UPDATE products SET image_url = '/assets/images/pain relif.jpeg' WHERE name = 'Neuro Flo';
UPDATE products SET image_url = '/assets/images/joint strenth.jpeg', is_featured = 1 WHERE name = 'Joint Strength';
UPDATE products SET image_url = '/assets/images/muscle strength .jpeg' WHERE name = 'Muscle Strength';
UPDATE products SET image_url = '/assets/images/skin care 1 .jpeg', is_new = 1 WHERE name = 'Face Wash (SKN CARE NO:1)';
UPDATE products SET image_url = '/assets/images/haircre 2 .jpeg' WHERE name = 'Cream (HAIR CARE NO:2)';

-- Sample Address for Sivasurya
INSERT INTO addresses (user_id, full_name, phone, address_line1, city, state, pincode, is_default) VALUES
(2, 'SIVASURYA S', '9344075887', '2/78, MUKKONAM, POOLANKINAR (PO)', 'UDUMALPETTAI', 'Tamil Nadu', '642122', 1),
(6, 'SIVASURYA S', '9344075887', '2/78, MUKKONAM, POOLANKINAR (PO)', 'UDUMALPETTAI', 'Tamil Nadu', '642122', 1);

-- Sample Address for Nisha
INSERT INTO addresses (user_id, full_name, phone, address_line1, city, state, pincode, is_default) VALUES
(3, 'Nisha', '8012954109', '12/A, Lotus Garden', 'Coimbatore', 'Tamil Nadu', '641001', 1);

-- Sample Address for Isabella (User 1)
INSERT INTO addresses (user_id, full_name, phone, address_line1, city, state, pincode, is_default) VALUES
(4, 'Isabella Thorne', '9988776655', 'Bungalow 42, Marble Heights', 'Delhi', 'Delhi', '110001', 1);

-- Sample Address for Manual Test
INSERT INTO addresses (user_id, full_name, phone, address_line1, city, state, pincode, is_default) VALUES
(1, 'Manual Test', '9876543210', 'Plot 5, Industrial Estate', 'Chennai', 'Tamil Nadu', '600032', 1);

-- ---------------------------------------------------------
-- DUMMY ORDERS & REFLECTIONS FOR TESTING DASHBOARD
-- ---------------------------------------------------------

-- NISHA ORDERS
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (3, 'TB-ORD-5501', 1598.00, 'DELIVERED', 'SUCCESS', '12/A, Lotus Garden, Coimbatore, Tamil Nadu', DATE_SUB(NOW(), INTERVAL 15 DAY));
SET @nisha_o1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@nisha_o1, 10, 2, 799.00);

INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (3, 'TB-ORD-5502', 899.00, 'PENDING', 'SUCCESS', '12/A, Lotus Garden, Coimbatore, Tamil Nadu', NOW());
SET @nisha_o2 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@nisha_o2, 1, 1, 899.00);

-- ISABELLA ORDERS
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (4, 'TB-ORD-8801', 3798.00, 'SHIPPED', 'SUCCESS', 'Bungalow 42, Marble Heights, Delhi', DATE_SUB(NOW(), INTERVAL 2 DAY));
SET @isab_o1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@isab_o1, 5, 1, 2499.00), (@isab_o1, 2, 1, 1299.00);

INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (4, 'TB-ORD-8802', 499.00, 'CANCELLED', 'REFUNDED', 'Bungalow 42, Marble Heights, Delhi', DATE_SUB(NOW(), INTERVAL 20 DAY));
SET @isab_o2 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@isab_o2, 13, 1, 499.00);

-- MANUAL TEST ORDERS
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (1, 'TB-ORD-1101', 3299.00, 'DELIVERED', 'SUCCESS', 'Plot 5, Industrial Estate, Chennai, Tamil Nadu', DATE_SUB(NOW(), INTERVAL 30 DAY));
SET @man_o1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@man_o1, 6, 1, 3299.00);

-- ADMIN ORDERS (Sivasurya)
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, 'TB-ORD-2201', 899.00, 'DELIVERED', 'SUCCESS', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', DATE_SUB(NOW(), INTERVAL 5 DAY));
SET @admin_o1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (@admin_o1, 1, 1, 899.00);

-- Add Reviews
INSERT INTO reviews (user_id, product_id, order_id, rating, comment, is_verified_purchase, is_approved, created_at)
VALUES 
(3, 10, @nisha_o1, 5, 'Best hair serum I have ever used. Highly recommend!', 1, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 5, @isab_o1, 4, 'Really helped with my joint pain during workouts.', 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 6, @man_o1, 5, 'Great for recovery. The quality is top notch.', 1, 0, DATE_SUB(NOW(), INTERVAL 25 DAY));

-- Add Payments
INSERT INTO payments (order_id, amount, payment_method, status, created_at)
VALUES 
(@nisha_o1, 1598.00, 'ONLINE', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(@nisha_o2, 899.00, 'COD', 'PENDING', NOW()),
(@isab_o1, 3798.00, 'ONLINE', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(@man_o1, 3299.00, 'ONLINE', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 30 DAY));
