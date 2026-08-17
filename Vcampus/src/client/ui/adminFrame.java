package client.ui;

import common.model.User;

import javax.swing.*;

public class adminFrame extends MainFrame {
    public adminFrame(User user) {
        super(user);
        buildSidebar();
    }

    @Override
    protected void addModuleButtons() {
        registerModule("图书馆", () -> new LibraryFrame(user), "/pictures/图书馆.png");
        registerModule("用户管理", () -> new UserManagePanel(user), "/pictures/用户管理.png");
        registerModule("学籍管理", () -> new StudentMgrDialog(user), "/pictures/学籍管理.png");
        registerModule("选课系统", () -> new CourseManagerFrame(user), "/pictures/排课.png");
        registerModule("商店", () -> new AdminShopFrame(user), "/pictures/商店.png");
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
        cardLayout.show(mainPanel, "Welcome");
    }
}
