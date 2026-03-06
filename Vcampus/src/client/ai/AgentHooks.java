package client.ai;

public interface AgentHooks {
    /** 返回系统提示（system prompt） */
    String systemPrompt();

    /** 返回数据集（供 LLM 使用） */
    String buildDataset();

    /** 返回欢迎/帮助文本（进入 Agent 和 /help 用） */
    String helpText();

    /** 进入对话前的预加载（需要时拉取数据），不需要可留空实现 */
    default void preload() {}
}
