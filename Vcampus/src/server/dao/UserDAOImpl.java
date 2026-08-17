package server.dao;

import common.model.User;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {



    @Override
    public User findUserById(String userId) {
        User user = null;
        String sql = "SELECT user_id, password, role, name, email, phone, address, created_at FROM user WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User findUserByEmail(String email) {
        User user = null;
        String sql = "SELECT user_id, password, role, name, email, phone, address, created_at FROM user WHERE email=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO user(user_id, password, role, name, email, phone, address) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getPassword());   // 密码由 Service 层哈希后传入，DAO 只负责存储
            ps.setString(3, user.getType());
            ps.setString(4, user.getName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getAddress());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePassword(String userId, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(true);
            // ❌ 不要再加密
            ps.setString(1, newPassword);  // ✅ 直接存传入值
            ps.setString(2, userId);

            int rows = ps.executeUpdate();
            System.out.println("[DAO] 影响行数=" + rows);
            System.out.println("[DAO] 事务已提交，哈希应=" + newPassword);
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ 新增：修改用户信息（不含密码）
    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET role=?, name=?, email=?, phone=?, address=? WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getType());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getAddress());
            ps.setString(6, user.getUserId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ 新增：删除用户
    @Override
    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM user WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, password, role, name, email, phone, address, created_at FROM user";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ 新增：模糊搜索（ID/姓名/邮箱）
    @Override
    public List<User> searchUsers(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, password, role, name, email, phone, address, created_at FROM user " +
                "WHERE user_id LIKE ? OR name LIKE ? OR email LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ 新增：按角色筛选
    @Override
    public List<User> findUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, password, role, name, email, phone, address, created_at FROM user WHERE role=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 工具方法：ResultSet -> User
    private User mapRow(ResultSet rs) throws Exception {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setPassword(rs.getString("password"));
        user.setType(rs.getString("role"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));

//        Timestamp ts = rs.getTimestamp("created_at");
//        if (ts != null) {
//            user.setCreatedAt(ts.toString()); // 转成字符串保存
//        }
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
