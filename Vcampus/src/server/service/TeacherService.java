package server.service;

import common.model.Teacher;

public interface TeacherService {
    /* 根据 teacherId 查完整对象 */
    Teacher getTeacherById(String teacherId);

    /* 根据 userId 反查教师对象 */
    Teacher getTeacherByUserId(String userId);
    boolean updateTeacher(Teacher teacher);
}