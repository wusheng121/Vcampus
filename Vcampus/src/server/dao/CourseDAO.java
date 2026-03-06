package server.dao;

import common.model.Course;
import common.model.Lesson;
import common.model.LessonTime;

import java.util.List;

public interface CourseDAO {
    // ---- course ----
    List<Course> listCourses();
    Course getCourseById(int courseId);

    // ---- lesson ----
    List<Lesson> listLessons();             // 列出所有开课（后续可加过滤条件）
    Lesson getLessonById(int lessonId);
    
    // ---- lesson_time ----
    List<LessonTime> listLessonTimes(int lessonId);

    int addCourse(Course course);          // 返回生成的 courseId（失败返回 -1）
    boolean updateCourse(Course course);   // 根据 courseId 更新
    boolean deleteCourse(int courseId);    // 删除课程（见实现中的事务说明）

    // 新增：课时 CRUD
    int addLesson(Lesson lesson);          // 返回生成的 lessonId（失败返回 -1）
    boolean updateLesson(Lesson lesson);   // 根据 lessonId 更新
    boolean deleteLesson(int lessonId);    // 删除课时（同时清理 lesson_time

    // 新增：上课时间
    int addLessonTime(LessonTime t);           // 返回生成的 time_id
    boolean updateLessonTime(LessonTime t);
    boolean deleteLessonTime(int timeId);

}
