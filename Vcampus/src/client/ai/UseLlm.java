package client.ai;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UseLlm {
    private final HttpClient client;
    private final String apiKey;
    private final String model = "deepseek-chat";

    public UseLlm() {
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyPort", "3288"); // VPN


        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        // CMD用环境变量存放：setx OPENAI_API_KEY "sk-xxxxxxx"
        this.apiKey = "sk-xxxxxxx"; // TODO: 从环境变量读取
//        if (this.apiKey == null || this.apiKey.isBlank()) {
//            throw new RuntimeException("请先设置环境变量 OPENAI_API_KEY");
//        }
    }

    /**
     * 向 OpenAI Chat Completions API 发送请求
     * @param messages 对话消息数组（包含 role/content）
     */
    public String getResponse(JSONArray messages) throws Exception {
        JSONObject body = new JSONObject()
                .put("model", model)
                .put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions")) // 调用openai API url
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
//    public static void main(String[] args) {
//    	
//    }

    public String chat(String string, Object history) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'chat'");
    }
}
