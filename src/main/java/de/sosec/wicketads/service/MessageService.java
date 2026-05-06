package de.sosec.wicketads.service;

import de.sosec.wicketads.db.DatabaseInitializer;
import de.sosec.wicketads.model.ConversationSummary;
import de.sosec.wicketads.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private static Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setRecipientId(rs.getInt("recipient_id"));
        m.setAdId(rs.getInt("ad_id"));
        m.setBody(rs.getString("body"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        try { m.setSenderUsername(rs.getString("sender_username")); } catch (SQLException ignored) {}
        try { m.setRecipientUsername(rs.getString("recipient_username")); } catch (SQLException ignored) {}
        return m;
    }

    public static List<ConversationSummary> getConversations(int userId) {
        List<ConversationSummary> list = new ArrayList<>();
        String sql = """
            SELECT sub.other_user_id, u.username as other_username, sub.ad_id, a.title as ad_title
            FROM (
                SELECT DISTINCT recipient_id as other_user_id, ad_id FROM messages WHERE sender_id = ?
                UNION
                SELECT DISTINCT sender_id as other_user_id, ad_id FROM messages WHERE recipient_id = ?
            ) sub
            JOIN users u ON u.id = sub.other_user_id
            LEFT JOIN ads a ON a.id = sub.ad_id
            ORDER BY other_username
            """;
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ConversationSummary(
                        rs.getInt("other_user_id"),
                        rs.getString("other_username"),
                        rs.getInt("ad_id"),
                        rs.getString("ad_title")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // VULNERABILITY: no check that the requesting user is actually a participant
    public static List<Message> getThread(int userId1, int userId2) {
        List<Message> list = new ArrayList<>();
        String sql = """
            SELECT m.id, m.sender_id, m.recipient_id, m.ad_id, m.body, m.sent_at,
                   s.username as sender_username, r.username as recipient_username
            FROM messages m
            JOIN users s ON s.id = m.sender_id
            JOIN users r ON r.id = m.recipient_id
            WHERE (m.sender_id = ? AND m.recipient_id = ?)
               OR (m.sender_id = ? AND m.recipient_id = ?)
            ORDER BY m.sent_at ASC
            """;
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMessage(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static void send(int senderId, int recipientId, int adId, String body) {
        try (Connection conn = DatabaseInitializer.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO messages (sender_id, recipient_id, ad_id, body) VALUES (?,?,?,?)")) {
            ps.setInt(1, senderId);
            ps.setInt(2, recipientId);
            if (adId > 0) ps.setInt(3, adId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, body);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
