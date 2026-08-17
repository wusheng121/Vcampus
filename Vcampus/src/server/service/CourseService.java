package server.service;

import common.model.*;
import server.dao.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 课程/选课相关的业务逻辑：
 * - 查询：课与时间
 * - 选课：开放/容量/时间冲突检查
 * - 退课
 *
 * 说明：
 * 1) 不访问 DB 工具类，所有 SQL 都在 DAO；这里只组织业务。
 * 2) 不新增 DTO；直接用已有的 Model。
 */
public class CourseService {

    private final CourseDAO courseDAO = new CourseDAOImpl();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAOImpl();
    private final TeacherDAO teacherDAO = new TeacherDAOImpl();


    // ========== 教师 ==========
    public List<Teacher> listTeachers() {
        return teacherDAO.listTeacher();
    }
    public Teacher getTeacherById(String id) {
        if(!id.strip().isEmpty()){
            return teacherDAO.getTeacherById(id);
        } else {
            return new Teacher();
        }
    }

    // ========== 查询 ==========
    // 取得course
    public Course getCourse(int courseId) {
        return courseDAO.getCourseById(courseId);
    }
    // 取得courses列表
    public List<Course> listCourses() {
        return courseDAO.listCourses();
    }

    // 取得lesson列表
    public List<Lesson> listLessons() {
        return courseDAO.listLessons();
    }
    // 取得lesson
    public Lesson getLesson(int lessonId) {
        return courseDAO.getLessonById(lessonId);
    }

    public List<LessonTime> listLessonTimes(int lessonId) {
        return courseDAO.listLessonTimes(lessonId);
    }

    /** 批量：一次返回全部上课时间，消除逐课 N+1。 */
    public List<LessonTime> listAllLessonTimes() {
        return courseDAO.listAllLessonTimes();
    }

    /** 批量：所有 lesson 的已选人数（lessonId -> count），消除逐课 N+1。 */
    public Map<Integer, Integer> countEnrolledForAll() {
        return courseDAO.countEnrolledForAll();
    }

    public List<Enrollment> listEnrollmentsByStudent(String studentId) {
        return enrollmentDAO.listByStudent(studentId);
    }

    public List<Enrollment> listEnrollmentsByLesson(int lessonId) {
        return enrollmentDAO.listByLesson(lessonId);
    }

    // ========== LessonTime 管理 ==========
    public Result createLessonTime(LessonTime t) {
        String err = validateLessonTime(t, false);
        if (err != null) return Result.fail(err);

        if (courseDAO.getLessonById(t.getLessonId()) == null) {
            return Result.fail("关联开课不存在");
        }

        int id = courseDAO.addLessonTime(t);
        return id > 0 ? Result.ok() : Result.fail("新增上课时间失败");
    }

    public Result updateLessonTime(LessonTime t) {
        String err = validateLessonTime(t, true);
        if (err != null) return Result.fail(err);
        boolean ok = courseDAO.updateLessonTime(t);
        return ok ? Result.ok() : Result.fail("更新上课时间失败");
    }

    public Result deleteLessonTime(int timeId) {
        if (timeId <= 0) return Result.fail("非法时间ID");
        boolean ok = courseDAO.deleteLessonTime(timeId);
        return ok ? Result.ok() : Result.fail("删除上课时间失败");
    }

    // 校验 LessonTime 数据
    private String validateLessonTime(LessonTime t, boolean isUpdate) {
        if (t == null) return "时间片为空";
        if (isUpdate && t.getTimeId() <= 0) return "非法时间ID";
        if (t.getLessonId() <= 0) return "lessonId 非法";
        if (t.getDayOfWeek() < 1 || t.getDayOfWeek() > 7) return "星期必须是 1-7";
        if (t.getStartSec() > t.getEndSec()) return "开始节次不能大于结束节次";
        return null;
    }

