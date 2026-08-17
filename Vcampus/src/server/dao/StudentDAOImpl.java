package server.dao;

import common.model.Student;
import common.model.StudentPersonal;
import common.model.StudentPersonalAudit;
import util.DBUtil;
import util.EncryptUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    /** 新增学生；成功返回 true，主键或身份证重复返回 false */
   /*public boolean insert(Student stu) {
        String sql = "INSERT INTO student(student_id, student_name, sex, identity, user_id) VALUES(?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stu.getStudentId());
            ps.setString(2, stu.getStudentName());
            ps.setString(3, stu.getSex());
            ps.setString(4, stu.getIdentity());
            ps.setString(5, stu.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException duplicate) {
            return false;  // 主键/身份证重复
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}*/
	/**
	 * 新增学生；成功返回 null，失败返回错误信息
	 */
	public String insert(Student stu) {
	    Connection conn = null;
	    PreparedStatement psStu  = null;
	    PreparedStatement psPer  = null;
	    PreparedStatement psUser = null;
	    try {
	        conn = DBUtil.getConnection();
	        conn.setAutoCommit(false);

	        // 1. 插入 student
	        String sqlStu = "INSERT INTO student(student_id,student_name,sex,identity,user_id) VALUES(?,?,?,?,?)";
	        psStu = conn.prepareStatement(sqlStu);
	        psStu.setString(1, stu.getStudentId());
	        psStu.setString(2, stu.getStudentName());
	        psStu.setString(3, stu.getSex());
	        psStu.setString(4, stu.getIdentity());
	        psStu.setString(5, stu.getUserId());
	        int rows1 = psStu.executeUpdate();

	        // 2. 插入 student_personal（空）
	        String sqlPer = "INSERT INTO student_personal(student_id,phone,email,address) VALUES(?,?,?,?)";
	        psPer = conn.prepareStatement(sqlPer);
	        psPer.setString(1, stu.getStudentId());
	        psPer.setString(2, "");
	        psPer.setString(3, "");
	        psPer.setString(4, "");
	        int rows2 = psPer.executeUpdate();

	        // 3. 插入 user 账号（身份证后6位=密码）
	        String identity = stu.getIdentity();
	        String pwd = String.format("%06d", Integer.parseInt(identity.length() >= 6 ? identity.substring(identity.length() - 6) : identity));
	        String email = stu.getStudentId() + System.currentTimeMillis() + "@stu.vcampus.com";
	        String sqlUser = "INSERT INTO user(user_id,password,role,name,email) VALUES(?,?,?,?,?)";
	        psUser = conn.prepareStatement(sqlUser);
	        psUser.setString(1, stu.getUserId());
	        psUser.setString(2, EncryptUtil.sha256(pwd));
	        psUser.setString(3, "student");
	        psUser.setString(4, stu.getStudentName());
	        psUser.setString(5, email);
	        int rows3 = psUser.executeUpdate();

	        conn.commit();
	        if (rows1 > 0 && rows2 > 0 && rows3 > 0) {
	            return null; // 成功
	        } else {
	            conn.rollback();
	            return "受影响行数为 0，事务回滚";
	        }

	    } catch (SQLIntegrityConstraintViolationException dup) {
	        String info = "主键/唯一索引冲突：" + dup.getMessage();
	        System.err.println(info);
	        rollbackQuietly(conn);
	        return info;

	    } catch (SQLException e) {
	        String info = "SQL异常：" + e.getMessage();
	        System.err.println("▼▼▼ 数据库异常 ▼▼▼");
	        e.printStackTrace();
	        rollbackQuietly(conn);
	        return info;

	    } catch (NumberFormatException e) {
	        String info = "身份证后6位解析失败：" + e.getMessage();
	        System.err.println(info);
	        rollbackQuietly(conn);
	        return info;

	    } finally {
	        closeQuietly(psUser);
	        closeQuietly(psPer);
	        closeQuietly(psStu);
	        closeQuietly(conn);
	    }
	}

	/* ---------- 下面是小工具 ---------- */

	private static void rollbackQuietly(Connection conn) {
	    if (conn != null) try { conn.rollback(); } catch (SQLException ignore) {}
	}

	private static void closeQuietly(AutoCloseable c) {
	    if (c != null) try { c.close(); } catch (Exception ignore) {}
	}
	    
