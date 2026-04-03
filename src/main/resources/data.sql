-- Insert sample categories with IGNORE to avoid duplicates
INSERT IGNORE INTO categories (id, name, description) VALUES
(1, 'Tops', 'Shirts, T-shirts, Blouses for kids'),
(2, 'Bottoms', 'Pants, Shorts, Skirts'),
(3, 'Dresses', 'Pretty dresses for girls'),
(4, 'Outerwear', 'Jackets, Coats, Sweaters'),
(5, 'Accessories', 'Hats, Gloves, Scarves');

-- Insert sample products
INSERT IGNORE INTO products (id, name, description, price, stock, image_url, category_id) VALUES
(1, 'Cotton T-Shirt Blue', 'Soft cotton t-shirt for boys', 12.99, 100, '/images/product1.jpg', 1),
(2, 'Floral Dress Pink', 'Beautiful floral dress for girls', 29.99, 50, '/images/product2.jpg', 3),
(3, 'Denim Jeans', 'Comfortable denim jeans', 24.50, 75, '/images/product3.jpg', 2),
(4, 'Warm Winter Jacket', 'Insulated jacket for cold weather', 49.99, 30, '/images/product4.jpg', 4),
(5, 'Striped Polo Shirt', 'Classic striped polo', 15.99, 80, '/images/product5.jpg', 1),
(6, 'Plaid Skirt', 'Cute plaid skirt', 19.99, 40, '/images/product6.jpg', 2),
(7, 'Princess Dress', 'Sparkly princess dress', 39.99, 20, '/images/product7.jpg', 3),
(8, 'Hoodie with Print', 'Fun printed hoodie', 22.99, 60, '/images/product8.jpg', 4),
(9, 'Baseball Cap', 'Adjustable cap', 9.99, 150, '/images/product9.jpg', 5),
(10, 'Sunglasses', 'Kid\'s sunglasses UV protection', 14.50, 100, '/images/product10.jpg', 5),
(11, 'Graphic T-Shirt', 'Fun cartoon graphic', 11.99, 90, '/images/product11.jpg', 1),
(12, 'Cargo Pants', 'Multiple pockets', 27.99, 45, '/images/product12.jpg', 2),
(13, 'Party Dress', 'Tulle party dress', 34.99, 25, '/images/product13.jpg', 3),
(14, 'Raincoat', 'Waterproof raincoat', 32.50, 35, '/images/product14.jpg', 4),
(15, 'Knitted Beanie', 'Warm winter beanie', 8.99, 120, '/images/product15.jpg', 5),
(16, 'Sweatpants', 'Comfortable sweatpants', 18.99, 70, '/images/product16.jpg', 2),
(17, 'Button-Up Shirt', 'Formal shirt', 21.99, 55, '/images/product17.jpg', 1),
(18, 'Denim Skirt', 'Casual denim skirt', 20.99, 40, '/images/product18.jpg', 2),
(19, 'Light Jacket', 'Lightweight spring jacket', 38.99, 30, '/images/product19.jpg', 4),
(20, 'Sandals', 'Summer sandals', 16.99, 80, '/images/product20.jpg', 5);

-- Insert users (password is 'password' encoded with BCrypt)
INSERT IGNORE INTO users (id, username, password, email, role) VALUES
(1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@kidsfashion.com', 'ROLE_ADMIN'),
(2, 'john', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'john@example.com', 'ROLE_CUSTOMER'),
(3, 'jane', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'jane@example.com', 'ROLE_CUSTOMER'),
(4, 'bob', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'bob@example.com', 'ROLE_CUSTOMER');

-- Insert coupons
INSERT IGNORE INTO coupons (id, code, discount_percent, start_date, end_date) VALUES
(1, 'SAVE10', 10.00, '2025-01-01', '2025-12-31'),
(2, 'WELCOME5', 5.00, '2025-01-01', '2025-12-31'),
(3, 'SUMMER20', 20.00, '2025-06-01', '2025-08-31');

-- Insert orders
INSERT IGNORE INTO orders (id, user_id, total_price, status, created_at) VALUES
(1, 2, 42.97, 'DELIVERED', '2025-03-01 10:30:00'),
(2, 2, 29.99, 'SHIPPED', '2025-03-05 14:20:00'),
(3, 3, 89.47, 'PROCESSING', '2025-03-07 09:15:00'),
(4, 4, 24.50, 'PENDING', '2025-03-08 16:45:00');

-- Insert order items
INSERT IGNORE INTO order_items (id, order_id, product_id, quantity, price) VALUES
(1, 1, 1, 2, 12.99),
(2, 1, 3, 1, 24.50),
(3, 2, 2, 1, 29.99),
(4, 3, 5, 3, 15.99),
(5, 3, 8, 1, 22.99),
(6, 3, 15, 2, 8.99),
(7, 4, 3, 1, 24.50);