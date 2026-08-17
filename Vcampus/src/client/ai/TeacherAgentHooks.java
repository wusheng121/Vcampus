package client.ai;

import client.controller.CourseController;
import client.net.ClientSocket;
import common.model.Course;
import common.model.Lesson;
import common.model.LessonTime;
import common.model.User;
import common.net.Message;
import common.net.MessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 教师课务助理 Hook。
 * 数据集：该教师名下的 lesson（教学班次）+ lesson_times + 对应 courses。
 */
public class TeacherAgentHooks implements AgentHooks {
    private final User user;
    private final CourseController controller = new CourseController();

    private String teacherId;
    private List<Lesson> myLessons = Collections.emptyList();
    private List<LessonTime> lessonTimes = new ArrayList<>();
    private List<Course> courses = Collections.emptyList();

    public TeacherAgentHooks(User user) {
        this.user = user;
    }

    @Override
    public String systemPrompt() {
        return AgentPrompts.teacherSystemPrompt();
    }

    @Override
    public void preload() {
        // 1) 反查 teacherId
        teacherId = findTeacherIdByUserId(user.getUserId());

        // 2) 拉全部 lessons/courses，过滤出本教师的班次
        List<Lesson> allLessons = controller.listLessons();
        courses = controller.listCourses();
        if (teacherId != null && allLessons != null) {
            myLessons = new ArrayList<>();
            for (Lesson l : allLessons) {
                if (teacherId.equals(l.getTeacherId())) {
                    myLessons.add(l);
                }
            }
        } else {
            myLessons = Collections.emptyList();
        }

        // 3) 拉这些班次的时间
        lessonTimes = new ArrayList<>();
        for (Lesson l : myLessons) {
            List<LessonTime> ts = controller.listLessonTimes(l.getLessonId());
            if (ts != null) lessonTimes.addAll(ts);
        }
    }

    @Override
    public String buildDataset() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<DATASET>>\n{\n");

        sb.append("  \"me\": {")
          .append("\"user_id\":\"").append(safe(user.getUserId())).append("\",")
          .append("\"teacher_id\":\"").append(teacherId == null ? "" : safe(teacherId)).append("\",")
          .append("\"name\":\"").append(safe(user.getName())).append("\"")
          .append("},\n");

        sb.append("  \"my_lessons\": [\n");
        for (int i = 0; i < myLessons.size(); i++) {
            Lesson l = myLessons.get(i);
            String courseName = courseNameOf(l.getCourseId());
            sb.append("    {\"lesson_id\":").append(l.getLessonId())
              .append(",\"course_id\":").append(l.getCourseId())
              .append(",\"course_name\":\"").append(safe(courseName)).append("\"")
              .append(",\"capacity\":").append(l.getCapacity())
              .append(",\"is_open\":").append(l.isOpen())
              .append(",\"classroom\":\"").append(safe(l.getClassroom())).append("\"")
              .append(",\"remark\":\"").append(safe(l.getRemark())).append("\"}");
            if (i < myLessons.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"lesson_times\": [\n");
        for (int i = 0; i < lessonTimes.size(); i++) {
            LessonTime t = lessonTimes.get(i);
            sb.append("    {\"lesson_id\":").append(t.getLessonId())
              .append(",\"day_of_week\":").append(t.getDayOfWeek())
              .append(",\"start_sec\":").append(t.getStartSec())
              .append(",\"end_sec\":").append(t.getEndSec())
              .append(",\"location\":\"").append(safe(t.getLocation())).append("\"}");
            if (i < lessonTimes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        sb.append("</DATASET>\n");
        return sb.toString();
    }

    @Override
    public String helpText() {
        return """
                我是你的教师课务助理

                可用指令：
                • /help —— 查看帮助
                • /clear —— 清空对话与上下文

                我能做什么：
                • 查询你的教学班次、上课时间、教室
                • 关联课程名称、学分等信息
                """;
    }

    private String courseNameOf(int courseId) {
        if (courses != null) {
            for (Course c : courses) {
                if (c.getCourseId() == courseId) return c.getCourseName();
            }
        }
        return "";
    }

    private String findTeacherIdByUserId(String userId) {
        try {
            Message req = new Message(MessageType.TEACHER_ID_BY_USERID, userId);
            Message resp = ClientSocket.getInstance().sendRequest(req);
            if ("success".equals(resp.getStatus())) {
                return (String) resp.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
