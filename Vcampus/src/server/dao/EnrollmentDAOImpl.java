package server.dao;

import common.model.Enrollment;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public boolean enroll(String studentId, int lessonId) {
        // 利用唯一键 (student_id, lesson_id)：已存在则更新为 enrolled；否则插入
        String sql = "INSERT INTO enrollment (student_id, lesson_id, status) " +
                     "VALUES (?, ?, 'enrolled') " +
                     "ON DUPLICATE KEY UPDATE status = VALUES(status), created_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, lessonId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 可按需改成抛异常
        }
    }

    @Override
    public boolean drop(String studentId, int lessonId) {
        String sql = "UPDATE enrollment SET status = 'dropped' WHERE student_id = ? AND lesson_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, lessonId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Enrollment> listByStudent(String studentId) {
        String sql = "SELECT enroll_id, student_id, lesson_id, status, created_at " +
                     "FROM enrollment WHERE student_id = ? ORDER BY created_at DESC";
        List<Enrollment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEnrollment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Enrollment> listByLesson(int lessonId) {
        String sql = "SELECT enroll_id, student_id, lesson_id, status, created_at " +
                     "FROM enrollment WHERE lesson_id = ? ORDER BY created_at DESC";
        List<Enrollment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEnrollment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int countEnrolled(int lessonId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE lesson_id = ? AND status = 'enrolled'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // -------- helper --------
    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollId(rs.getInt("enroll_id"));
        e.setStudentId(rs.getString("student_id"));
        e.setLessonId(rs.getInt("lesson_id"));
        e.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        e.setCreatedAt(ts == null ? null : new java.util.Date(ts.getTime()));
        return e;
    }
}
