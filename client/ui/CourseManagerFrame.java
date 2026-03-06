/**
 * 选课系统-管理员端主页 (改为 JPanel 形式)
 */
package client.ui;

import common.model.User;

import javax.swing.*;
import java.awt.*;

public class CourseManagerFrame extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User user;
    private LessonManagePanel lessonManagePanel;
    private AdminCoursePanel adminCoursePanel;

    public CourseManagerFrame(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // 顶部栏：直接显示两个按钮（开放选课 / 课程）
        JMenuBar menuBar = new JMenuBar();
        JButton openCourseBtn = new JButton("选课系统");
        JButton adminCourseBtn = new JButton("课程管理");
        menuBar.add(openCourseBtn);
        menuBar.add(Box.createHorizontalStrut(12)); // 小间距
        menuBar.add(adminCourseBtn);
        add(menuBar, BorderLayout.NORTH);    

        // 主内容页面
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        lessonManagePanel = new LessonManagePanel();
        adminCoursePanel = new AdminCoursePanel();

        cardPanel.add(lessonManagePanel, "lessonManage");
        cardPanel.add(adminCoursePanel, "adminCourse");
        add(cardPanel, BorderLayout.CENTER);

        // 顶部按钮切换事件
        openCourseBtn.addActionListener(e -> cardLayout.show(cardPanel, "lessonManage"));
        adminCourseBtn.addActionListener(e -> cardLayout.show(cardPanel, "adminCourse"));
    }

    /** 如果还需要单独运行，可以用外层 JFrame 包裹 */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("虚拟校园 - 选课系统(管理员)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new CourseManagerFrame(new User()));
            frame.setVisible(true);
        });
    }
}
