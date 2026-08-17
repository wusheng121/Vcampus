package server.net;

import common.model.Student;
import common.model.StudentPersonal;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.dao.StudentDAOImpl;
import server.service.AuthService;
import server.service.StudentService;

import java.util.ArrayList;

/**
 * 学籍 / 学生个人资料域
 */
public class StudentHandler implements Handler {
    private final StudentService studentService = new StudentService();
    private final AuthService auth = new AuthService();

    private boolean denied(Message request, Message response, String perm) {
        User c = request.getCaller();
        if (c == null || !auth.hasPermission(c.getType(), perm)) {
            response.setStatus("fail");
            response.setMsg("无权限：" + perm);
            return true;
        }
        return false;
    }

    @Override
    public Message handle(Message request) {
        Message response = new Message();
        switch (request.getType()) {
            case MessageType.STUDENT_LIST -> {
                response.setStatus("success");
                response.setData(new ArrayList<>(studentService.getAllStudents()));
            }
            case MessageType.STUDENT_GET -> {
                String id = (String) request.getData();
                response.setStatus("success");
                response.setData(studentService.getStudent(id));
            }
            case MessageType.STUDENT_ADD -> {
                if (denied(request, response, Permissions.STUDENT_CREATE)) break;
                Student stu = (Student) request.getData();
                String err = studentService.addStudentAndGetErr(stu);
                boolean ok = (err == null);
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "" : "插入失败：" + (err.isEmpty() ? "学号/身份证/一卡通重复" : err));
            }
            case MessageType.STUDENT_UPDATE -> {
                if (denied(request, response, Permissions.STUDENT_UPDATE)) break;
                boolean ok = studentService.updateStudent((Student) request.getData());
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_DELETE -> {
                if (denied(request, response, Permissions.STUDENT_DELETE)) break;
                boolean ok = studentService.deleteStudent((String) request.getData());
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_AUDIT_LIST -> {
                response.setStatus("success");
                response.setData(new ArrayList<>(studentService.getPendingAudits()));
            }
            case MessageType.STUDENT_AUDIT_PASS -> {
                if (denied(request, response, Permissions.STUDENT_AUDIT)) break;
                Object[] arr = (Object[]) request.getData();  // [studentId, auditor, remark]
                boolean ok = studentService.audit((String) arr[0], true, (String) arr[1], (String) arr[2]);
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_AUDIT_REJECT -> {
                if (denied(request, response, Permissions.STUDENT_AUDIT)) break;
                Object[] arr = (Object[]) request.getData();
                boolean ok = studentService.audit((String) arr[0], false, (String) arr[1], (String) arr[2]);
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_AUDIT_SUBMIT -> {
                StudentPersonal sp = (StudentPersonal) request.getData();
                boolean ok = studentService.submitPersonalAudit(sp);
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_AUDIT_STATUS -> {
                String studentId = (String) request.getData();
                String status = studentService.getPersonalAuditStatus(studentId);
                response.setStatus("success");
                response.setData(status);
            }
            case MessageType.STUDENT_AUDIT_REMARK -> {
                String studentId = (String) request.getData();
                String remark = studentService.getPersonalAuditRemark(studentId);
                response.setStatus("success");
                response.setData(remark);
            }
            case MessageType.STUDENT_PERSONAL_SAVE -> {
                StudentPersonal sp = (StudentPersonal) request.getData();
                boolean ok = studentService.saveOrUpdatePersonal(sp);
                response.setStatus(ok ? "success" : "fail");
            }
            case MessageType.STUDENT_ID_BY_USERID -> {
                String userId = (String) request.getData();
                System.out.println("[诊断] 收到反查请求 userId = " + userId);
                String stuId = new StudentDAOImpl().findStudentIdByUserId(userId);
                System.out.println("[诊断] 反查结果 stuId = " + stuId);
                response = new Message();
                response.setStatus("success");
                response.setData(stuId);
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
