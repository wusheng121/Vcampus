package client.ai;

import common.model.User;

/**
 * 按当前模块名 + 用户角色，产出对应的 {@link AgentHooks}。
 * 供全局助理在切换模块时调用。
 */
public final class AgentHooksFactory {
    private AgentHooksFactory() {}

    public static AgentHooks forContext(User user, String moduleName) {
        if (user == null) {
            return new GeneralAgentHooks(null);
        }
        String role = user.getType() == null ? "" : user.getType();
        String m = moduleName == null ? "" : moduleName;

        if ("图书馆".equals(m)) {
            return new LibraryAgentHooks(user);
        }
        if ("商店".equals(m)) {
            return new ShopAgentHooks(user);
        }
        if ("选课系统".equals(m)) {
            if ("student".equalsIgnoreCase(role)) return new CourseAgentHooks(user);
            if ("teacher".equalsIgnoreCase(role)) return new TeacherAgentHooks(user);
            return new GeneralAgentHooks(user); // 管理员课程管理走通用兜底
        }
        return new GeneralAgentHooks(user);
    }

    /** 对话窗标题。 */
    public static String titleFor(String moduleName) {
        if (moduleName == null) return "智能助理";
        return switch (moduleName) {
            case "图书馆" -> "图书馆助理";
            case "选课系统" -> "选课助理";
            case "商店" -> "商店助理";
            default -> "校园助理";
        };
    }
}
