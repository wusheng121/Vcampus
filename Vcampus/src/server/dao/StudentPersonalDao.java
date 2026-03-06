package server.dao;

import common.model.StudentPersonal;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentPersonalDao {

    /* 学生提交/更新个人资料（自动待审核） */
    public boolean savePersonal(StudentPersonal p) {
        String sql = """
            INSERT INTO student_personal
            (student_id,phone,email,address,status,apply_time)
            VALUES (?,?,?,?,'PENDING',NOW())
            ON DUPLICATE KEY UPDATE
            phone=?,email=?,address=?,status='PENDING',apply_time=NOW(),
            auditor_id=NULL,audit_time=NULL
            """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getStudentId());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getAddress());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* 根据学号查个人资料 */
   

    /* 管理员审核 */
    public boolean audit(String studentId, boolean pass, String auditorId) {
        String sql = "UPDATE student_personal SET status=?, auditor_id=?, audit_time=NOW() WHERE student_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pass ? "APPROVED" : "REJECTED");
            ps.setString(2, auditorId);
            ps.setString(3, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* 待审核列表 */
 

}

