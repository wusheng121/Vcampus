package server.dao;

import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class PasswordResetDAOImpl implements PasswordResetDAO {

    @Override
    public boolean save(String userId, String codeHash, Timestamp expireTime) {
        String sql = "INSERT INTO password_reset(user_id, code_hash, expire_time, used) VALUES(?,?,?,0)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, codeHash);
            ps.setTimestamp(3, expireTime);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ResetRecord findLatestUnused(String userId) {
        String sql = "SELECT id, user_id, code_hash, expire_time FROM password_reset " +
                     "WHERE user_id=? AND used=0 AND expire_time > ? " +
                     "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResetRecord(
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("code_hash"),
                            rs.getTimestamp("expire_time"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean markUsed(int id) {
        String sql = "UPDATE password_reset SET used=1 WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int deleteStale(String userId) {
        String sql = "DELETE FROM password_reset WHERE user_id=? AND (used=1 OR expire_time < ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressWarnings("unused")
    private static Date now() { return new Date(); }
}
