package test;

import common.model.User;
import server.dao.UserDAO;
import server.dao.UserDAOImpl;
import server.service.UserService;

public class LoginDebugTest {
    public static void main(String[] args) {
        // 1. 测试 DAO
        UserDAO userDAO = new UserDAOImpl();
        String testUserId = "admin1";

        User user = userDAO.findUserById(testUserId);
        if (user != null) {
            System.out.println("[DAO] 找到用户: " + user.getUserId() + ", 密码=" + user.getPassword());
        } else {
            System.out.println("[DAO] 用户不存在: " + testUserId);
        }

        // 2. 测试 Service 登录逻辑
        UserService userService = new UserService();
        String inputPassword = "123456"; // 模拟客户端输入
        User verified = userService.login(testUserId, inputPassword);

        if (verified != null) {
            System.out.println("[Service] 登录成功: " + verified.getUserId());
        } else {
            System.out.println("[Service] 登录失败: 账号或密码错误");
        }
    }
}
