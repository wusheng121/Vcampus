package server.service;

import common.model.User;
import server.dao.UserDAO;
import server.dao.UserDAOImpl;
import util.DBUtil;
import util.EncryptUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO = new UserDAOImpl();

    // 登录
    public User login(String userId, String password) {
        User user = userDAO.findUserById(userId);

        System.out.println("尝试登录: " + userId + " 输入密码=" + password);
        if (user == null) {
            System.out.println("数据库未找到该用户！");
        } else {
            System.out.println("数据库查到: " + user.getUserId() + " 密码=" + user.getPassword());
        }

        if (user != null && user.getPassword().equals(EncryptUtil.sha256(password))) {
            return user;
        }
        return null;
    }

    // 注册/新增用户（管理员权限）
    public boolean registerUser(User newUser) {
        return userDAO.addUser(newUser);
    }

    // 修改密码
    /**
     * 带旧密码校验的密码修改
     * @return 原密码正确且更新成功返回 true
     */
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        // 1. 查用户
        System.out.println("[UPDATE] 客户端传来的新密码明文: " + newPassword);
        User user = userDAO.findUserById(userId);
        if (user == null) {
            return false;
        }

        // 2. 比对旧密码（SHA256）
        String oldHash = EncryptUtil.sha256(oldPassword);
        if (!oldHash.equals(user.getPassword())) {
            return false; // 原密码错误
        }

        // 3. 落库新密码（同样 SHA256）
        String newHash = EncryptUtil.sha256(newPassword);

        // 添加调试输出
        System.out.println("[UPDATE-PASSWORD] 用户: " + userId);
        System.out.println("[UPDATE-PASSWORD] 原密码哈希: " + user.getPassword());
        System.out.println("[UPDATE-PASSWORD] 输入旧密码哈希: " + oldHash);
        System.out.println("[UPDATE-PASSWORD] 新密码哈希: " + newHash);
        System.out.println("[SRV] 新密码哈希=" + newHash);
        System.out.println("[SRV] 即将调用 DAO 类: " + userDAO.getClass().getName());
        return userDAO.updatePassword(userId, newHash);
    }

    public boolean updateUserInfo(User user) {
        String sql = "UPDATE USER SET name=?, email=?, phone=?, address=? WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getAddress());
            ps.setString(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 获取所有用户
    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }

    // 删除用户（仅管理员）
    public boolean deleteUser(String userId) {

        return userDAO.deleteUser(userId);
    }

    // 更新用户信息（管理员可修改 name/email/role）
    public boolean updateUser(User user) {

        return userDAO.updateUser(user);
    }

    // 按关键字搜索（userId/name/email）
    public List<User> searchUsers(String keyword) {
        return userDAO.searchUsers(keyword);
    }

    // 按角色筛选
    public List<User> findUsersByRole(String role) {
        return userDAO.findUsersByRole(role);
    }
}
