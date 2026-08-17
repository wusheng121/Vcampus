package client.ai;

import common.model.User;

/**
 * 通用校园助理 Hook（首页/未覆盖模块的兜底）。
 * 数据集：用户角色 + 姓名 + 该角色可见的模块清单，用于功能引导。
 */
public class GeneralAgentHooks implements AgentHooks {
    private final User user;

    public GeneralAgentHooks(User user) {
        this.user = user;
    }

    @Override
    public String systemPrompt() {
        return AgentPrompts.generalSystemPrompt();
    }

    @Override
    public String buildDataset() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<DATASET>>\n");
        sb.append("{\n");
        sb.append("  \"me\": {")
          .append("\"user_id\":\"").append(safe(user.getUserId())).append("\",")
          .append("\"name\":\"").append(safe(user.getName())).append("\",")
          .append("\"role\":\"").append(safe(user.getType())).append("\"")
          .append("},\n");
        sb.append("  \"available_modules\": [");
        String[] mods = modulesForRole(user.getType());
        for (int i = 0; i < mods.length; i++) {
            sb.append("\"").append(mods[i]).append("\"");
            if (i < mods.length - 1) sb.append(",");
        }
        sb.append("]\n}\n");
        sb.append("</DATASET>\n");
        return sb.toString();
    }

    @Override
    public String helpText() {
        return """
                我是你的校园通用助理

                可用指令：
                • /help —— 查看帮助
                • /clear —— 清空对话与上下文

                我能做什么：
                • 介绍各模块功能、告诉你“去哪做某事”
                • 回答与你账户相关的基本信息
                • 进入具体模块（图书馆/选课/商店）后，我会自动切换为该模块的专属助理
                """;
    }

    private static String[] modulesForRole(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            return new String[]{"图书馆", "用户管理", "学籍管理", "选课系统", "商店"};
        }
        if ("teacher".equalsIgnoreCase(role)) {
            return new String[]{"图书馆", "选课系统", "商店"};
        }
        return new String[]{"图书馆", "选课系统", "商店"};
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
