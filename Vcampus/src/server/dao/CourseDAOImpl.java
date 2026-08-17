/**
 * 开放访问lesson & course DB的DAO
 */
package server.dao;

import common.model.Course;
import common.model.Lesson;
import common.model.LessonTime;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDAOImpl implements CourseDAO {

    // ---------- course ----------
    @Override
    public List<Course> listCourses() {
        String sql = "SELECT course_id, course_code, course_name, credit, description FROM course";
        List<Course> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCourseCode(rs.getString("course_code"));
                c.setCourseName(rs.getString("course_name"));
                c.setCredit(rs.getDouble("credit"));
                c.setDescription(rs.getString("description"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Course getCourseById(int courseId) {
        String sql = "SELECT course_id, course_code, course_name, credit, description FROM course WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Course c = new Course();
                    c.setCourseId(rs.getInt("course_id"));
                    c.setCourseCode(rs.getString("course_code"));
                    c.setCourseName(rs.getString("course_name"));
                    c.setCredit(rs.getDouble("credit"));
                    c.setDescription(rs.getString("description"));
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- lesson ----------
    @Override
    public List<Lesson> listLessons() {
        String sql = "SELECT lesson_id, course_id, capacity, is_open, enroll_start, enroll_end, classroom, remark, teacher_id FROM lesson";
        List<Lesson> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLesson(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Lesson getLessonById(int lessonId) {
        String sql = "SELECT lesson_id, course_id, capacity, is_open, enroll_start, enroll_end, classroom, remark, teacher_id FROM lesson WHERE lesson_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapLesson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- lesson_time ----------
    @Override
    public List<LessonTime> listLessonTimes(int lessonId) {
        String sql = "SELECT time_id, lesson_id, day_of_week, start_sec, end_sec, location " +
                     "FROM lesson_time WHERE lesson_id = ? ORDER BY day_of_week, start_sec";
        List<LessonTime> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LessonTime t = new LessonTime();
                    t.setTimeId(rs.getInt("time_id"));
                    t.setLessonId(rs.getInt("lesson_id"));
                    t.setDayOfWeek(rs.getInt("day_of_week"));
                    t.setStartSec(rs.getInt("start_sec"));
                    t.setEndSec(rs.getInt("end_sec"));
                    t.setLocation(rs.getString("location"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<LessonTime> listAllLessonTimes() {
        String sql = "SELECT time_id, lesson_id, day_of_week, start_sec, end_sec, location " +
                     "FROM lesson_time ORDER BY lesson_id, day_of_week, start_sec";
        List<LessonTime> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LessonTime t = new LessonTime();
                t.setTimeId(rs.getInt("time_id"));
                t.setLessonId(rs.getInt("lesson_id"));
                t.setDayOfWeek(rs.getInt("day_of_week"));
                t.setStartSec(rs.getInt("start_sec"));
                t.setEndSec(rs.getInt("end_sec"));
                t.setLocation(rs.getString("location"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Map<Integer, Integer> countEnrolledForAll() {
        String sql = "SELECT lesson_id, COUNT(*) AS cnt FROM enrollment " +
                     "WHERE status = 'enrolled' GROUP BY lesson_id";
        Map<Integer, Integer> map = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("lesson_id"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // ---------- helpers ----------
    private Lesson mapLesson(ResultSet rs) throws SQLException {
        Lesson l = new Lesson();
        l.setLessonId(rs.getInt("lesson_id"));
        l.setCourseId(rs.getInt("course_id"));
        l.setCapacity(rs.getInt("capacity"));
        l.setOpen(rs.getBoolean("is_open"));
        Timestamp st = rs.getTimestamp("enroll_start");
        Timestamp et = rs.getTimestamp("enroll_end");
        l.setEnrollStart(st == null ? null : new java.util.Date(st.getTime()));
        l.setEnrollEnd(et == null ? null : new java.util.Date(et.getTime()));
        l.setClassroom(rs.getString("classroom"));
        l.setRemark(rs.getString("remark"));
        l.setTeacherId(rs.getString("teacher_id")); // 新增
        return l;
    }

    // ---------- 实现CRUD -----------
    // 添加Course
    @Override
    public int addCourse(Course course) { // 回传course_id
        // 不填course_id
        String sql = "INSERT INTO course (course_code, course_name, credit, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getDescription());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    course.setCourseId(id); // 回填
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // 更新Course
    @Override
    public boolean updateCourse(Course course) {
        String sql = "UPDATE course SET course_code = ?, course_name = ?, credit = ?, description = ? WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setDouble(3, course.getCredit());
            ps.setString(4, course.getDescription());
            ps.setInt(5, course.getCourseId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 删除Course
    @Override
    public boolean deleteCourse(int courseId) {
        // 注意：若有外键约束(lesson.course_id -> course.course_id)，
        // 需要确保 ON DELETE CASCADE，或先删子表（见文末备注）。
        String sql = "DELETE FROM course WHERE course_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }    

    // 添加Lesson
    @Override
    public int addLesson(Lesson lesson) {
        String sql = "INSERT INTO lesson (course_id, capacity, is_open, enroll_start, enroll_end, classroom, remark, teacher_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, lesson.getCourseId());
            ps.setInt(2, lesson.getCapacity());
            ps.setBoolean(3, lesson.isOpen());

            if (lesson.getEnrollStart() != null) {
                ps.setTimestamp(4, new Timestamp(lesson.getEnrollStart().getTime()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            if (lesson.getEnrollEnd() != null) {
                ps.setTimestamp(5, new Timestamp(lesson.getEnrollEnd().getTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setString(6, lesson.getClassroom());
            ps.setString(7, lesson.getRemark());
            ps.setString(8, lesson.getTeacherId());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    lesson.setLessonId(id); // 回填
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // 更新Lesson
    @Override
    public boolean updateLesson(Lesson lesson) {
        String sql = "UPDATE lesson SET course_id = ?, capacity = ?, is_open = ?, enroll_start = ?, enroll_end = ?, classroom = ?, remark = ?, teacher_id = ? " +
                     "WHERE lesson_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lesson.getCourseId());
            ps.setInt(2, lesson.getCapacity());
            ps.setBoolean(3, lesson.isOpen());

            if (lesson.getEnrollStart() != null) {
                ps.setTimestamp(4, new Timestamp(lesson.getEnrollStart().getTime()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            if (lesson.getEnrollEnd() != null) {
                ps.setTimestamp(5, new Timestamp(lesson.getEnrollEnd().getTime()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            ps.setString(6, lesson.getClassroom());
            ps.setString(7, lesson.getRemark());
            ps.setString(8, lesson.getTeacherId());
            ps.setInt(9, lesson.getLessonId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 删除Lesson
    @Override
    public boolean deleteLesson(int lessonId) {
        String sql = "DELETE FROM lesson WHERE lesson_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lessonId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

// === 新增 LessonTime CRUD 实现 ===
    @Override
    public int addLessonTime(LessonTime t) {
        String sql = "INSERT INTO lesson_time (lesson_id, day_of_week, start_sec, end_sec, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getLessonId());
            ps.setInt(2, t.getDayOfWeek());
            ps.setInt(3, t.getStartSec());
            ps.setInt(4, t.getEndSec());
            ps.setString(5, t.getLocation());
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    t.setTimeId(id); // 回填
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean updateLessonTime(LessonTime t) {
        String sql = "UPDATE lesson_time SET lesson_id=?, day_of_week=?, start_sec=?, end_sec=?, location=? WHERE time_id=?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getLessonId());
            ps.setInt(2, t.getDayOfWeek());
            ps.setInt(3, t.getStartSec());
            ps.setInt(4, t.getEndSec());
            ps.setString(5, t.getLocation());
            ps.setInt(6, t.getTimeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteLessonTime(int timeId) {
        String sql = "DELETE FROM lesson_time WHERE time_id=?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, timeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
