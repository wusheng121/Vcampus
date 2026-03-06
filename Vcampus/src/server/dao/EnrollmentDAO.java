package server.dao;

import common.model.Enrollment;

import java.util.List;

public interface EnrollmentDAO {

    boolean enroll(String studentId, int lessonId);   // 选课（写入或把已存在记录状态改为 enrolled）
    boolean drop(String studentId, int lessonId);     // 退课（把状态改为 dropped）

    List<Enrollment> listByStudent(String studentId); // 查某学生所有选课记录
    List<Enrollment> listByLesson(int lessonId);      // 查某门课的所有学生（老师看名单时用）

    int countEnrolled(int lessonId);                  // 统计已选人数（配合容量用）
}