//在 StudentDao.java 里追加
/**
* 按学号查询
*/
	/* 2. 管理员查询待审列表 */
	public List<StudentPersonalAudit> findPendingAudits() {
	    String sql = 
	        "SELECT a.student_id, a.phone, a.email, a.address, a.apply_time, a.status, s.student_name " +
	        "FROM student_personal_audit a " +
	        "JOIN student s ON a.student_id = s.student_id " +
	        "WHERE a.status = 'PENDING' " +
	        "ORDER BY a.apply_time";
	    try {
	        System.out.println("【诊断】数据库连接 = " + DBUtil.getConnection());
	    } catch (SQLException e) {
	        System.out.println("【诊断】拿连接失败 = " + e.getMessage());
	        e.printStackTrace();
	    }
	    List<StudentPersonalAudit> list = new ArrayList<>();
	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            StudentPersonalAudit a = new StudentPersonalAudit();
	            a.setStudentId(rs.getString("student_id"));
	            a.setStudentName(rs.getString("student_name"));
	            a.setPhone    (rs.getString("phone"));
	            a.setEmail    (rs.getString("email"));
	            a.setAddress  (rs.getString("address"));
	            a.setApplyTime(rs.getTimestamp("apply_time"));
	            a.setStatus   (rs.getString("status"));   // ← 别忘了这行
	            list.add(a);
	        }
	    } catch (SQLException e) {
	        System.out.println("【findPendingAudits 异常】" + e.getMessage());
	        e.printStackTrace();
	    }
	    return list;
	}
