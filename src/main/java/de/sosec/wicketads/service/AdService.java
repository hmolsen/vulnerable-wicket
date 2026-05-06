package de.sosec.wicketads.service;

import de.sosec.wicketads.db.DatabaseInitializer;
import de.sosec.wicketads.model.Ad;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdService {

    private static Ad mapAd(ResultSet rs) throws SQLException {
        Ad ad = new Ad();
        ad.setId(rs.getInt("id"));
        ad.setOwnerId(rs.getInt("owner_id"));
        ad.setTitle(rs.getString("title"));
        ad.setDescription(rs.getString("description"));
        ad.setPrice(rs.getBigDecimal("price"));
        ad.setCategory(rs.getString("category"));
        ad.setCreatedAt(rs.getTimestamp("created_at"));
        try { ad.setOwnerUsername(rs.getString("owner_username")); } catch (SQLException ignored) {}
        return ad;
    }

    public static List<Ad> findRecent() {
        List<Ad> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT a.*, u.username as owner_username FROM ads a " +
                     "JOIN users u ON u.id = a.owner_id ORDER BY a.created_at DESC LIMIT 50")) {
            while (rs.next()) list.add(mapAd(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // VULNERABILITY: SQL injection via string concatenation
    public static List<Ad> search(String keyword, String category) {
        List<Ad> list = new ArrayList<>();
        String sql = "SELECT a.*, u.username as owner_username FROM ads a " +
                     "JOIN users u ON u.id = a.owner_id WHERE 1=1";

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND (LOWER(a.title) LIKE '%" + keyword.toLowerCase() + "%' OR LOWER(a.description) LIKE '%" + keyword.toLowerCase() + "%')";
        }
        if (category != null && !category.isBlank()) {
            sql += " AND a.category = '" + category + "'";
        }
        sql += " ORDER BY a.created_at DESC";

        try (Connection conn = DatabaseInitializer.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapAd(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static Ad findById(int id) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT a.*, u.username as owner_username FROM ads a " +
                     "JOIN users u ON u.id = a.owner_id WHERE a.id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAd(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static int create(int ownerId, String title, String description,
                             java.math.BigDecimal price, String category) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO ads (owner_id, title, description, price, category) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ownerId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setString(5, category);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static List<Ad> findByOwner(int ownerId) {
        List<Ad> list = new ArrayList<>();
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT a.*, u.username as owner_username FROM ads a " +
                     "JOIN users u ON u.id = a.owner_id WHERE a.owner_id = ? ORDER BY a.created_at DESC")) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAd(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
