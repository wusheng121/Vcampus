package client.controller;
import client.net.ClientSocket;
import common.model.Student;
import common.model.StudentPersonal;
import common.model.StudentPersonalAudit;
import common.net.Message;
import common.net.MessageType;
import server.dao.StudentDAO;

import java.util.ArrayList;
import java.util.List;
public class StudentController {
private final ClientSocket client = new ClientSocket();
public List<Student> getAllStudents() {
    try {
        Message req = new Message(MessageType.STUDENT_LIST, null);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (List<Student>) resp.getData();
        }
    } catch (Exception e) { e.printStackTrace(); }
    return new ArrayList<>();
}

public Student getStudent(String studentId) {
    try {
        Message req = new Message(MessageType.STUDENT_GET, studentId);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (Student) resp.getData();
        }
    } catch (Exception e) { e.printStackTrace(); }
    return null;
}

public boolean addStudent(Student stu) {
    try {
        Message req = new Message(MessageType.STUDENT_ADD, stu);
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) { e.printStackTrace(); return false; }
}

public boolean updateStudent(Student stu) {
    try {
        Message req = new Message(MessageType.STUDENT_UPDATE, stu);
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) { e.printStackTrace(); return false; }
}

public boolean deleteStudent(String studentId) {
    try {
        Message req = new Message(MessageType.STUDENT_DELETE, studentId);
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) { e.printStackTrace(); return false; }
}

public boolean submitPersonalAudit(StudentPersonal sp) {
    try {
        Message req = new Message(MessageType.STUDENT_AUDIT_SUBMIT, sp);
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) { e.printStackTrace(); return false; }
}

public List<StudentPersonalAudit> getPendingAudits() {
    try {
        Message req = new Message(MessageType.STUDENT_AUDIT_LIST, null);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (List<StudentPersonalAudit>) resp.getData();
        }
    } catch (Exception e) { e.printStackTrace(); }
    return new ArrayList<>();
}

public boolean audit(String studentId, boolean pass, String auditor, String remark) {
    try {
        Message req = new Message(
                pass ? MessageType.STUDENT_AUDIT_PASS : MessageType.STUDENT_AUDIT_REJECT,
                new Object[]{studentId, auditor, remark});
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) { e.printStackTrace(); return false; }
}
/* ----- 获取审核状态 ----- */
public String getPersonalAuditStatus(String studentId) {
    try {
        Message req = new Message(MessageType.STUDENT_AUDIT_STATUS, studentId);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (String) resp.getData(); // "APPROVED" / "REJECTED" / "PENDING" / ""
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
}

/* ----- 获取审核备注 ----- */
public String getPersonalAuditRemark(String studentId) {
    try {
        Message req = new Message(MessageType.STUDENT_AUDIT_REMARK, studentId);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (String) resp.getData(); // 拒绝原因
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
}
/* 管理员保存/更新联系方式（存在则 UPDATE，不存在则 INSERT）*/
public boolean saveOrUpdatePersonal(StudentPersonal sp) {
    try {
        Message req = new Message(MessageType.STUDENT_PERSONAL_SAVE, sp);
        Message resp = client.sendRequest(req);
        return "success".equals(resp.getStatus());
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
/** 弹出窗口用：获取个人基本信息 */
public StudentPersonal getPersonal(String studentId) {
    return new StudentDAO().getPersonal(studentId);
}

/** 保存按钮用：保存个人基本信息 */
public boolean savePersonal(StudentPersonal sp) {
    return new StudentDAO().saveOrUpdatePersonal(sp);
}
/** 用一卡通号反查学号 */
public String findStudentIdByUserId(String userId) {
    try {
        Message req = new Message(MessageType.STUDENT_ID_BY_USERID, userId);
        Message resp = client.sendRequest(req);
        if ("success".equals(resp.getStatus())) {
            return (String) resp.getData();   // 学号
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}}