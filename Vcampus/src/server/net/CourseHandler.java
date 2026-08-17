package server.net;

import common.model.Course;
import common.model.Enrollment;
import common.model.Lesson;
import common.model.LessonTime;
import common.model.Teacher;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.service.AuthService;
import server.service.CourseService;

import java.util.List;

/**
 * 课程 / 排课 / 选课域
 */
public class CourseHandler implements Handler {
    private final CourseService courseService = new CourseService();
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
            case MessageType.LIST_LESSONS -> {
                List<Lesson> lessons = courseService.listLessons();
                response.setStatus("success");
                response.setData(lessons);
            }
            case MessageType.LIST_LESSON_TIMES -> {
                int lessonId = (int) request.getData();
                List<LessonTime> times = courseService.listLessonTimes(lessonId);
                response.setStatus("success");
                response.setData(times);
            }
            case MessageType.LIST_ALL_LESSON_TIMES -> {
                response.setStatus("success");
                response.setData(courseService.listAllLessonTimes());
            }
            case MessageType.COUNT_ENROLLED_ALL -> {
                response.setStatus("success");
                response.setData(courseService.countEnrolledForAll());
            }
            case MessageType.ENROLL -> {
                if (denied(request, response, Permissions.ENROLLMENT_ENROLL)) break;
                Object[] arr = (Object[]) request.getData();
                String studentId = (String) arr[0];
                int lessonId = (int) arr[1];
                CourseService.Result result = courseService.enroll(studentId, lessonId);
                response.setStatus(result.ok ? "success" : "fail");
                response.setMsg(result.msg);
            }
            case MessageType.DROP -> {
                if (denied(request, response, Permissions.ENROLLMENT_DROP)) break;
                Object[] arr = (Object[]) request.getData();
                String studentId = (String) arr[0];
                int lessonId = (int) arr[1];
                CourseService.Result result = courseService.drop(studentId, lessonId);
                response.setStatus(result.ok ? "success" : "fail");
                response.setMsg(result.msg);
            }
            case MessageType.LIST_MY_ENROLLMENTS -> {
                String studentId = (String) request.getData();
                List<Enrollment> enrollments = courseService.listEnrollmentsByStudent(studentId);
                response.setStatus("success");
                response.setData(enrollments);
            }
            case MessageType.GET_COURSE_BY_ID -> {
                int courseId = (int) request.getData();
                Course course = courseService.getCourse(courseId);
                response.setStatus("success");
                response.setData(course);
            }
            case MessageType.GET_LESSON_BY_ID -> {
                int lessonId = (int) request.getData();
                Lesson lesson = courseService.getLesson(lessonId);
                response.setStatus("success");
                response.setData(lesson);
            }
            case MessageType.LIST_COURSES -> {
                List<Course> courses = courseService.listCourses();
                response.setStatus("success");
                response.setData(courses);
            }
            case MessageType.CREATE_COURSE -> {
                if (denied(request, response, Permissions.COURSE_CREATE)) break;
                Course c = (Course) request.getData();
                CourseService.Result r = courseService.createCourse(c);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
                if (r.ok) {
                    response.setData(c);
                }
            }
            case MessageType.UPDATE_COURSE -> {
                if (denied(request, response, Permissions.COURSE_UPDATE)) break;
                Course c = (Course) request.getData();
                CourseService.Result r = courseService.updateCourse(c);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.DELETE_COURSE -> {
                if (denied(request, response, Permissions.COURSE_DELETE)) break;
                int courseId = (int) request.getData();
                CourseService.Result r = courseService.deleteCourse(courseId);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.CREATE_LESSON -> {
                if (denied(request, response, Permissions.LESSON_CREATE)) break;
                Lesson l = (Lesson) request.getData();
                CourseService.Result r = courseService.createLesson(l);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
                if (r.ok) {
                    response.setData(l);
                }
            }
            case MessageType.UPDATE_LESSON -> {
                if (denied(request, response, Permissions.LESSON_UPDATE)) break;
                Lesson l = (Lesson) request.getData();
                CourseService.Result r = courseService.updateLesson(l);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.DELETE_LESSON -> {
                if (denied(request, response, Permissions.LESSON_DELETE)) break;
                int lessonId = (int) request.getData();
                CourseService.Result r = courseService.deleteLesson(lessonId);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.COUNT_ENROLLED -> {
                int lessonId = (int) request.getData();
                int n = courseService.countEnrolled(lessonId);
                response.setStatus("success");
                response.setData(n);
            }
            case MessageType.CREATE_LESSON_TIME -> {
                if (denied(request, response, Permissions.LESSONTIME_CREATE)) break;
                LessonTime t = (LessonTime) request.getData();
                CourseService.Result r = courseService.createLessonTime(t);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
                if (r.ok) response.setData(t);
            }
            case MessageType.UPDATE_LESSON_TIME -> {
                if (denied(request, response, Permissions.LESSONTIME_UPDATE)) break;
                LessonTime t = (LessonTime) request.getData();
                CourseService.Result r = courseService.updateLessonTime(t);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.DELETE_LESSON_TIME -> {
                if (denied(request, response, Permissions.LESSONTIME_DELETE)) break;
                int timeId = (int) request.getData();
                CourseService.Result r = courseService.deleteLessonTime(timeId);
                response.setStatus(r.ok ? "success" : "fail");
                response.setMsg(r.msg);
            }
            case MessageType.LIST_TEACHERS -> {
                List<Teacher> teachers = courseService.listTeachers();
                response.setStatus("success");
                response.setData(teachers);
            }
            case MessageType.GET_TEACHER_BY_ID -> {
                String id = (String) request.getData();
                Teacher t = courseService.getTeacherById(id);
                response.setStatus("success");
                response.setData(t);
            }
            case MessageType.LIST_ENROLLMENTS_BY_LESSON -> {
                int lessonId = (int) request.getData();
                List<Enrollment> list = courseService.listEnrollmentsByLesson(lessonId);
                response.setStatus("success");
                response.setData(list);
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
