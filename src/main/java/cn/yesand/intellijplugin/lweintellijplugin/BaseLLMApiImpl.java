package cn.yesand.intellijplugin.lweintellijplugin;

import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager;
import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import cn.yesand.intellijplugin.lweintellijplugin.vo.CommonResponse;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public abstract class BaseLLMApiImpl implements LLMApi {

    protected final OkHttpClient createClient(int timeoutSeconds) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    protected Map<String, Object> buildRequestData(String userContent, String model, boolean isStream) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", PromptManager.getDefaultPrompt());
        messages.add(systemMessage);

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
        requestData.put("stream", isStream);
        return requestData;
    }

    protected final String sendRequest(Map<String, Object> requestData, StreamResponseCallback callback) throws IOException {
        AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);
        OkHttpClient client = createClient(settings.getAiSocketTimeout());
        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestData);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(settings.getAiHost())
                .post(body)
                .addHeader("Authorization", "Bearer " + settings.getAiToken())
                .addHeader("Content-Type", "application/json")
                .build();

        boolean isStream = (boolean) requestData.getOrDefault("stream", false);

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
            return null;
        } else {
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

    // 子类只需实现chatMessage和generateCommitMessage的参数拼装
    @Override
    public String chatMessage(String msg) throws IOException {
        return chatMessage(msg, null);
    }

    @Override
    public String chatMessage(String msg, StreamResponseCallback callback) throws IOException {
        AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);
        Map<String, Object> requestData = buildRequestData(msg, settings.getAiModel(), callback != null);
        return sendRequest(requestData, callback);
    }

    @Override
    public String sayHello() throws IOException {
        return "Hello from BaseLLMApiImpl!";
    }
}
