package client.ui;

import common.model.User;

import javax.swing.*;

public class adminFrame extends MainFrame {
    public adminFrame(User user) {
        super(user);

//        mainPanel.add(new LibraryFrame(user), "图书馆");
//        mainPanel.add(new UserManagePanel(user), "用户管理");
    }

    @Override
    protected void addModuleButtons() {
        registerModule("图书馆", () -> new LibraryFrame(user), "/pictures/图书馆.png");
        registerModule("用户管理", () -> new UserManagePanel(user), "/pictures/用户管理.png");
        registerModule("学籍管理", () -> new StudentMgrDialog(), "/pictures/学籍管理.png");
        registerModule("选课系统", () -> new CourseManagerFrame(user), "/pictures/排课.png");
        registerModule("商店", () -> new AdminShopFrame(user), "/pictures/商店.png");
    }

    @Override
    protected void addModuleMenu(JMenu systemMenu) {
        JMenuItem manageStudents = new JMenuItem("学籍管理");
        manageStudents.addActionListener(e -> {
            showModule("学籍管理");
        });

        JMenuItem manageCourses = new JMenuItem("选课系统");
        manageCourses.addActionListener(e -> {
            showModule("选课系统");
        });

        JMenuItem library = new JMenuItem("图书馆");
        library.addActionListener(e -> {
            showModule("图书馆");  // 直接切换到已注册的 LibraryPanel
        });

        JMenuItem shop = new JMenuItem("商店");
        shop.addActionListener(e -> {
            showModule("商店");
        });

        JMenuItem user1 = new JMenuItem("用户管理");
        user1.addActionListener(e -> {
            showModule("用户管理"); // 直接切换到已注册的 UserManagePanel
        });

        systemMenu.add(manageStudents);
        systemMenu.add(manageCourses);
        systemMenu.add(library);
        systemMenu.add(shop);
        systemMenu.add(user1);
    }

    protected void showPersonalInfo() {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "未能加载用户信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        AdminInfoPanel panel = new AdminInfoPanel(user, this::backToMain);
        mainPanel.add(panel, "ADMIN_INFO");
        cardLayout.show(mainPanel, "ADMIN_INFO");
    }

    private void backToMain() {
        cardLayout.show(mainPanel, "MAIN");
    }
}
