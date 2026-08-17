package util;

import common.model.User;

/**
 * 客户端会话：持有当前登录用户。登录后设置，退出时清除。
 * {@link client.net.ClientSocket} 在每次发请求时自动把它注入到 {@code Message.caller}，
 * 供服务端做 RBAC 鉴权（按你的选择，未引会话令牌，仍由客户端携带，不解决伪造）。
 */
public final class ClientSession {
    private static User current;

    private ClientSession() {
    }

    public static void login(User user) {
        current = user;
    }

    public static void logout() {
        current = null;
    }

    public static User getCurrentUser() {
        return current;
    }
}
