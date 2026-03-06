package client.ui;

import common.model.User;
import util.FloatingAgentButton;

import javax.swing.*;
import java.awt.*;

// 学生选课系统主页UI (改为 JPanel 形式)
public class CourseSelectionFrame extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User user;
    private TimetablePanel timetablePanel; // 课表功能panel
    private SelectCoursePanel selectCoursePanel; // 选课功能panel
    private AgentPanel agentPanel; // TODO:测试
    private FloatingAgentButton.Handle fab;  // 悬浮按钮句柄（用来控制显隐）

    public CourseSelectionFrame(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // 顶部栏：放两個按鈕（选课 / 课表）
        JMenuBar menuBar = new JMenuBar();
        JButton btnSelect = new JButton("选课");
        JButton btnTimetable = new JButton("课表");
        // JButton btnAgent = new JButton("智能客服(测试用)");
        menuBar.add(btnSelect);
        menuBar.add(Box.createHorizontalStrut(12));
        menuBar.add(btnTimetable);
        // menuBar.add(Box.createHorizontalStrut(12));
        // menuBar.add(btnAgent);
        add(menuBar, BorderLayout.NORTH);

        // 主内容页面
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 这里用两个独立的JPanel类
        timetablePanel = new TimetablePanel(user); // 必须先于下方
        selectCoursePanel = new SelectCoursePanel(user, timetablePanel::refresh);
        agentPanel = new AgentPanel(user); // TODO:测试

        cardPanel.add(selectCoursePanel, "selectCourse");
        cardPanel.add(timetablePanel, "timetable");
        cardPanel.add(agentPanel,"agent"); // TODO : 测试
        
        // 右下角机器人图标（点击切到 agent）—— 用 attach，而不是 wrap
        Icon botIcon = loadBotIcon(); // 加载不到会用“🤖”兜底
        fab = FloatingAgentButton.attach(
                cardPanel,
                () -> {                         // 点击动作
                    cardLayout.show(cardPanel, "agent");
                    fab.button.setVisible(false); // 进客服后隐藏按钮
                },
                botIcon,
                100,   // 按钮尺寸
                16    // 边距
        );
        add(fab.layeredPane, BorderLayout.CENTER);  // 把分层面板加到中心        
        
        // 頂部按鈕切換事件
        // btnSelect.addActionListener(e -> cardLayout.show(cardPanel, "selectCourse"));
        // btnTimetable.addActionListener(e -> cardLayout.show(cardPanel, "timetable"));
        btnSelect.addActionListener(e -> {
            cardLayout.show(cardPanel, "selectCourse");
            if (fab != null) fab.button.setVisible(true);   // 选课页显示按钮
        });
        btnTimetable.addActionListener(e -> {
            cardLayout.show(cardPanel, "timetable");
            if (fab != null) fab.button.setVisible(true);   // 课表页显示按钮
        });

        // 初始显示选课页时，确保按钮可见
        if (fab != null) fab.button.setVisible(true);        
        // btnAgent.addActionListener(e -> cardLayout.show(cardPanel, "agent"));
    }

    /** 尝试从资源路径加载机器人图标，可换自己的路径；加载失败返回 null（将使用“🤖”文本兜底） */
    private Icon loadBotIcon() {
        // 1) 先从类路径找（建议把图片放到 resources 下，例如 /icons/bot.png）
        java.net.URL url = getClass().getResource("/pictures/robot.png");
        if (url != null) return new ImageIcon(url);
        return null;
    }    

    // 独立运行时外层 JFrame 包裹
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("虚拟校园 - 选课系统(学生)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new CourseSelectionFrame(new User()));
            frame.setVisible(true);
        });
    }
}
