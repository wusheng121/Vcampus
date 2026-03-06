package server.dao;

import common.model.Teacher;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAOImpl implements TeacherDAO{

    /* 把 ResultSet → Teacher 对象（含新字段） */
    private Teacher rowToTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setTeacherId  (rs.getString("teacher_id"));
        t.setTeacherName(rs.getString("teacher_name"));
        t.setSex        (rs.getString("sex"));
        t.setSubject    (rs.getString("subject"));
        t.setUserId     (rs.getString("user_id"));
        t.setPhone      (rs.getString("phone"));
        t.setEmail      (rs.getString("email"));
        t.setAddress    (rs.getString("address"));
        return t;
    }

    @Override
    public List<Teacher> listTeacher() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM teacher";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rowToTeacher(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Teacher getTeacherById(String teacherId) {
        String sql = "SELECT * FROM teacher WHERE teacher_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowToTeacher(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Teacher getTeacherByUserId(String userId) {
        String sql = "SELECT * FROM teacher WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowToTeacher(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