public Student findById(String studentId) {
	String sql = "SELECT s.student_id, s.student_name, s.sex, s.identity, s.user_id, " +
            "       p.phone, p.email, p.address " +
            "FROM student s LEFT JOIN student_personal p ON s.student_id = p.student_id " +
            "WHERE s.student_id = ?";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, studentId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            // 先组装 Student 原有字段
            Student s = new Student();
            s.setStudentId(rs.getString("student_id"));
            s.setStudentName(rs.getString("student_name"));
            s.setSex(rs.getString("sex"));
            s.setIdentity(rs.getString("identity"));
            s.setUserId(rs.getString("user_id"));

            // 再组装 StudentPersonal（可能全为 null）
            String phone  = rs.getString("phone");
            String email  = rs.getString("email");
            String address = rs.getString("address");

            // 只要有一列非空，就认为存在记录
            if (phone != null || email != null || address != null) {
                s.setPersonal(new StudentPersonal(
                        s.getStudentId(),
                        phone  == null ? "" : phone,
                        email  == null ? "" : email,
                        address == null ? "" : address));
            }
            return s;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

/**
* 按学号删除
*/
public boolean delete(String studentId) {
 String sql = "DELETE FROM student WHERE student_id=?";
 try (Connection conn = DBUtil.getConnection();
      PreparedStatement ps = conn.prepareStatement(sql)) {
     ps.setString(1, studentId);
     return ps.executeUpdate() > 0;
 } catch (SQLException e) {
     e.printStackTrace();
     return false;
 }
}
/**
 * 按学号更新学生信息
 * @return 成功返回 true，学号不存在返回 false
 */
public boolean update(Student stu) {
    String sql = "UPDATE student SET student_name=?, sex=?, identity=?, user_id=? WHERE student_id=?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, stu.getStudentName());
        ps.setString(2, stu.getSex());
        ps.setString(3, stu.getIdentity());
        ps.setString(4, stu.getUserId());
        ps.setString(5, stu.getStudentId());   // 作为条件

        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
//StudentDao.java
public List<Student> findAll() {
    String sql = "SELECT s.student_id, s.student_name, s.sex, s.identity, s.user_id, " +
                 "       p.phone, p.email, p.address " +
                 "FROM student s LEFT JOIN student_personal p ON s.student_id = p.student_id";
    List<Student> list = new ArrayList<>();
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Student s = new Student();
            s.setStudentId(rs.getString("student_id"));
            s.setStudentName(rs.getString("student_name"));
            s.setSex(rs.getString("sex"));
            s.setIdentity(rs.getString("identity"));
            s.setUserId(rs.getString("user_id"));

            // 组装 StudentPersonal
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String address = rs.getString("address");
            if (phone != null || email != null || address != null) {
                s.setPersonal(new StudentPersonal(
                    s.getStudentId(),
                    phone == null ? "" : phone,
                    email == null ? "" : email,
                    address == null ? "" : address));
            }
            list.add(s);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
/**
 * 联查学籍 + 个人基本信息
 */

/* 1. 学生提交待审 */
public boolean submitPersonalAudit(StudentPersonal sp) {
    String sql = "INSERT INTO student_personal_audit(student_id,phone,email,address,status,apply_time) " +
                 "VALUES(?,?,?,?,'PENDING',now()) " +
                 "ON DUPLICATE KEY UPDATE " +
                 "phone=VALUES(phone),email=VALUES(email),address=VALUES(address),status='PENDING',apply_time=now()";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, sp.getStudentId());
        ps.setString(2, sp.getPhone());
        ps.setString(3, sp.getEmail());
        ps.setString(4, sp.getAddress());
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/* 2. 管理员查询待审列表 */
public String findStudentIdByUserId(String userId) {
    String sql = "SELECT student_id FROM student WHERE user_id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString(1);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

/**
 * 查询学生联系方式审核状态
 * @return 状态字符串（PENDING / APPROVED / REJECTED）或 null
 */


/* 3. 管理员审批 */
public boolean auditPersonal(String studentId, boolean approve, String auditorId, String remark) {
    String sql;
    if (approve) {
        sql = "INSERT INTO student_personal(student_id,phone,email,address) " +
              "VALUES((SELECT student_id FROM student_personal_audit WHERE student_id=?), " +
              "       (SELECT phone FROM student_personal_audit WHERE student_id=?), " +
              "       (SELECT email FROM student_personal_audit WHERE student_id=?), " +
              "       (SELECT address FROM student_personal_audit WHERE student_id=?)) " +
              "ON DUPLICATE KEY UPDATE " +
              "phone=VALUES(phone),email=VALUES(email),address=VALUES(address)";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 1; i <= 4; i++) ps.setString(i, studentId);
                ps.executeUpdate();
            }
            sql = "UPDATE student_personal_audit SET status='APPROVED',audit_time=now(),auditor_id=?,remark=? WHERE student_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, auditorId);
                ps.setString(2, remark);
                ps.setString(3, studentId);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    } else {
        sql = "UPDATE student_personal_audit SET status='REJECTED',audit_time=now(),auditor_id=?,remark=? WHERE student_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auditorId);
            ps.setString(2, remark);
            ps.setString(3, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
/** 保存或更新基本信息：存在则 UPDATE，不存在则 INSERT */
public boolean saveOrUpdatePersonal(StudentPersonal sp) {
    String sql = "INSERT INTO student_personal(student_id, phone, email, address) " +
                 "VALUES (?,?,?,?) " +
                 "ON DUPLICATE KEY UPDATE phone=VALUES(phone), email=VALUES(email), address=VALUES(address)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, sp.getStudentId());
        ps.setString(2, sp.getPhone());
        ps.setString(3, sp.getEmail());
        ps.setString(4, sp.getAddress());
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
public String getPersonalAuditStatus(String studentId) {
    String sql = "SELECT status FROM student_personal_audit WHERE student_id=?";
    return queryForString(sql, studentId);   // 自己封装即可
}

public String getPersonalAuditRemark(String studentId) {
    String sql = "SELECT remark FROM student_personal_audit WHERE student_id=?";
    return queryForString(sql, studentId);
}
/**
 * 私有工具：执行返回单行单列字符串的 SQL
 * @param sql  带一个 ? 占位符的查询语句
 * @param id   要绑定的 student_id
 * @return     查到的字符串；无结果或异常返回 ""
 */
private String queryForString(String sql, String id) {
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getString(1) : "";   // 有值返回值，没有返回空串
    } catch (SQLException e) {
        e.printStackTrace();
        return "";
    }
}
public StudentPersonal getPersonal(String studentId) {
    String sql = "SELECT phone, email, address FROM student_personal WHERE student_id=?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, studentId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new StudentPersonal(
                studentId,
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    // 没有记录返回空对象
    return new StudentPersonal(studentId, "", "", "");
}
}
