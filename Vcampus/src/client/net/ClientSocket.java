package client.net;

import common.net.Message;
import util.Config;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客户端与服务端之间的持久连接（单例）。
 * <p>所有控制器共享同一实例，复用一条 TCP 连接发送全部请求，
 * 相比此前"每次请求新建并关闭 Socket"大幅减少握手开销与端口消耗。
 * <p>断线时自动重置并重连一次后重试；通过 socket 的 TCP keepalive 维持活性。
 * <p>{@code sendRequest} 同步化，保证多线程（EDT / SwingWorker）串行收发，避免对象流交错。
 */
public class ClientSocket {
    private static final ClientSocket INSTANCE = new ClientSocket();

    private final String serverHost;
    private final int serverPort;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientSocket() {
        this.serverHost = Config.get("server.host", "127.0.0.1");
        this.serverPort = Config.getInt("server.port", 8888);
    }

    public static ClientSocket getInstance() {
        return INSTANCE;
    }

    /** 确保连接可用：未建立或已关闭则（重新）建立。 */
    private void ensureConnected() throws Exception {
        if (socket != null && !socket.isClosed() && socket.isConnected()
                && out != null && in != null) {
            return;
        }
        closeQuietly();
        socket = new Socket(serverHost, serverPort);
        socket.setKeepAlive(true); // TCP 心跳，尽早发现半开连接
        // 先建输出流并 flush 发送流头，避免对端 ObjectInputStream 阻塞
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * 发送请求并等待响应。若连接异常，重置后重连一次并重试。
     */
    public synchronized Message sendRequest(Message request) throws Exception {
        try {
            ensureConnected();
            // 自动注入当前登录用户为 caller，供服务端 RBAC（调用方未单独设置时）
            if (request.getCaller() == null) {
                request.setCaller(util.ClientSession.getCurrentUser());
            }
            System.out.println("[ClientSocket] 发送请求 type=" + request.getType());
            out.writeObject(request);
            out.flush();
            Message response = (Message) in.readObject();
            System.out.println("[ClientSocket] 收到响应 status=" + response.getStatus()
                    + ", msg=" + response.getMsg());
            return response;
        } catch (Exception first) {
            System.out.println("[ClientSocket] 连接异常，尝试重连并重试: " + first.getMessage());
            closeQuietly();
            ensureConnected();
            out.writeObject(request);
            out.flush();
            Message response = (Message) in.readObject();
            return response;
        }
    }

    private void closeQuietly() {
        if (out != null) try { out.close(); } catch (Exception ignore) {}
        if (in != null) try { in.close(); } catch (Exception ignore) {}
        if (socket != null) try { socket.close(); } catch (Exception ignore) {}
        out = null;
        in = null;
        socket = null;
    }
}
