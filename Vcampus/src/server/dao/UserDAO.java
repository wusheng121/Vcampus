package server.dao;

import common.model.User;
import java.util.List;

public interface UserDAO {
    // 根据用户ID查找用户
    User findUserById(String userId);

    // 添加新用户（由管理员添加）
    boolean addUser(User user);

    // 修改用户密码
    boolean updatePassword(String userId, String newPassword);

    // 可选：查询所有用户
    List<User> findAllUsers();
}