    public int countEnrolled(int lessonId) {
        return enrollmentDAO.countEnrolled(lessonId);
    }

// ========== 管理：课程 & 开课 CRUD ==========
// -------- Course --------

/** 新增课程：通过 DAO 新增并回填主键 */
public Result createCourse(Course course) {
    String err = validateCourse(course);
    if (err != null) return Result.fail(err);

    int id = courseDAO.addCourse(course);
    return id > 0 ? Result.ok() : Result.fail("新增课程失败（数据库执行失败）");
}

/** 更新课程 */
public Result updateCourse(Course course) {
    if (course == null || course.getCourseId() <= 0) return Result.fail("非法课程ID");
    String err = validateCourse(course);
    if (err != null) return Result.fail(err);

    boolean ok = courseDAO.updateCourse(course);
    return ok ? Result.ok() : Result.fail("更新课程失败（数据库执行失败或课程不存在）");
}

/**
 * 删除课程：
 * 若 DB 未设置 ON DELETE CASCADE，需要先删该课程下所有 lesson（以及其选课/时间）
 * 这里给两种方案：
 * 1) 简单删除（已配置级联时使用）
 * 2) 保护性删除（未配置级联时使用，放开注释启用）
 */
public Result deleteCourse(int courseId) {
    if (courseId <= 0) return Result.fail("非法课程ID");

    // ——方案1：简单删除（有外键级联时用）
    boolean ok = courseDAO.deleteCourse(courseId);
    return ok ? Result.ok() : Result.fail("删除课程失败（可能课程不存在或存在外键限制）");
    
}

// -------- Lesson（开课） --------

/** 新增开课（Lesson） */
public Result createLesson(Lesson lesson) {
    String err = validateLesson(lesson, /*isUpdate=*/false);
    if (err != null) return Result.fail(err);

    // 业务校验：容量非负、课程存在
    Course c = courseDAO.getCourseById(lesson.getCourseId());
    if (c == null) return Result.fail("关联课程不存在");

    int id = courseDAO.addLesson(lesson);
    return id > 0 ? Result.ok() : Result.fail("新增开课失败（数据库执行失败）");
}

/** 更新开课（Lesson） */
public Result updateLesson(Lesson lesson) {
    if (lesson == null || lesson.getLessonId() <= 0) return Result.fail("非法开课ID");
    String err = validateLesson(lesson, /*isUpdate=*/true);
    if (err != null) return Result.fail(err);

    // 若调整到其他课程，需确保目标课程存在
    if (lesson.getCourseId() > 0 && courseDAO.getCourseById(lesson.getCourseId()) == null) {
        return Result.fail("关联课程不存在");
    }

    boolean ok = courseDAO.updateLesson(lesson);
    return ok ? Result.ok() : Result.fail("更新开课失败（数据库执行失败或记录不存在）");
}

/**
 * 删除开课（Lesson）：
 * 若 DB 未设置 ON DELETE CASCADE（lesson_time、enrollment），需要先清理选课、时间等再删。
 * 这里默认简单删除（推荐在 DB 配置外键级联）。需要“安全删除”时，参考注释的方案。
 */
public Result deleteLesson(int lessonId) {
    if (lessonId <= 0) return Result.fail("非法开课ID");

    // ——简单删除（有级联时用）
    boolean ok = courseDAO.deleteLesson(lessonId);
    return ok ? Result.ok() : Result.fail("删除开课失败（可能记录不存在或外键限制）");
}




    // ========== 选课 ==========
    /**
     * 选课带业务校验：是否存在课程、是否开放、是否已满、是否与已选课程冲突。
     * 成功返回 Result.ok=true；失败给出原因。
     */
    public Result enroll(String studentId, int lessonId) {
        // 1) 课程是否存在
        Lesson lesson = courseDAO.getLessonById(lessonId);
        if (lesson == null) {
            return Result.fail("课程不存在");
        }

        // 2) 是否开放
        if (!lesson.isOpen()) {
            return Result.fail("该课程未开放选课");
        }

        // 3) 容量是否已满
        int enrolledCnt = enrollmentDAO.countEnrolled(lessonId);
        if (enrolledCnt >= Math.max(0, lesson.getCapacity())) {
            return Result.fail("课程容量已满");
        }

        // 4) 时间冲突检查（与该学生当前所有“已选”课程）
        if (hasTimeConflict(studentId, lessonId)) {
            return Result.fail("与已选课程时间冲突");
        }

        // 5) 调 DAO 执行选课（利用唯一键去重）
        boolean ok = enrollmentDAO.enroll(studentId, lessonId);
        return ok ? Result.ok() : Result.fail("选课失败（数据库执行失败）");
    }

