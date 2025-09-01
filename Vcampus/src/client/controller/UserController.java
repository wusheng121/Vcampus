package client.controller;

import client.net.ClientSocket;
import common.net.Message;
import common.model.User;

public class UserController {
    private ClientSocket clientSocket = new ClientSocket();

    public Message login(String userId, String password) {
        try {
            // 封装用户对象
            User user = new User();
            user.setUserId(userId);
            user.setPassword(password);

            // 封装消息
            System.out.println("客户端发送登录请求: " + userId + "/" + password);

            Message request = new Message();
            request.setType("login");
            request.setData(user);
            System.out.println("客户端发送登录请求: " + userId + "/" + password);


            // 调用net层发送
            return clientSocket.sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            Message errorMsg = new Message();
            errorMsg.setStatus("fail");
            errorMsg.setMsg("无法连接服务器: " + e.getMessage());
            return errorMsg;
        }
    }
}
