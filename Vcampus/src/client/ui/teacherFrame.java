package client.ui;

import client.net.ClientSocket;
import common.model.Teacher;
import common.model.User;
import common.net.Message;
import common.net.MessageType;

import javax.swing.*;
import java.awt.*;

public class teacherFrame extends MainFrame {
    private Teacher teacher;              // 当前登录教师对象     // 原主页
    private TeacherInfoPanel infoPanel;            // 个人信息面板（懒加载）
    public teacherFrame(User user) {
        super(user);
        buildSidebar();

        /* 加载教师对象 */
        String teacherId = findTeacherIdByUserId(user.getUserId());
        if (teacherId != null) {
            this.teacher = findTeacherById(teacherId);
        }

    }

    /* =============== 工具：反查 teacher_id =============== */
    private String findTeacherIdByUserId(String userId) {
        try {
            Message req = new Message(MessageType.TEACHER_ID_BY_USERID, userId);
            Message resp = ClientSocket.getInstance().sendRequest(req);
            if ("success".equals(resp.getStatus())) {
                return (String) resp.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* =============== 工具：根据 teacher_id 查完整对象 =============== */
    private Teacher findTeacherById(String teacherId) {
        try {
            Message req = new Message(MessageType.TEACHER_GET, teacherId);
            Message resp = ClientSocket.getInstance().sendRequest(req);
            if ("success".equals(resp.getStatus())) {
                return (Teacher) resp.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void addModuleButtons() {
        registerModule("图书馆", () -> new LibraryFrame(user), "/pictures/图书馆.png");
        registerModule("选课系统", () -> new TeacherCourseFrame(user), "/pictures/排课.png");
        registerModule("商店", () -> new ShopFrame(user), "/pictures/商店.png");
    }

    /* =============== 个人信息 =============== */
    protected void showPersonalInfo() {
        if (teacher == null) {
            JOptionPane.showMessageDialog(this, "未能加载教师信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 移除旧面板（如果有）
        if (infoPanel != null) {
            mainPanel.remove(infoPanel);
        }
        infoPanel = new TeacherInfoPanel(teacher, this::backToMain);
        mainPanel.add(infoPanel, "TEACHER_INFO");
        cardLayout.show(mainPanel, "TEACHER_INFO");   // 用父类的 cardLayout
    }

    private void backToMain() {
        cardLayout.show(mainPanel, "Welcome");
    }
}
