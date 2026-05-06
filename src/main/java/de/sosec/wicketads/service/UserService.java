package de.sosec.wicketads.service;

import de.sosec.wicketads.db.DatabaseInitializer;
import de.sosec.wicketads.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setFullName(rs.getString("full_name"));
        u.setStreet(rs.getString("street"));
        u.setHouseNumber(rs.getString("house_number"));
        u.setZipCode(rs.getString("zip_code"));
        u.setCity(rs.getString("city"));
        u.setCountry(rs.getString("country"));
        u.setPhone(rs.getString("phone"));
        u.setEmail(rs.getString("email"));
        return u;
    }

    public static User findByUsername(String username) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static User findById(int id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static int create(String username, String password, String role) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static void update(int id, String username, String password, String role,
                              String fullName, String street, String houseNumber,
                              String zipCode, String city, String country,
                              String phone, String email) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET username=?, password=?, role=?, full_name=?, street=?, " +
                     "house_number=?, zip_code=?, city=?, country=?, phone=?, email=? WHERE id=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setString(4, fullName);
            ps.setString(5, street);
            ps.setString(6, houseNumber);
            ps.setString(7, zipCode);
            ps.setString(8, city);
            ps.setString(9, country);
            ps.setString(10, phone);
            ps.setString(11, email);
            ps.setInt(12, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteById(int id) {
        try (Connection conn = DatabaseInitializer.getConnection()) {
            // Delete messages first
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM messages WHERE sender_id = ? OR recipient_id = ?")) {
                ps.setInt(1, id);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            // Delete ads
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM ads WHERE owner_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // Delete user
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
