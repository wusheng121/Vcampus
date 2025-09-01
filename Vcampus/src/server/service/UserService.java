package server.service;

import common.model.User;
import server.dao.UserDAO;
import server.dao.UserDAOImpl;

public class UserService {
    private UserDAO userDAO = new UserDAOImpl();

    public User login(String userId, String password) {
        User user = userDAO.findUserById(userId);

        System.out.println("尝试登录: " + userId + " 输入密码=" + password);
        if (user == null) {
            System.out.println("数据库未找到该用户！");
        } else {
            System.out.println("数据库查到: " + user.getUserId() + " 密码=" + user.getPassword());
        }

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean registerUser(User newUser, String operatorRole) {
        // 仅管理员能添加用户
        if (!"admin".equals(operatorRole)) {
            return false;
        }
        // 检查是否已存在
        if (userDAO.findUserById(newUser.getUserId()) != null) {
            return false;
        }
        return userDAO.addUser(newUser);
    }

    public boolean changePassword(String userId, String newPwd) {
        return userDAO.updatePassword(userId, newPwd);
    }
}
