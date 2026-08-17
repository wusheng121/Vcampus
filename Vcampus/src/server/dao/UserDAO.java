package server.dao;

import common.model.User;
import java.util.List;

public interface UserDAO {
    // 根据用户ID查找
    User findUserById(String userId);

    // 根据邮箱查找（找回密码用）
    User findUserByEmail(String email);

    // 添加用户
    boolean addUser(User user);

    // 修改用户信息（不含密码）
    boolean updateUser(User user);

    // 修改密码
    boolean updatePassword(String userId, String newPassword);

    // 删除用户
    boolean deleteUser(String userId);

    // 查询所有用户
    List<User> findAllUsers();

    // 模糊搜索（按ID/姓名/邮箱）
    List<User> searchUsers(String keyword);

    // 按角色筛选
    List<User> findUsersByRole(String role);
}
