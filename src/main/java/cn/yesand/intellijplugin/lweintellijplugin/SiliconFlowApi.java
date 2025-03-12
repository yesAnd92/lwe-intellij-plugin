package cn.yesand.intellijplugin.lweintellijplugin;


import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import cn.yesand.intellijplugin.lweintellijplugin.vo.CommonResponse;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiliconFlowApi {
    private static OkHttpClient createClient(int timeoutSeconds) {
        return new OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }

    private static final String DEFAULT_PROMPT = "Analyze the provided git diff output and generate structured commit messages following these rules:\n" +
            "1.Format according to Conventional Commits specification:\n" +
            "types(Scope):<Short description>\n" +
            "[BREAKING CHANGE: if applicable]\n" +
            "Common types: feat, fix, chore, docs, style, refactor, test\n" +
            "Scope: 2-5 word module name (e.g. \"auth\", \"dashboard\")\n" +
            "Short_description: Start with imperative verb (\"Add\" not \"Added\") and keep description under 50 characters\n" +
            "2.Group similar changes (e.g., multiple UI fixes) into single commits\n" +
            "3.Maximum 5 distinct commits, ordered by importance\n" +
            "Provide just the formatted commit messages (no explanations) based on the code changes in the diff.";

    public static String generateCommitMessage(String diff, Project project) throws IOException {
        AiCommitSettings settings = project.getService(AiCommitSettings.class);
        OkHttpClient client = createClient(settings.getAiSocketTimeout());
        // 读取 prompt 文件
//        String prompt;
//        try {
//            prompt = new String(
//                SiliconFlowApi.class.getClassLoader()
//                    .getResourceAsStream("./prompt/git_diff_summary.prompt")
//                    .readAllBytes(),
//                StandardCharsets.UTF_8
//            );
//        } catch (Exception e) {
//            // 如果读取失败，使用默认 prompt
//            prompt = "请根据以下代码变更生成一个简洁明了的 Git 提交信息，使用约定式提交格式：";
//        }


        String model = settings.getAiModel();
        // 构建消息
        List<Map<String, Object>> messages = new ArrayList<>();
        // 更建议用明确的对象构建方式
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", diff);
        messages.add(userMessage);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", DEFAULT_PROMPT);
        messages.add(systemMessage);

        // 构建请求数据
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("messages", messages);
        requestData.put("model", model);
        requestData.put("frequency_penalty", 0.5);
        requestData.put("temperature", 0.5);
        requestData.put("max_tokens", 4096);
        requestData.put("response_format", new HashMap<String, Object>() {{
            put("type", "text");
        }});
        requestData.put("stop", null);
        requestData.put("stream", false);

        // 转换为 JSON
        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestData);

        // 构建请求
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(settings.getAiHost())
                .post(body)
                .addHeader("Authorization", "Bearer " + settings.getAiToken())
                .addHeader("Content-Type", "application/json")
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);

            CommonResponse commitResponse = gson.fromJson(
                    response.body().string(),
                    CommonResponse.class
            );
            return commitResponse.getChoices().get(0).getMessage().getContent();
        }
    }

}