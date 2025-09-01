package client.ui;

import common.model.User;

import javax.swing.*;

public class studentFrame extends MainFrame {
    public studentFrame(User user) {
        super(user);
    }

    @Override
    protected void addModuleMenu(JMenu systemMenu) {
        JMenuItem courseSelect = new JMenuItem("选课系统");
        courseSelect.addActionListener(e -> showMessage("进入选课系统模块"));

        JMenuItem library = new JMenuItem("图书馆");
        library.addActionListener(e -> showMessage("进入图书馆模块"));

        JMenuItem shop = new JMenuItem("商店");
        shop.addActionListener(e -> showMessage("进入商店模块"));

        systemMenu.add(courseSelect);
        systemMenu.add(library);
        systemMenu.add(shop);
    }
}
