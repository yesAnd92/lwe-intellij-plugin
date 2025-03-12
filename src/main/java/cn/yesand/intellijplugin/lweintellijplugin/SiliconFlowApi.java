package cn.yesand.intellijplugin.lweintellijplugin;


import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import cn.yesand.intellijplugin.lweintellijplugin.vo.CommonResponse;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;

import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager;

public class SiliconFlowApi {
    private static OkHttpClient createClient(int timeoutSeconds) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    // 添加一个新的接口用于处理流式响应
    public interface StreamResponseCallback {
        void onMessage(String message);

        void onComplete();

        void onError(Throwable throwable);
    }

    public static String generateCommitMessage(String diff, Project project) throws IOException {
        // 使用非流式方式调用
        return generateCommitMessage(diff, project, null);
    }

    public static String generateCommitMessage(String diff, Project project, StreamResponseCallback callback) throws IOException {
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
        systemMessage.put("content", PromptManager.getDefaultPrompt());
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

        // 根据是否有回调决定是否使用流式响应
        boolean isStream = callback != null;
        requestData.put("stream", isStream);

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

        // 如果是流式响应
        if (isStream) {
            StringBuilder fullResponse = new StringBuilder();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        if (callback != null) {
                            callback.onError(new IOException("Unexpected response " + response));
                        }
                        return;
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            if (callback != null) {
                                callback.onError(new IOException("Response body is null"));
                            }
                            return;
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream(), "UTF-8"));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) continue;
                            if (!line.startsWith("data:")) continue;

                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                if (callback != null) {
                                    callback.onComplete();
                                }
                                continue;
                            }

                            try {
                                Map<String, Object> eventData = gson.fromJson(data, Map.class);
                                if (eventData.containsKey("choices")) {
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) eventData.get("choices");
                                    if (!choices.isEmpty()) {
                                        Map<String, Object> choice = choices.get(0);
                                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                                        if (delta != null && delta.containsKey("content")) {
                                            String content = (String) delta.get("content");
                                            fullResponse.append(content);
                                            if (callback != null) {
                                                callback.onMessage(content);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                if (callback != null) {
                                    callback.onError(e);
                                }
                            }
                        }
                    }
                }
            });

            // 对于流式响应，我们返回null，因为实际内容会通过回调传递
            return null;
        } else {
            // 非流式响应，保持原有逻辑
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

}