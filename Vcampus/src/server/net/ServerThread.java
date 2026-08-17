package server.net;

import common.net.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * 服务端连接线程：读取请求，按域分发到对应 {@link Handler}，回写响应。
 * 原先 700+ 行的巨型 switch 已拆分到各域 Handler。
 */
public class ServerThread implements Runnable {
    private final Socket socket;
    private final List<Handler> handlers = List.of(
            new UserHandler(),
            new StudentHandler(),
            new CourseHandler(),
            new BookHandler(),
            new JournalHandler(),
            new ShopHandler()
    );

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("[ServerThread] 客户端已连接: " + socket.getRemoteSocketAddress());
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message request = (Message) in.readObject();
                System.out.println("[ServerThread] 收到请求 type=" + request.getType());

                Message response = dispatch(request);
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
    }

    /** 依次询问各处理器，命中则返回其响应；均不命中则回退"未知请求类型"。 */
    private Message dispatch(Message request) {
        for (Handler h : handlers) {
            Message resp = h.handle(request);
            if (resp != null) {
                return resp;
            }
        }
        Message fallback = new Message();
        fallback.setStatus("fail");
        fallback.setMsg("未知请求类型：" + request.getType());
        return fallback;
    }
}
