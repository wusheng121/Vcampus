package server.service;

import common.model.User;
import server.dao.PasswordResetDAO;
import server.dao.PasswordResetDAOImpl;
import server.dao.UserDAO;
import server.dao.UserDAOImpl;
import util.Config;
import util.DBUtil;
import util.EncryptUtil;
import util.MailSender;
import util.Validators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

public class UserService {
    private UserDAO userDAO = new UserDAOImpl();
    private final PasswordResetDAO resetDAO = new PasswordResetDAOImpl();
    private final Random random = new Random();

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
        // 密码哈希统一在 Service 层完成，DAO 只负责存储（与 updatePassword 一致）
        newUser.setPassword(EncryptUtil.sha256(newUser.getPassword()));
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

    // ========================= 找回密码 =========================

    /**
     * 请求找回密码：生成 6 位验证码，存表（哈希）并发到用户注册邮箱。
     * 用户不存在/无邮箱/发信失败均回"已发送"文案，防账号枚举。
     *
     * @return 给客户端展示的提示文案
     */
    public String requestPasswordReset(String userIdOrEmail) {
        // 防枚举的统一提示
        String generic = "如该账号存在，验证码已发送至其注册邮箱，请查收（10 分钟内有效）。";
        if (userIdOrEmail == null || userIdOrEmail.isBlank()) return generic;

        User user = resolveUser(userIdOrEmail);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return generic;
        }

        int ttl = Config.getInt("mail.verify-code.ttl-minutes", 10);
        String code = String.format("%06d", random.nextInt(1000000));
        String codeHash = EncryptUtil.sha256(code);
        Timestamp expire = new Timestamp(System.currentTimeMillis() + ttl * 60_000L);

        resetDAO.deleteStale(user.getUserId());
        boolean saved = resetDAO.save(user.getUserId(), codeHash, expire);
        if (!saved) return "发送失败，请稍后重试。";

        boolean mailed = MailSender.sendVerifyCode(user.getEmail(), code, ttl);
        if (!mailed) return "验证码邮件发送失败，请检查邮箱或联系管理员。";

        return "验证码已发送至 " + maskEmail(user.getEmail()) + "，" + ttl + " 分钟内有效。";
    }

    /**
     * 用验证码重置密码。
     *
     * @return "success" 成功；否则为失败原因
     */
    public String resetPassword(String userIdOrEmail, String code, String newPassword) {
        if (userIdOrEmail == null || userIdOrEmail.isBlank() || code == null || code.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return "参数不能为空";
        }
        User user = resolveUser(userIdOrEmail);
        if (user == null) return "账号不存在";

        if (!Validators.passwordStrong(newPassword)) {
            return "新密码强度不足：需至少 8 位且含字母与数字";
        }

        PasswordResetDAO.ResetRecord rec = resetDAO.findLatestUnused(user.getUserId());
        if (rec == null) return "验证码无效或已过期，请重新获取";
        if (!rec.codeHash.equals(EncryptUtil.sha256(code))) return "验证码错误";

        boolean ok = userDAO.updatePassword(user.getUserId(), EncryptUtil.sha256(newPassword));
        if (!ok) return "重置失败，请稍后重试";
        resetDAO.markUsed(rec.id);
        return "success";
    }

    /** 按账号或邮箱定位用户。 */
    private User resolveUser(String userIdOrEmail) {
        String key = userIdOrEmail.trim();
        if (key.indexOf('@') >= 0) {
            return userDAO.findUserByEmail(key);
        }
        User u = userDAO.findUserById(key);
        return u != null ? u : userDAO.findUserByEmail(key);
    }

    /** 邮箱脱敏：ab****@qq.com */
    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String head = email.substring(0, Math.min(2, at));
        return head + "***" + email.substring(at);
    }
}
