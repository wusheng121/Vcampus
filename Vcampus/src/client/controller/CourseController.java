package client.controller;
import common.net.MessageType;

import client.net.ClientSocket;
import common.model.*;
import common.net.Message;

import java.util.Collections;
import java.util.List;

public class CourseController {
    private final ClientSocket socket = ClientSocket.getInstance();

    public List<Teacher> listTeachers() {
        try {
            Message req = new Message(MessageType.LIST_TEACHERS, null);
            Message resp = socket.sendRequest(req);
            return (List<Teacher>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    public Teacher getTeacherById(String id) {
        try {
            Message req = new Message(MessageType.GET_TEACHER_BY_ID, id);
            Message resp = socket.sendRequest(req);
            return (Teacher) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }    
    
    
    public List<Lesson> listLessons() {
        try {
            Message req = new Message(MessageType.LIST_LESSONS, null);
            Message resp = socket.sendRequest(req);
            return (List<Lesson>) resp.getData();            
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    public List<LessonTime> listLessonTimes(int lessonId) {
        try {
            Message req = new Message(MessageType.LIST_LESSON_TIMES, lessonId);
            Message resp = socket.sendRequest(req);
            return (List<LessonTime>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** 批量：一次返回全部上课时间，消除逐课 N+1。 */
    @SuppressWarnings("unchecked")
    public List<LessonTime> listAllLessonTimes() {
        try {
            Message req = new Message(MessageType.LIST_ALL_LESSON_TIMES, null);
            Message resp = socket.sendRequest(req);
            Object data = resp.getData();
            return data instanceof List ? (List<LessonTime>) data : Collections.emptyList();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** 批量：所有 lesson 的已选人数（lessonId -> count），消除逐课 N+1。 */
    @SuppressWarnings("unchecked")
    public java.util.Map<Integer, Integer> countEnrolledForAll() {
        try {
            Message req = new Message(MessageType.COUNT_ENROLLED_ALL, null);
            Message resp = socket.sendRequest(req);
            Object data = resp.getData();
            return data instanceof java.util.Map ? (java.util.Map<Integer, Integer>) data : java.util.Collections.emptyMap();
        } catch(Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyMap();
        }
    }
    
    public Course getCourseById(int courseId){
        try {
            Message req = new Message(MessageType.GET_COURSE_BY_ID, courseId);
            Message resp = socket.sendRequest(req);
            return (Course) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return new Course();
        }
    }
    public Lesson getLessonById(int lessonId) {
        try {
            Message req = new Message(MessageType.GET_LESSON_BY_ID, lessonId);
            Message resp = socket.sendRequest(req);
            return (Lesson) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return new Lesson();
        }
    }
    public List<Course> listCourses() {
        try {
            Message req = new Message(MessageType.LIST_COURSES, null);
            Message resp = socket.sendRequest(req);
            return (List<Course>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public Message enroll(String studentId, int lessonId) {
        try {
            Message req = new Message(MessageType.ENROLL, new Object[]{studentId, lessonId});
            return socket.sendRequest(req);
        } catch(Exception e) {
            e.printStackTrace();
            Message error = new Message();
            error.setStatus("fail");
            error.setMsg("无法连接服务器(选课): " + e.getMessage());
            return error;            
        }
    }

    public Message drop(String studentId, int lessonId) {
        try {
        Message req = new Message(MessageType.DROP, new Object[]{studentId, lessonId});
        return socket.sendRequest(req);
        } catch(Exception e) {
            e.printStackTrace();
            Message error = new Message();
            error.setStatus("fail");
            error.setMsg("无法连接服务器(退课): " + e.getMessage());
            return error;
        }
    }

    public List<Enrollment> listMyEnrollments(String studentId) {
        try {
        Message req = new Message(MessageType.LIST_MY_ENROLLMENTS, studentId);
        Message resp = socket.sendRequest(req);
        return (List<Enrollment>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ===== Course CRUD =====
    public Message createCourse(Course course) {
        try {
            Message req = new Message(MessageType.CREATE_COURSE, course);
            Message resp = socket.sendRequest(req);
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))
                    && resp.getData() instanceof Course) {
                Course created = (Course) resp.getData();
                course.setCourseId(created.getCourseId()); // 回填新ID
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(新增课程): " + e.getMessage());
            return err;
        }
    }

    public Message updateCourse(Course course) {
        try {
            Message req = new Message(MessageType.UPDATE_COURSE, course);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(更新课程): " + e.getMessage());
            return err;
        }
    }

    public Message deleteCourse(int courseId) {
        try {
            Message req = new Message(MessageType.DELETE_COURSE, courseId);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(删除课程): " + e.getMessage());
            return err;
        }
    }

    // ===== Lesson CRUD =====
    public Message createLesson(Lesson lesson) {
        try {
            Message req = new Message(MessageType.CREATE_LESSON, lesson);
            Message resp = socket.sendRequest(req);
            if ("success".equalsIgnoreCase(String.valueOf(resp.getStatus()))
                    && resp.getData() instanceof Lesson) {
                Lesson created = (Lesson) resp.getData();
                lesson.setLessonId(created.getLessonId()); // 回填新ID
            }
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(新增开课): " + e.getMessage());
            return err;
        }
    }

    public Message updateLesson(Lesson lesson) {
        try {
            Message req = new Message(MessageType.UPDATE_LESSON, lesson);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(更新开课): " + e.getMessage());
            return err;
        }
    }

    public Message deleteLesson(int lessonId) {
        try {
            Message req = new Message(MessageType.DELETE_LESSON, lessonId);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message();
            err.setStatus("fail");
            err.setMsg("无法连接服务器(删除开课): " + e.getMessage());
            return err;
        }
    }
    // 选课人数
    public int countEnrolled(int lessonId) {
        try {
            Message req = new Message(MessageType.COUNT_ENROLLED, lessonId);
            Message resp = socket.sendRequest(req);
            Object data = resp.getData();
            if (data instanceof Integer) return (Integer) data;
            if (data != null) return Integer.parseInt(String.valueOf(data));
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    // 在 CourseController 中新增以下三个方法
    public Message createLessonTime(common.model.LessonTime t) {
        try {
            Message req = new Message(MessageType.CREATE_LESSON_TIME, t);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(新增上课时间): " + e.getMessage());
            return err;
        }
    }
    public Message updateLessonTime(common.model.LessonTime t) {
        try {
            Message req = new Message(MessageType.UPDATE_LESSON_TIME, t);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(更新上课时间): " + e.getMessage());
            return err;
        }
    }
    public Message deleteLessonTime(int timeId) {
        try {
            Message req = new Message(MessageType.DELETE_LESSON_TIME, timeId);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(删除上课时间): " + e.getMessage());
            return err;
        }
    }
    public List<Enrollment> listEnrollmentsByLesson(int lessonId) {
        try {
            Message req = new Message(MessageType.LIST_ENROLLMENTS_BY_LESSON, lessonId);
            Message resp = socket.sendRequest(req);
            return (List<Enrollment>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }    
}