package server.dao;

import common.model.Student;
import common.model.StudentPersonal;
import common.model.StudentPersonalAudit;

import java.util.List;

/**
 * 学生学籍 / 个人资料数据访问接口。
 * 与其他 DAO（UserDAO / BookDAO / ...）保持 interface + Impl 的一致模式。
 */
public interface StudentDAO {

    /** 新增学生；成功返回 null，失败返回错误信息 */
    String insert(Student stu);

    /** 管理员查询待审列表 */
    List<StudentPersonalAudit> findPendingAudits();

    /** 按学号查询学生（联查个人基本信息） */
    Student findById(String studentId);

    /** 按学号删除 */
    boolean delete(String studentId);

    /** 按学号更新学生信息 */
    boolean update(Student stu);

    /** 查询全部学生 */
    List<Student> findAll();

    /** 学生提交个人资料变更审核 */
    boolean submitPersonalAudit(StudentPersonal sp);

    /** 根据 user_id 反查 student_id */
    String findStudentIdByUserId(String userId);

    /** 管理员审批个人资料变更 */
    boolean auditPersonal(String studentId, boolean approve, String auditorId, String remark);

    /** 保存或更新学生基本信息（存在则 UPDATE，不存在则 INSERT） */
    boolean saveOrUpdatePersonal(StudentPersonal sp);

    /** 查询学生联系方式审核状态（PENDING/APPROVED/REJECTED） */
    String getPersonalAuditStatus(String studentId);

    /** 查询审核备注 */
    String getPersonalAuditRemark(String studentId);

    /** 获取学生个人资料 */
    StudentPersonal getPersonal(String studentId);
}
