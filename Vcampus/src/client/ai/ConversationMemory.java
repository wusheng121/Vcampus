package client.ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * 负责管理对话上下文，支持 Sliding Window。
 */
public class ConversationMemory {
    private final LinkedList<JSONObject> messages = new LinkedList<>();
    private final int maxMessages; // 最多保留多少条 message

    public ConversationMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    // 添加用户消息
    public void addUserMessage(String content) {
        JSONObject msg = new JSONObject()
                .put("role", "user")
                .put("content", content);
        messages.add(msg);
        trim();
    }

    // 添加 AI 消息
    public void addAssistantMessage(String content) {
        JSONObject msg = new JSONObject()
                .put("role", "assistant")
                .put("content", content);
        messages.add(msg);
        trim();
    }

    // 获取裁剪后的消息列表
    public JSONArray getRecentMessages() {
        return new JSONArray(messages);
    }

    // 清空所有上下文
    public void clear() {
        messages.clear();
    }

    // 超过限制就从最老的开始丢
    private void trim() {
        while (messages.size() > maxMessages) {
            messages.removeFirst();
        }
    }

    public Object getHistory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHistory'");
    }
}
