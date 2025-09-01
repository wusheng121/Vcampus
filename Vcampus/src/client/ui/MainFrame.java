package client.ui;

import common.model.User;

import javax.swing.*;
import java.awt.*;

public abstract class MainFrame extends JFrame {
    protected User user;

    public MainFrame(User user) {
        this.user = user;
        setTitle("虚拟校园 - " + user.getType() + " 界面");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 顶部菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 模块菜单
        JMenu systemMenu = new JMenu("功能模块");
        addModuleMenu(systemMenu);  // 子类实现：添加不同身份的功能模块
        menuBar.add(systemMenu);

        // 个人信息
        JMenu personalMenu = new JMenu("个人");
        JMenuItem infoItem = new JMenuItem("个人信息");
        infoItem.addActionListener(e -> showMessage("打开个人信息模块"));
        JMenuItem logoutItem = new JMenuItem("登出");
        logoutItem.addActionListener(e -> logout());
        personalMenu.add(infoItem);
        personalMenu.addSeparator();
        personalMenu.add(logoutItem);
        menuBar.add(personalMenu);

        setJMenuBar(menuBar);

        // 主内容面板
        JLabel welcome = new JLabel("欢迎你, " + user.getUserId() + " (" + user.getType() + ")", JLabel.CENTER);
        welcome.setFont(new Font("微软雅黑", Font.BOLD, 24));
        add(welcome, BorderLayout.CENTER);
    }

    protected abstract void addModuleMenu(JMenu systemMenu);

    protected void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void logout() {
        dispose();
        new LoginFrame().setVisible(true); // 返回登录界面
    }
}
