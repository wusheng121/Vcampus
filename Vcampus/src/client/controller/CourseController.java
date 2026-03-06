package client.controller;

import client.net.ClientSocket;
import common.model.*;
import common.net.Message;

import java.util.Collections;
import java.util.List;

public class CourseController {
    private final ClientSocket socket = new ClientSocket();

    public List<Teacher> listTeachers() {
        try {
            Message req = new Message("listTeachers", null);
            Message resp = socket.sendRequest(req);
            return (List<Teacher>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    public Teacher getTeacherById(String id) {
        try {
            Message req = new Message("getTeacherById", id);
            Message resp = socket.sendRequest(req);
            return (Teacher) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }    
    
    
    public List<Lesson> listLessons() {
        try {
            Message req = new Message("listLessons", null);
            Message resp = socket.sendRequest(req);
            return (List<Lesson>) resp.getData();            
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    public List<LessonTime> listLessonTimes(int lessonId) {
        try {
            Message req = new Message("listLessonTimes", lessonId);
            Message resp = socket.sendRequest(req);
            return (List<LessonTime>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public Course getCourseById(int courseId){
        try {
            Message req = new Message("getCourseById", courseId);
            Message resp = socket.sendRequest(req);
            return (Course) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return new Course();
        }
    }
    public Lesson getLessonById(int lessonId) {
        try {
            Message req = new Message("getLessonById", lessonId);
            Message resp = socket.sendRequest(req);
            return (Lesson) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return new Lesson();
        }
    }
    public List<Course> listCourses() {
        try {
            Message req = new Message("listCourses", null);
            Message resp = socket.sendRequest(req);
            return (List<Course>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public Message enroll(String studentId, int lessonId) {
        try {
            Message req = new Message("enroll", new Object[]{studentId, lessonId});
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
        Message req = new Message("drop", new Object[]{studentId, lessonId});
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
        Message req = new Message("listMyEnrollments", studentId);
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
            Message req = new Message("createCourse", course);
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
            Message req = new Message("updateCourse", course);
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
            Message req = new Message("deleteCourse", courseId);
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
            Message req = new Message("createLesson", lesson);
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
            Message req = new Message("updateLesson", lesson);
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
            Message req = new Message("deleteLesson", lessonId);
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
            Message req = new Message("countEnrolled", lessonId);
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
            Message req = new Message("createLessonTime", t);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(新增上课时间): " + e.getMessage());
            return err;
        }
    }
    public Message updateLessonTime(common.model.LessonTime t) {
        try {
            Message req = new Message("updateLessonTime", t);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(更新上课时间): " + e.getMessage());
            return err;
        }
    }
    public Message deleteLessonTime(int timeId) {
        try {
            Message req = new Message("deleteLessonTime", timeId);
            return socket.sendRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            Message err = new Message(); err.setStatus("fail"); err.setMsg("无法连接服务器(删除上课时间): " + e.getMessage());
            return err;
        }
    }
    public List<Enrollment> listEnrollmentsByLesson(int lessonId) {
        try {
            Message req = new Message("listEnrollmentsByLesson", lessonId);
            Message resp = socket.sendRequest(req);
            return (List<Enrollment>) resp.getData();
        } catch(Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }    
}