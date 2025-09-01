package server.net;

import common.model.User;
import common.net.Message;
import server.service.UserService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Socket socket;
    private UserService userService = new UserService();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("[ServerThread] 客户端已连接: " + socket.getRemoteSocketAddress());

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {
            out.flush(); // 确保流头发送出去
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message request = (Message) in.readObject();
                System.out.println("[ServerThread] 收到请求 type=" + request.getType());

                Message response = new Message();

                if ("login".equals(request.getType())) {
                    User user = (User) request.getData();
                    User verified = userService.login(user.getUserId(), user.getPassword());
                    if (verified != null) {
                        response.setStatus("success");
                        response.setData(verified);
                        response.setMsg("登录成功");
                    } else {
                        response.setStatus("fail");
                        response.setMsg("账号或密码错误");
                    }
                } else {
                    response.setStatus("fail");
                    response.setMsg("未知请求类型：" + request.getType());
                }

                out.writeObject(response);
                out.flush();
                System.out.println("[ServerThread] 响应已发送");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }}

