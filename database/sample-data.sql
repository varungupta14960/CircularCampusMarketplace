-- ============================================================
-- CircularCampusMarketplace - Sample / Demo Data
-- Run AFTER schema.sql.
--
-- DEMO PASSWORD (same for all 5 demo accounts): Password123!
-- The password_hash values below are real PBKDF2WithHmacSHA256
-- hashes (format "iterations:base64(salt):base64(hash)") generated
-- and verified against com.campusmarketplace.util.PasswordUtil
-- before being included here - they are not placeholders.
-- ============================================================

USE circular_campus_marketplace;

INSERT INTO students (name, email, password_hash, wallet_balance, sustainability_points) VALUES
('Aarav Sharma',  'student1@example.com', '120000:aZH42RtGSCW3RtpiTbm59Q==:AWeUymezPPe5oypmYV6rtfyETC714ujap6h+S1fB524=', 750.00, 20),
('Diya Patel',    'student2@example.com', '120000:HLlqLotq9coxDBThofbeFw==:IGjX2BC1QPP1TAqn55B7r4dXUwJZVrwz+5JyZxl6a9Y=', 300.00, 10),
('Rohan Mehta',   'student3@example.com', '120000:nw6Hwf9RM49i+b6MwB5bRw==:Y6qvjCiIfTNIO720jaiPyPqnZ/oOpOBCfQjUyGZjxQs=',  50.00,  0),
('Isha Verma',    'student4@example.com', '120000:aZH42RtGSCW3RtpiTbm59Q==:AWeUymezPPe5oypmYV6rtfyETC714ujap6h+S1fB524=', 500.00, 10),
('Kabir Nair',    'student5@example.com', '120000:HLlqLotq9coxDBThofbeFw==:IGjX2BC1QPP1TAqn55B7r4dXUwJZVrwz+5JyZxl6a9Y=', 500.00,  0);

-- Listings (mix of categories, conditions, sellers, statuses)
INSERT INTO listings (seller_id, title, description, category, price, item_condition, status) VALUES
(1, 'Data Structures Textbook (Cormen)', 'Introduction to Algorithms, 3rd edition. Some highlighting inside.', 'Books', 650.00, 'Good', 'AVAILABLE'),
(1, 'Scientific Calculator - Casio fx-991ES', 'Barely used, works perfectly, includes cover.', 'Electronics', 450.00, 'Like New', 'AVAILABLE'),
(2, 'Study Table Lamp', 'LED desk lamp with adjustable brightness.', 'Hostel Essentials', 300.00, 'Good', 'AVAILABLE'),
(2, 'Single Bed Mattress', 'Used one semester, no stains, foldable foam mattress.', 'Furniture', 900.00, 'Fair', 'AVAILABLE'),
(3, 'Winter Hoodie - Size M', 'Warm fleece hoodie, worn a handful of times.', 'Clothing', 350.00, 'Good', 'AVAILABLE'),
(3, 'Wired Headphones', 'Basic wired headphones, good sound quality.', 'Electronics', 150.00, 'Fair', 'AVAILABLE'),
(4, 'Engineering Drawing Kit', 'Complete drafting kit with compass, scales, set squares.', 'Books', 200.00, 'Good', 'AVAILABLE'),
(4, 'Mini Refrigerator', 'Compact hostel-room fridge, 45L, works great.', 'Hostel Essentials', 3500.00, 'Good', 'AVAILABLE'),
(5, 'Bookshelf - 3 tier', 'Wooden bookshelf, slightly wobbly leg but sturdy.', 'Furniture', 500.00, 'Fair', 'AVAILABLE'),
(1, 'Old Physics Notes (Sem 1-2)', 'Handwritten notes, spiral bound, useful for revision.', 'Books', 80.00, 'Good', 'AVAILABLE'),
-- Already-sold example (for testing history / SOLD-listing purchase prevention)
(2, 'Bluetooth Speaker - JBL Clone', 'Portable speaker, good bass.', 'Electronics', 600.00, 'Good', 'SOLD'),
-- Removed example (for testing REMOVED-listing handling)
(3, 'Old Cricket Bat', 'Willow bat, decent condition.', 'Other', 250.00, 'Fair', 'REMOVED');

-- A transaction record matching the SOLD listing above (listing_id 11: buyer=3, seller=2)
INSERT INTO transactions (buyer_id, seller_id, listing_id, amount, status) VALUES
(3, 2, 11, 600.00, 'COMPLETED');
