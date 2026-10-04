-- ============================================================
-- CircularCampusMarketplace - Database Schema
-- MySQL 8.4 LTS
-- Run this once to create the database and tables.
-- Then run sample-data.sql to load demo data.
-- ============================================================

DROP DATABASE IF EXISTS circular_campus_marketplace;
CREATE DATABASE circular_campus_marketplace
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE circular_campus_marketplace;

-- ------------------------------------------------------------
-- students
-- Cart is session-based (HttpSession), not persisted here.
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id            INT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100)   NOT NULL,
    email                 VARCHAR(150)   NOT NULL,
    password_hash         VARCHAR(255)   NOT NULL,
    wallet_balance         DECIMAL(10,2)  NOT NULL DEFAULT 500.00,
    sustainability_points INT            NOT NULL DEFAULT 0,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT chk_wallet_nonneg CHECK (wallet_balance >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- listings
-- ------------------------------------------------------------
CREATE TABLE listings (
    listing_id       INT AUTO_INCREMENT PRIMARY KEY,
    seller_id        INT NOT NULL,
    title            VARCHAR(150)   NOT NULL,
    description      TEXT,
    category         ENUM('Books','Electronics','Hostel Essentials','Furniture','Clothing','Other') NOT NULL,
    price            DECIMAL(10,2)  NOT NULL,
    item_condition   ENUM('New','Like New','Good','Fair') NOT NULL,
    status           ENUM('AVAILABLE','SOLD','REMOVED') NOT NULL DEFAULT 'AVAILABLE',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT chk_price_positive CHECK (price > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_status   ON listings(status);
CREATE INDEX idx_listings_seller   ON listings(seller_id);
CREATE INDEX idx_listings_price    ON listings(price);

-- ------------------------------------------------------------
-- transactions
-- One row per listing sold. amount is the authoritative price
-- captured at time of purchase (never trust client-side price).
-- ------------------------------------------------------------
CREATE TABLE transactions (
    txn_id      INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id    INT NOT NULL,
    seller_id   INT NOT NULL,
    listing_id  INT NOT NULL,
    amount      DECIMAL(10,2) NOT NULL,
    txn_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      ENUM('COMPLETED') NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT fk_txn_buyer    FOREIGN KEY (buyer_id)   REFERENCES students(student_id),
    CONSTRAINT fk_txn_seller   FOREIGN KEY (seller_id)  REFERENCES students(student_id),
    CONSTRAINT fk_txn_listing  FOREIGN KEY (listing_id) REFERENCES listings(listing_id),
    CONSTRAINT chk_txn_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_txn_buyer  ON transactions(buyer_id);
CREATE INDEX idx_txn_seller ON transactions(seller_id);
CREATE INDEX idx_txn_listing ON transactions(listing_id);
