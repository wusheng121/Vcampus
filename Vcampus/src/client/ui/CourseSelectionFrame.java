package client.ui;

import common.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 学生选课系统主页（JPanel 形式，作为 MainFrame 的一个模块）。
 * 含"选课"与"课表"两页；AI 助理由 MainFrame 的全局浮动按钮统一提供。
 */
public class CourseSelectionFrame extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User user;
    private TimetablePanel timetablePanel; // 课表功能panel
    private SelectCoursePanel selectCoursePanel; // 选课功能panel

    public CourseSelectionFrame(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // 顶部栏：选课 / 课表
        JMenuBar menuBar = new JMenuBar();
        JButton btnSelect = new JButton("选课");
        JButton btnTimetable = new JButton("课表");
        menuBar.add(btnSelect);
        menuBar.add(Box.createHorizontalStrut(12));
        menuBar.add(btnTimetable);
        add(menuBar, BorderLayout.NORTH);

        // 主内容页面
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // timetablePanel 必须先于 selectCoursePanel 构造（后者依赖其 refresh 回调）
        timetablePanel = new TimetablePanel(user);
        selectCoursePanel = new SelectCoursePanel(user, timetablePanel::refresh);

        cardPanel.add(selectCoursePanel, "selectCourse");
        cardPanel.add(timetablePanel, "timetable");
        add(cardPanel, BorderLayout.CENTER);

        btnSelect.addActionListener(e -> cardLayout.show(cardPanel, "selectCourse"));
        btnTimetable.addActionListener(e -> cardLayout.show(cardPanel, "timetable"));
    }
}
