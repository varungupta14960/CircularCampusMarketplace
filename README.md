# CircularCampusMarketplace

**PBL 2 — Circular Campus Marketplace: Resale, Cart & Wallet**

### CampusCycle — *"Give Your Stuff a Second Life."*

A peer-to-peer campus resale/thrift marketplace built with a traditional **Java Servlet + JSP + JDBC + MySQL** stack — no Spring, no Hibernate, no frontend frameworks.

---

## 1. Problem Statement

College students regularly accumulate books, gadgets, hostel essentials, furniture and clothing they no longer need, while other students on the same campus are actively looking to buy exactly those things secondhand. There's no lightweight, campus-specific way to connect the two sides. CampusCycle is a self-contained web marketplace where students list items, browse/search what others are selling, and pay for purchases using an in-app wallet — demonstrating a complete, realistic e-commerce flow using only core Java EE/Jakarta EE building blocks.

## 2. Features

- Student registration, login, logout
- Create / edit / remove listings (6 categories, 4 conditions)
- Guest and logged-in browsing, with server-side keyword + category + min/max price search
- Guest "recently viewed" and guest cart via **Cookies**
- Logged-in cart via **HttpSession**
- Guest-cart-to-session-cart migration on login
- Wallet balance, atomic checkout, buyer debit / seller credit
- Sustainability points (+10 buyer, +10 seller per completed sale)
- Buyer/seller transaction history
- Server-side ownership enforcement (can't edit/delete someone else's listing)
- Friendly 403/404/500 error pages

## 3. Learning Objectives Demonstrated

| Objective | Where |
|---|---|
| Cookies vs. `HttpSession` | `CookieUtil` + `CartServlet` (guest cart = Cookie, logged-in cart = Session), `recentlyViewed` cookie |
| Session creation / invalidation | `LoginServlet` (`session.setAttribute`), `LogoutServlet` (`session.invalidate()`) |
| Guest → logged-in cart migration | `LoginServlet.migrateGuestCartToSession()` |
| JDBC / MySQL persistence | All `dao/` classes, exclusively `PreparedStatement` |
| Cart-to-order conversion | `CartServlet` (session cart) → `CheckoutServlet` → `TransactionDAO.performCheckout()` |
| Concurrent HTTP requests | `TransactionDAO` uses `SELECT ... FOR UPDATE` row locking with a fixed lock order, so two simultaneous purchase attempts on the same listing cannot both succeed (verified — see §15) |

## 4. Tech Stack

- Java 17
- Apache Tomcat 10.1.x
- Jakarta Servlet 6.0 / Jakarta Pages (JSP) 3.1 / Jakarta-compatible JSTL
- MySQL 8.x, MySQL Connector/J **9.3.0** (the requested 26.7.0 does not exist on Maven Central — verified; 9.3.0 is the real current version used instead)
- Maven (WAR packaging)
- Bootstrap 5.3.x, vanilla JavaScript, plain JDBC

No Spring, Hibernate, JPA, React/Angular/Vue, Node/PHP/Python backends, MongoDB/PostgreSQL, Docker, or other frameworks are used anywhere in this project.

## 5. Project Structure

```
CircularCampusMarketplace/
├── pom.xml
├── README.md
├── .gitignore
├── database/
│   ├── schema.sql
│   └── sample-data.sql
└── src/main/
    ├── java/com/campusmarketplace/
    │   ├── model/    Student.java, Listing.java, Transaction.java
    │   ├── dao/      StudentDAO.java, ListingDAO.java, TransactionDAO.java
    │   ├── servlet/  14 servlets (Home, Register, Login, Logout, Marketplace,
    │   │             ListingDetails, CreateListing, EditListing, DeleteListing,
    │   │             MyListings, Cart, Checkout, TransactionHistory, Profile)
    │   ├── filter/   AuthenticationFilter.java
    │   └── util/     DBConnection.java, PasswordUtil.java, CookieUtil.java, AppConstants.java
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml             (session config + error pages only; servlets/filter use annotations)
        │   └── views/              all JSPs (forward-only, not directly browsable)
        │       └── common/         header.jsp, alerts.jsp, footer.jsp
        ├── css/style.css
        └── js/app.js
```