    // ========== 退课 ==========
    public Result drop(String studentId, int lessonId) {
        Lesson lesson = courseDAO.getLessonById(lessonId);
        if (lesson == null) {
            return Result.fail("课程不存在");
        }
        // 退课时间窗：已过选课截止时间(enroll_end)则禁止退课，与选课截止规则一致
        java.util.Date end = lesson.getEnrollEnd();
        if (end != null && new java.util.Date().after(end)) {
            return Result.fail("退课时间已截止（已过选课截止时间）");
        }
        boolean ok = enrollmentDAO.drop(studentId, lessonId);
        return ok ? Result.ok() : Result.fail("退课失败（可能未选该课或数据库执行失败）");
    }

    // ========== 辅助：时间冲突检测 ==========
    /**
     * 判断 candidateLessonId 与该学生“已选课程”是否存在时间冲突
     */
    public boolean hasTimeConflict(String studentId, int candidateLessonId) {
        // 候选课的所有时间片
        List<LessonTime> candTimes = safeList(courseDAO.listLessonTimes(candidateLessonId));
        if (candTimes.isEmpty()) return false; // 没排时间就不判冲突

        // 学生已选课程的时间片
        List<LessonTime> myTimes = new ArrayList<>();

        List<Enrollment> myEnrs = safeList(enrollmentDAO.listByStudent(studentId))
                .stream()
                .filter(e -> "enrolled".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        for (Enrollment e : myEnrs) {
            // 跳过与候选课相同的课（重复选课不算冲突；DAO 层会做去重更新）
            if (Objects.equals(e.getLessonId(), candidateLessonId)) continue;
            myTimes.addAll(safeList(courseDAO.listLessonTimes(e.getLessonId())));
        }

        // 在同一天且时间段相交即冲突
        for (LessonTime a : candTimes) {
            for (LessonTime b : myTimes) {
                if (a.getDayOfWeek() == b.getDayOfWeek()
                        && overlap(a.getStartSec(), a.getEndSec(), b.getStartSec(), b.getEndSec())) {
                    return true;
                }
            }
        }
        return false;
    }

    // 区间是否相交（闭区间）
    private boolean overlap(int s1, int e1, int s2, int e2) {
        return !(e1 < s2 || e2 < s1);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    // ========== 统一返回结构 ==========
    // TODO: 可以用common.net.Message处理
    public static class Result {
        public final boolean ok;
        public final String msg;

        private Result(boolean ok, String msg) {
            this.ok = ok;
            this.msg = msg;
        }
        public static Result ok() { return new Result(true, "OK"); }
        public static Result fail(String msg) { return new Result(false, msg); }

        @Override public String toString() { return (ok ? "OK" : "FAIL") + (msg == null ? "" : (": " + msg)); }
    }

    
    // ========== 辅助校验 ==========
    private String validateCourse(Course c) {
        if (c == null) return "课程对象为空";
        if (isBlank(c.getCourseCode())) return "课程代码不能为空";
        if (isBlank(c.getCourseName())) return "课程名称不能为空";
        if (c.getCredit() < 0) return "学分不能为负数";
        return null;
    }

    private String validateLesson(Lesson l, boolean isUpdate) {
        if (l == null) return "开课对象为空";
        if (!isUpdate && l.getLessonId() > 0) return "新增时不应携带 lessonId";
        if (l.getCourseId() <= 0) return "关联课程ID非法";
        if (l.getCapacity() < 0) return "容量不能为负数";
        if (l.getEnrollStart() != null && l.getEnrollEnd() != null
                && l.getEnrollStart().after(l.getEnrollEnd())) {
            return "选课开始时间不能晚于结束时间";
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
