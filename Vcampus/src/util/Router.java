package util;

import client.ui.adminFrame;
import client.ui.studentFrame;
import client.ui.teacherFrame;
import common.model.User;

public class Router {
    public static void route(User user) {
        switch (user.getType()) {
            case "admin":
                new adminFrame(user).setVisible(true);
                break;
            case "teacher":
                new teacherFrame(user).setVisible(true);
                break;
            case "student":
                new studentFrame(user).setVisible(true);
                break;
            default:
                throw new IllegalArgumentException("未知的用户角色: " + user.getType());
        }
    }
}
