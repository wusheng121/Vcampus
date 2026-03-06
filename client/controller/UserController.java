package client.controller;

import client.net.ClientSocket;
import common.model.PasswordDTO;
import common.model.User;
import common.net.Message;

import java.util.List;

@SuppressWarnings("unchecked")
public class UserController {
    private ClientSocket clientSocket = new ClientSocket();

    // 登录
    public Message login(String userId, String password) {
        try {
            User user = new User();
            user.setUserId(userId);
            user.setPassword(password);

            Message request = new Message();
            request.setType("login");
            request.setData(user);

            return clientSocket.sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            Message errorMsg = new Message();
            errorMsg.setStatus("fail");
            errorMsg.setMsg("无法连接服务器: " + e.getMessage());
            return errorMsg;
        }
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        try {
            Message request = new Message();
            request.setType("getAllUsers");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<User>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 添加用户
    public boolean addUser(User user) {
        try {
            Message request = new Message();
            request.setType("addUser");
            request.setData(user);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 更新用户信息（不含密码）
    public boolean updateUser(User user) {
        try {
            Message request = new Message();
            request.setType("updateUser");
            request.setData(user);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 修改密码
    public boolean updatePassword(String userId, String oldPwd, String newPwd) {
        try {
            PasswordDTO dto = new PasswordDTO(userId, oldPwd, newPwd);
            Message req = new Message();
            req.setType("updatePassword"); // 使用不同的消息类型
            req.setData(dto);

            System.out.println("[CLI] 发送 updatePassword 请求");
            Message resp = clientSocket.sendRequest(req);
            System.out.println("[CLI] 收到响应 status=" + resp.getStatus() + ", msg=" + resp.getMsg());

            return "success".equals(resp.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除用户
    public boolean deleteUser(String userId) {
        try {
            Message request = new Message();
            request.setType("deleteUser");
            request.setData(userId);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 模糊搜索用户
    public List<User> searchUsers(String keyword) {
        try {
            Message request = new Message();
            request.setType("searchUsers");
            request.setData(keyword);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<User>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 按角色筛选用户
    public List<User> findUsersByRole(String role) {
        try {
            Message request = new Message();
            request.setType("findUsersByRole");
            request.setData(role);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<User>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
