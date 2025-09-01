package client.ui;

import common.model.User;

import javax.swing.*;

public class adminFrame extends MainFrame {
    public adminFrame(User user) {
        super(user);
    }

    @Override
    protected void addModuleMenu(JMenu systemMenu) {
        JMenuItem manageStudents = new JMenuItem("学籍管理");
        manageStudents.addActionListener(e -> showMessage("进入学籍管理模块"));

        JMenuItem manageCourses = new JMenuItem("选课系统");
        manageCourses.addActionListener(e -> showMessage("进入选课系统模块"));

        JMenuItem library = new JMenuItem("图书馆");
        library.addActionListener(e -> showMessage("进入图书馆模块"));

        JMenuItem shop = new JMenuItem("商店");
        shop.addActionListener(e -> showMessage("进入商店模块"));

        systemMenu.add(manageStudents);
        systemMenu.add(manageCourses);
        systemMenu.add(library);
        systemMenu.add(shop);
    }
}