## 6. Database Setup

1. Install MySQL 8.x and start the server.
2. Run the two SQL files in order, from a clean/any state (the schema script drops and recreates the database):
   ```
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample-data.sql
   ```

## 7. Configuration

`com.campusmarketplace.util.DBConnection` reads `DB_URL` / `DB_USER` / `DB_PASSWORD` environment variables first; if unset, it falls back to local-dev placeholder constants in the same file (`root` / `root`, MySQL's own well-known local default — **not** a real deployed secret).

```
export DB_URL="jdbc:mysql://localhost:3306/circular_campus_marketplace?useSSL=false&serverTimezone=UTC"
export DB_USER=root
export DB_PASSWORD=yourpassword
```

For anything beyond local grading/dev, set these environment variables rather than editing the source — see **Security Notes** (§14).

## 8. Maven Build

```
mvn clean package
```

Produces `target/CircularCampusMarketplace.war`.

## 9. Tomcat Deployment

1. `cp target/CircularCampusMarketplace.war <TOMCAT_HOME>/webapps/`
2. Start Tomcat: `<TOMCAT_HOME>/bin/startup.sh` (Windows: `startup.bat`)
3. Open: **http://localhost:8080/CircularCampusMarketplace/**
4. Stop Tomcat: `<TOMCAT_HOME>/bin/shutdown.sh` (Windows: `shutdown.bat`)
5. Logs: `<TOMCAT_HOME>/logs/catalina.out`. Deployment failures are almost always (a) MySQL not running / wrong credentials, or (b) `mysql-connector-j` missing from `WEB-INF/lib` in the WAR.

This project has been run successfully on **Apache Tomcat 10.1.36 + Java 17 + MySQL 8.4.x** in Eclipse, with the homepage loading at the URL above.

## 10. Demo Credentials

All 5 demo accounts share the password **`Password123!`**

| Email | Wallet | Points |
|---|---|---|
| student1@example.com | ₹750.00 | 20 |
| student2@example.com | ₹300.00 | 10 |
| student3@example.com | ₹50.00 | 0 |
| student4@example.com | ₹500.00 | 10 |
| student5@example.com | ₹500.00 | 0 |

These are demo-only accounts seeded by `sample-data.sql`; the stored values are real PBKDF2 **hashes**, not plaintext passwords, so they're safe to publish.

## 11. Main Application Workflow

```
Guest:  Home → Marketplace → Search/Filter → Listing Details (recentlyViewed
        cookie updates) → Add to Cart (guestCart cookie) → Login
Login:  guestCart cookie read → validated against DB → merged into
        HttpSession cart → cookie cleared
Seller: Login → Create Listing → appears in Marketplace → Edit/Remove →
        My Listings
Buyer:  Browse → Add to Cart (session) → Checkout → wallet verified →
        buyer debited, seller credited → listing SOLD → transaction row
        created → sustainability points awarded → cart cleared →
        Transaction History updated
Logout: HttpSession invalidated → back to guest
```

## 12. Cookies vs. Sessions

- **Cookies** (`recentlyViewed`, `guestCart`) store only listing IDs, on the client, for **unauthenticated guests**. They're bounded in size, deduplicated, `HttpOnly`, and never trusted blindly — every ID is re-validated against the database before use. Nothing sensitive (price, wallet, password) is ever stored in a cookie.
- **`HttpSession`** stores the authenticated student object and the logged-in cart, server-side, for the duration of the login. It's created on login and destroyed on logout (`session.invalidate()`).
- The handoff between the two — reading the `guestCart` cookie, validating every ID against the DB, merging into the session cart, then clearing the cookie — happens once, in `LoginServlet`, to keep that boundary explicit and in one place.

## 13. Checkout / Transaction Handling

`TransactionDAO.performCheckout()` runs as a single JDBC transaction (`setAutoCommit(false)`):

1. Locks every cart listing row with `SELECT ... FOR UPDATE`, in ascending `listing_id` order (fixed lock order avoids deadlocks between two concurrent checkouts).
2. Re-validates each listing is `AVAILABLE` and not self-owned, using **only** the database's price — never a client-supplied value.
3. Locks the buyer's wallet row, then every distinct seller's wallet row (ascending `student_id` order), and verifies sufficient balance.
4. Deducts the buyer, credits each seller, marks listings `SOLD`, inserts one `transactions` row per item, and awards sustainability points — all inside the same transaction.
5. Commits only if every step succeeds; any failure rolls back the **entire** transaction, so no partial checkout can ever be persisted.

## 14. Security Notes

- Passwords are never stored in plaintext — `PasswordUtil` uses PBKDF2WithHmacSHA256, 120,000 iterations, a random per-user salt (standard JDK `javax.crypto`, no extra framework).
- All SQL uses `PreparedStatement`; no query is ever built by string-concatenating user input.
- Ownership (editing/deleting a listing) is enforced **twice**: once in the servlet before rendering/acting, and again in the DAO's `WHERE seller_id = ?` clause, so a tampered request ID still can't modify someone else's data.
- Checkout re-derives price, availability, and seller identity from the database every time — client input is never trusted for money calculations.
- No secrets, API keys, or real credentials are committed to this repository. The one hardcoded DB credential in source (`root`/`root`) is MySQL's own standard local-dev default, intended only to let the project run out of the box for grading; see §7 for overriding it via environment variables.
- This is an academic project: there is no CSRF token, no rate limiting, and no HTTPS enforcement. These would be required for a production deployment but were out of scope for this brief.

## 15. Testing / Verification

The following were **actually executed**, not just read through:

1. **Compilation:** the full Java source tree was compiled with a real JDK against the exact Jakarta Servlet 6.0 method signatures the code calls — zero errors.
2. **Database:** `schema.sql` and `sample-data.sql` were loaded into a real, freshly-created MySQL 8.0 database — all foreign keys, `CHECK` constraints, and `ENUM` values validated with zero errors.
3. **DAO/business logic:** the real, unmodified `StudentDAO`, `ListingDAO`, `TransactionDAO`, and `PasswordUtil` classes were run against that live database. 42 functional assertions passed, covering login/password verification, registration, search/filter, ownership enforcement at the SQL layer, a full multi-item checkout (correct total, wallet debit/credit, sustainability points, SOLD status, transaction rows), and every failure path (re-buying a sold item, buying your own listing, insufficient balance, empty cart, nonexistent listing) with zero state change on failure.
4. **Concurrency:** two threads were made to race to buy the *same* listing simultaneously against the live database, repeated multiple times. Every run: exactly one buyer succeeded, exactly one wallet was charged — confirming the `SELECT ... FOR UPDATE` locking holds under real concurrent load.
5. **Static cross-checks:** every `@WebServlet` path vs. every JSP link/form action, every `getParameter()` vs. form field name, every `setAttribute()` vs. JSP EL variable, every DAO SQL column vs. `schema.sql`, and `<div>`/`<form>`/`<table>`/`c:choose` tag balance across all composed pages — all verified consistent.

**Not run** in this environment: `mvn clean package` itself (Maven Central is not reachable from the verification sandbox) and the real Tomcat 10.1 container (though the project has separately been confirmed by the author to run successfully on Tomcat 10.1.36 + Java 17 + MySQL 8.4.x in Eclipse).

## 16. Known Limitations

- No pagination on the marketplace grid (fine at this dataset size; would need `LIMIT`/`OFFSET` for a larger catalog).
- No image upload for listings (text-only, as no file-storage requirement was specified).
- No password reset / email verification flow.
- `DBConnection` opens a new connection per call rather than using a pooled `DataSource` — fine for a college project's traffic; a production deployment would add connection pooling.
- No CSRF protection, rate limiting, or HTTPS enforcement (see §14).

## 17. Future Improvements

- Listing photos (would need file storage or an object-storage integration).
- Pagination and sorting on the marketplace grid.
- Ratings/reviews for sellers after a completed transaction.
- Email notifications on sale/purchase.
- Connection pooling (HikariCP or Tomcat's built-in JDBC pool) for production-grade deployments.
