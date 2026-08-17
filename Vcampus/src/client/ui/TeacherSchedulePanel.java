package client.ui;

import client.controller.CourseController;
import common.model.Course;
import common.model.Enrollment;
import common.model.Lesson;
import common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeacherSchedulePanel extends JPanel {
    private final User user;
    private final CourseController controller = new CourseController();

    public TeacherSchedulePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        List<Lesson> allLessons = controller.listLessons();
        List<Lesson> myLessons = allLessons.stream()
                .filter(l -> user.getUserId().equals(l.getTeacherId()))
                .collect(Collectors.toList());
        // 批量预加载课程，消除逐课 getCourseById 的 N+1
        Map<Integer, Course> courseMap = new HashMap<>();
        for (Course c : controller.listCourses()) courseMap.put(c.getCourseId(), c);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        for (Lesson lesson : myLessons) {
            Course course = courseMap.get(lesson.getCourseId());
            String courseName = course != null ? course.getCourseName() : String.valueOf(lesson.getCourseId());

            JPanel lessonPanel = new JPanel(new BorderLayout());
            lessonPanel.setBorder(BorderFactory.createTitledBorder("课程：" + courseName + "（开课ID: " + lesson.getLessonId() + "）"));

            // 课程基本信息
            JTextArea infoArea = new JTextArea(
                    "教室: " + lesson.getClassroom() + "\n" +
                    "容量: " + lesson.getCapacity() + "\n" +
                    "备注: " + lesson.getRemark()
            );
            infoArea.setEditable(false);
            lessonPanel.add(infoArea, BorderLayout.NORTH);

            // 学生名单
            List<Enrollment> enrollments = controller.listEnrollmentsByLesson(lesson.getLessonId());
            DefaultListModel<String> studentListModel = new DefaultListModel<>();
            for (Enrollment e : enrollments) {
                studentListModel.addElement("学生ID: " + e.getStudentId() + " 状态: " + e.getStatus());
            }
            JList<String> studentList = new JList<>(studentListModel);
            lessonPanel.add(new JScrollPane(studentList), BorderLayout.CENTER);

            mainPanel.add(lessonPanel);
            mainPanel.add(Box.createVerticalStrut(10));
        }

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }
}