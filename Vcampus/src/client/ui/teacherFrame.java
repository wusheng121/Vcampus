package client.ui;

import common.model.User;

import javax.swing.*;

public class teacherFrame extends MainFrame {
    public teacherFrame(User user) {
        super(user);
    }

    @Override
    protected void addModuleMenu(JMenu systemMenu) {
        JMenuItem courseManage = new JMenuItem("选课系统");
        courseManage.addActionListener(e -> showMessage("进入课程管理模块"));

        JMenuItem library = new JMenuItem("图书馆");
        library.addActionListener(e -> showMessage("进入图书馆模块"));

        JMenuItem shop = new JMenuItem("商店");
        shop.addActionListener(e -> showMessage("进入商店模块"));

        systemMenu.add(courseManage);
        systemMenu.add(library);
        systemMenu.add(shop);
    }
}
