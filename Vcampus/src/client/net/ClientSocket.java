package client.net;

import common.net.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocket {
    private String serverHost = "127.0.0.1";
    private int serverPort = 5000; // 服务器监听端口

    public Message sendRequest(Message request) throws Exception {
        try (Socket socket = new Socket(serverHost, serverPort)) {

            // 先创建输出流并 flush，避免服务器阻塞
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            // 再创建输入流
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 调试输出：发送前
            System.out.println("[ClientSocket] 发送请求 type=" + request.getType());

            // 发送消息
            out.writeObject(request);
            out.flush();

            // 接收返回消息
            Message response = (Message) in.readObject();

            // 调试输出：接收后
            System.out.println("[ClientSocket] 收到响应 status=" + response.getStatus()
                    + ", msg=" + response.getMsg());

            return response;
        }
    }
}
