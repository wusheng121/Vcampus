package server.net;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 5000; // 与客户端一致
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，监听端口: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接：" + clientSocket.getInetAddress());
                // 为每个连接创建新线程
                new Thread(new ServerThread(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
