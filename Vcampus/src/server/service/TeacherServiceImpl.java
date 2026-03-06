package server.service;

import common.model.Teacher;
import server.dao.TeacherDAO;
import server.dao.TeacherDAOImpl;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TeacherServiceImpl implements TeacherService {

    private final TeacherDAO teacherDAO = new TeacherDAOImpl();

    @Override
    public Teacher getTeacherById(String teacherId) {
        return teacherDAO.getTeacherById(teacherId);
    }

    @Override
    public Teacher getTeacherByUserId(String userId) {
        return teacherDAO.getTeacherByUserId(userId);
    }
 // server.service.TeacherServiceImpl
    @Override
    public boolean updateTeacher(Teacher teacher) {
        // 只更新联系字段（phone/email/address）
        String sql = "UPDATE teacher SET phone=?, email=?, address=? WHERE teacher_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, teacher.getPhone());
            ps.setString(2, teacher.getEmail());
            ps.setString(3, teacher.getAddress());
            ps.setString(4, teacher.getTeacherId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}