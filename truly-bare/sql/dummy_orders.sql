USE truly_bare;

-- Determine user ID (assuming user SIVASURYA S is ID 2, if not we fall back or it inserts for whatever ID 2 is)

-- Order 1: Delivered
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, CONCAT('TB-ORD-', FLOOR(RAND() * 900000 + 100000)), 3497.00, 'DELIVERED', 'SUCCESS', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', DATE_SUB(NOW(), INTERVAL 5 DAY));
SET @order1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES 
(@order1, 1, 1, 1999.00), 
(@order1, 13, 1, 1498.00);

-- Order 2: Shipped
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, CONCAT('TB-ORD-', FLOOR(RAND() * 900000 + 100000)), 4200.00, 'SHIPPED', 'SUCCESS', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', DATE_SUB(NOW(), INTERVAL 2 DAY));
SET @order2 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES 
(@order2, 15, 1, 4200.00);

-- Order 3: Pending
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, CONCAT('TB-ORD-', FLOOR(RAND() * 900000 + 100000)), 1999.00, 'PENDING', 'SUCCESS', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', NOW());
SET @order3 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES 
(@order3, 1, 1, 1999.00);

-- Order 4: Cancelled
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, CONCAT('TB-ORD-', FLOOR(RAND() * 900000 + 100000)), 599.00, 'CANCELLED', 'REFUNDED', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', DATE_SUB(NOW(), INTERVAL 10 DAY));
SET @order4 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES 
(@order4, 3, 1, 599.00);

-- Order 5: Review Dummy
INSERT INTO orders (user_id, order_number, total_amount, status, payment_status, shipping_address, created_at) 
VALUES (2, CONCAT('TB-ORD-', FLOOR(RAND() * 900000 + 100000)), 3000.00, 'DELIVERED', 'SUCCESS', '2/78, MUKKONAM, POOLANKINAR (PO), UDUMALPETTAI', DATE_SUB(NOW(), INTERVAL 15 DAY));
SET @order5 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES 
(@order5, 21, 1, 3000.00);

-- Add a Dummy Review so the Reflections section lights up!
INSERT INTO reviews (user_id, product_id, order_id, rating, comment, is_verified_purchase, is_approved)
VALUES (2, 21, @order5, 5, 'The Reverence Body Cleanser exceeded my expectations. The aromatic botanical blend is exceptional and deeply grounding. A true luxury experience.', 1, 1);
