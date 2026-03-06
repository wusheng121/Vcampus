package client.ui;

import client.net.ClientSocket;
import common.model.Student;
import common.model.User;
import common.net.Message;
import common.net.MessageType;

import javax.swing.*;
import java.awt.*;

public class studentFrame extends MainFrame {
      // 原主页
    private StudentInfoPanel infoPanel;

    public studentFrame(User user) {
        super(user);
    }

    @Override
    protected void addModuleButtons() {
        registerModule("图书馆", () -> new LibraryFrame(user), "/pictures/图书馆.png");
        registerModule("选课系统", () -> new CourseSelectionFrame(user), "/pictures/排课.png");
        registerModule("商店", () -> new ShopFrame(user), "/pictures/商店.png");
    }

    @Override
    protected void addModuleMenu(JMenu systemMenu) {

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


        systemMenu.add(manageCourses);
        systemMenu.add(library);
        systemMenu.add(shop);
    }
    protected void showPersonalInfo() {
        if (student == null) {
            JOptionPane.showMessageDialog(this, "未能加载学生信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 移除旧面板（如果有）
        if (infoPanel != null) {
            mainPanel.remove(infoPanel);
        }
        infoPanel = new StudentInfoPanel(student, this::backToMain);
        mainPanel.add(infoPanel, "STUDENT_INFO");
        cardLayout.show(mainPanel, "STUDENT_INFO");   // 用父类的 cardLayout
    }

    private void backToMain() {
        cardLayout.show(mainPanel, "MAIN");
    }

}
