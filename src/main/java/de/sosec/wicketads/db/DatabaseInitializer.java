package de.sosec.wicketads.db;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static DataSource dataSource;
    private static volatile boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        dataSource = JdbcConnectionPool.create(
                "jdbc:h2:mem:wicketads;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'user',
                    full_name VARCHAR(200),
                    street VARCHAR(200),
                    house_number VARCHAR(20),
                    zip_code VARCHAR(20),
                    city VARCHAR(100),
                    country VARCHAR(100),
                    phone VARCHAR(50),
                    email VARCHAR(200)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ads (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    owner_id INTEGER NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    description VARCHAR(4000),
                    price DECIMAL(10,2),
                    category VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_id) REFERENCES users(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    sender_id INTEGER NOT NULL,
                    recipient_id INTEGER NOT NULL,
                    ad_id INTEGER,
                    body VARCHAR(4000),
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sender_id) REFERENCES users(id),
                    FOREIGN KEY (recipient_id) REFERENCES users(id)
                )
            """);

            // Seed: admin user
            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, email)
                KEY(id)
                VALUES (1, 'admin', 'admin', 'admin', 'Administrator', 'admin@wicketads.local')
            """);

            // Seed: regular users
            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, street, house_number, zip_code, city, country, phone, email)
                KEY(id)
                VALUES (2, 'alice', 'alice123', 'user', 'Alice Smith', 'Main Street', '42', '12345', 'Springfield', 'USA', '+1-555-0100', 'alice@example.com')
            """);

            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, street, house_number, zip_code, city, country, phone, email)
                KEY(id)
                VALUES (3, 'bob', 'bob456', 'user', 'Bob Jones', 'Oak Avenue', '7', '67890', 'Shelbyville', 'USA', '+1-555-0200', 'bob@example.com')
            """);

            // Seed: sample ads for Alice
            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (1, 2, 'Vintage Guitar', 'Beautiful 1960s Stratocaster, barely played. Comes with original case.', 1200.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (2, 2, 'Road Bike - Trek 7.2', 'Excellent condition, 21 speeds, only 200 miles. Selling because I moved downtown.', 350.00, 'Vehicles', CURRENT_TIMESTAMP)
            """);

            // Seed: sample ads for Bob
            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (3, 3, 'MacBook Pro 2021', 'M1 Pro, 16GB RAM, 512GB SSD. Selling to upgrade. Perfect condition.', 1400.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (4, 3, 'Freelance Web Dev Services', 'Full stack developer available for short-term projects. React, Java, Spring.', 75.00, 'Services', CURRENT_TIMESTAMP)
            """);

            // Seed: messages between Alice and Bob about the guitar
            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (1, 3, 2, 1, 'Hi Alice, is the guitar still available?', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (2, 2, 3, 1, 'Yes Bob, it is! Feel free to come and try it.', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (3, 3, 2, 1, 'Great, would you take $1000 for it?', CURRENT_TIMESTAMP)
            """);

            // Reset sequences so new inserts don't conflict with seeded IDs
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 10");
            stmt.execute("ALTER TABLE ads ALTER COLUMN id RESTART WITH 10");
            stmt.execute("ALTER TABLE messages ALTER COLUMN id RESTART WITH 10");

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }

        System.out.println("");
        System.out.println("==============================================");
        System.out.println("  WicketAds is running at http://localhost:8080");
        System.out.println("  Admin login: admin / admin");
        System.out.println("  Seed users:  alice / alice123  |  bob / bob456");
        System.out.println("==============================================");
        System.out.println("");
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
