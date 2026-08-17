package client.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import util.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UseLlm {
    private final HttpClient client;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public UseLlm() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        // API key：优先环境变量 DEEPSEEK_API_KEY，其次 config.properties，最后留空
        String key = System.getenv("DEEPSEEK_API_KEY");
        if (key == null || key.isBlank()) {
            key = Config.get("deepseek.api.key", "");
        }
        this.apiKey = key;

        this.model = Config.get("deepseek.model", "deepseek-chat");
        this.baseUrl = Config.get("deepseek.base-url", "https://api.deepseek.com/chat/completions");
    }

    /**
     * 向 DeepSeek Chat Completions API 发送请求
     *
     * @param messages 对话消息数组（含 role/content）
     */
    public String getResponse(JSONArray messages) throws Exception {
        JSONObject body = new JSONObject()
                .put("model", model)
                .put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 非 2xx 直接把错误体返回，便于排错
        if (response.statusCode() / 100 != 2) {
            return "请求失败（HTTP " + response.statusCode() + "）：\n" + response.body();
        }

        // 解析 content
        JSONObject json = new JSONObject(response.body());
        JSONObject choice0 = json.getJSONArray("choices").getJSONObject(0);
        JSONObject message = choice0.getJSONObject("message");
        return message.optString("content", "");
    }

    /**
     * 便捷重载：单轮对话（可附带历史消息）。
     *
     * @param userMessage 本轮用户输入
     * @param history     历史消息，建议为 JSONArray（每项含 role/content）；为 null 则忽略
     */
    public String chat(String userMessage, Object history) throws Exception {
        JSONArray messages = new JSONArray();
        if (history instanceof JSONArray arr) {
            for (int i = 0; i < arr.length(); i++) {
                Object o = arr.get(i);
                if (o instanceof JSONObject) {
                    messages.put(o);
                }
            }
        }
        messages.put(new JSONObject().put("role", "user").put("content", userMessage));
        return getResponse(messages);
    }
}
