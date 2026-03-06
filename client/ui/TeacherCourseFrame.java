package client.ui;

import common.model.User;

import javax.swing.*;
import java.awt.*;

// 教师选课系统主页UI
public class TeacherCourseFrame extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private TeacherSchedulePanel teacherSchedulePanel;
    private User user;

    public TeacherCourseFrame(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // 主内容页面
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        teacherSchedulePanel = new TeacherSchedulePanel(user);
        cardPanel.add(teacherSchedulePanel, "teacherSchedule");

        add(cardPanel, BorderLayout.CENTER);

    }
}

