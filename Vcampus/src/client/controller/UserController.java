package client.controller;
import common.net.MessageType;

import client.net.ClientSocket;
import common.model.PasswordDTO;
import common.model.User;
import common.net.Message;

import java.util.List;

@SuppressWarnings("unchecked")
public class UserController {
    private ClientSocket clientSocket = ClientSocket.getInstance();

    // 登录
    public Message login(String userId, String password) {
        try {
            User user = new User();
            user.setUserId(userId);
            user.setPassword(password);

            Message request = new Message();
            request.setType(MessageType.LOGIN);
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
            request.setType(MessageType.GET_ALL_USERS);

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
            request.setType(MessageType.ADD_USER);
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
            request.setType(MessageType.UPDATE_USER);
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
            req.setType(MessageType.UPDATE_PASSWORD); // 使用不同的消息类型
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
            request.setType(MessageType.DELETE_USER);
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
            request.setType(MessageType.SEARCH_USERS);
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
            request.setType(MessageType.FIND_USERS_BY_ROLE);
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

    // ========================= 找回密码 =========================

    /** 请求发送验证码到注册邮箱。返回服务端提示文案。 */
    public String requestPasswordReset(String userIdOrEmail) {
        try {
            Message request = new Message();
            request.setType(MessageType.REQUEST_PASSWORD_RESET);
            request.setData(userIdOrEmail);
            Message response = clientSocket.sendRequest(request);
            return response.getMsg();
        } catch (Exception e) {
            e.printStackTrace();
            return "请求失败：" + e.getMessage();
        }
    }

    /** 用验证码重置密码。返回服务端结果（success=成功）。 */
    public String resetPassword(String userIdOrEmail, String code, String newPassword) {
        try {
            Message request = new Message();
            request.setType(MessageType.RESET_PASSWORD);
            request.setData(new Object[]{userIdOrEmail, code, newPassword});
            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus()) ? "success" : response.getMsg();
        } catch (Exception e) {
            e.printStackTrace();
            return "请求失败：" + e.getMessage();
        }
    }
}
