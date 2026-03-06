package server.service;
import common.model.Student;
import common.model.StudentPersonal;
import common.model.StudentPersonalAudit;
import server.dao.StudentDAO;
import java.util.ArrayList;
import java.util.List;
public class StudentService {
private final StudentDAO dao = new StudentDAO();
public List<Student> getAllStudents() {
    return dao.findAll();
}

public Student getStudent(String studentId) {
    return dao.findById(studentId);
}

public String addStudent(Student stu) {
    return dao.insert(stu);
}

public boolean updateStudent(Student stu) {
    return dao.update(stu);
}

public boolean deleteStudent(String studentId) {
    return dao.delete(studentId);
}

public boolean submitPersonalAudit(StudentPersonal sp) {
    return dao.submitPersonalAudit(sp);
}

public List<StudentPersonalAudit> getPendingAudits() {
    return dao.findPendingAudits();
}

public boolean audit(String studentId, boolean pass, String auditor, String remark) {
    return dao.auditPersonal(studentId, pass, auditor, remark);
}
public String getPersonalAuditStatus(String studentId) {
    return dao.getPersonalAuditStatus(studentId);
}

public String getPersonalAuditRemark(String studentId) {
    return dao.getPersonalAuditRemark(studentId);
}
public boolean saveOrUpdatePersonal(StudentPersonal sp) {
    return dao.saveOrUpdatePersonal(sp); // 已存在的方法
}
//返回 null 表示成功；否则返回具体冲突信息
public String addStudentAndGetErr(Student stu) {
 try {
	 return dao.insert(stu);
 } catch (Exception e) {           // 捕获所有 SQL 异常
     e.printStackTrace();          // 服务器控制台打印
     return e.getMessage();        // 把异常文本带回客户端
 }
}
}