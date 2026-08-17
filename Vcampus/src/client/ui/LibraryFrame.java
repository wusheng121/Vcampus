package client.ui;
import util.UITheme;

import common.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;

public class LibraryFrame extends JPanel {
    private final User user;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private HashMap<String, JPanel> moduleMap = new HashMap<>();

    public LibraryFrame(User user) {
        this.user=user;
//        setTitle("虚拟校园 - 图书馆");
//        setSize(800, 600);
//        setLocationRelativeTo(null);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 顶部功能栏
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        String[] modules;
        if ("admin".equals(user.getType())) {
            modules = new String[]{"书目检索", "分类浏览", "期刊导航", "我的图书馆", "书籍管理", "期刊管理", "借阅记录"};
        } else {
            modules = new String[]{"书目检索", "分类浏览", "期刊导航", "我的图书馆"};
        }

        for (String name : modules) {
            JButton btn = new JButton(name);
            btn.setBackground(UITheme.PRIMARY);
            btn.setForeground(new Color(255, 255, 255));
            btn.addActionListener(e -> showModule(name));
            btnPanel.add(btn);
        }

        // 登出按钮
//        JButton logoutBtn = new JButton("登出");
//        logoutBtn.addActionListener(e -> logout());
//        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        rightPanel.add(logoutBtn);

        topPanel.add(btnPanel, BorderLayout.WEST);
//        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 主内容区
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // 添加模块
        for (String name : modules) {
//            JPanel panel;
//            if ("我的图书馆".equals(name)) {
//                panel = new MyLibraryPanel(user);
//            } else if ("书籍管理".equals(name)) {
//                panel = new BookManagePanel();
//            }else if("书目检索".equals(name)){
//                panel = new BookSearchPanel(user);
//            } else if("借阅记录".equals(name)){
//                panel = new BorrowRecordsPanel();
//            } else if("分类浏览".equals(name)){
//                panel = new CategoryBrowsePanel(user);
//            } else if ("期刊管理".equals(name)) {
//                panel = new JournalManagePanel();
//            } else if ("期刊导航".equals(name)) {
//                panel = new JournalNavigationPanel();
//            }
//            else {
//                panel = new JPanel(new BorderLayout());
//                JLabel label = new JLabel(name + " 模块内容区", JLabel.CENTER);
//                label.setFont(new Font("微软雅黑", Font.BOLD, 28));
//                panel.add(label, BorderLayout.CENTER);
//            }
//            moduleMap.put(name, panel);
            JPanel panel = new JPanel();
            contentPanel.add(panel, name);
        }
        add(contentPanel, BorderLayout.CENTER);

        // ===== 默认显示第一个模块 =====
        showModule(modules[0]);
    }

//    private void showModule(String name) {
//        cardLayout.show(contentPanel, name);
//    }
// 切换模块
    /** 切换模块（懒加载真正的 Panel） */
    private void showModule(String name) {
        if (!moduleMap.containsKey(name)) {
            JPanel panel;
            switch (name) {
                case "我的图书馆":
                    panel = new MyLibraryPanel(user);
                    break;
                case "书籍管理":
                    panel = new BookManagePanel();
                    break;
                case "书目检索":
                    panel = new BookSearchPanel(user);
                    break;
                case "借阅记录":
                    panel = new BorrowRecordsPanel();
                    break;
                case "分类浏览":
                    panel = new CategoryBrowsePanel(user);
                    break;
                case "期刊管理":
                    panel = new JournalManagePanel();
                    break;
                case "期刊导航":
                    panel = new JournalNavigationPanel();
                    break;
                default:
                    panel = new JPanel(new BorderLayout());
                    JLabel label = new JLabel(name + " 模块内容区", JLabel.CENTER);
                    label.setFont(new Font("微软雅黑", Font.BOLD, 28));
                    panel.add(label, BorderLayout.CENTER);
            }

            moduleMap.put(name, panel);
            contentPanel.add(panel, name); // 替换掉占位符
        }

        cardLayout.show(contentPanel, name);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

