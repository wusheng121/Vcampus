package server.dao;

import common.model.Teacher;

import java.util.List;

public interface TeacherDAO {
    // list所有老师
    List<Teacher> listTeacher();
    // 以id找寻老师
    Teacher getTeacherById(String id);
    /* 根据外键 user_id 反查教师 */
    Teacher getTeacherByUserId(String userId);
}
