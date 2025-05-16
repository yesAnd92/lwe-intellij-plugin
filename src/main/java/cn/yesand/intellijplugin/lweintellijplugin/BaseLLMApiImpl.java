package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;

import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager;
import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import cn.yesand.intellijplugin.lweintellijplugin.vo.CommonResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class BaseLLMApiImpl implements LLMApi {

    protected final OkHttpClient createClient(int timeoutSeconds) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    protected Map<String, Object> buildRequestData(String userContent, String prompt, String model, boolean isStream) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt);
        messages.add(systemMessage);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("messages", messages);
        requestData.put("model", model);
        requestData.put("frequency_penalty", 0.5);
        requestData.put("temperature", 0.5);
        requestData.put("max_tokens", 4096);
        requestData.put("response_format", new HashMap<String, Object>() {
            {
                put("type", "text");
            }
        });
        requestData.put("stop", null);
        requestData.put("stream", isStream);
        return requestData;
    }

    protected final String sendRequest(Map<String, Object> requestData, StreamResponseCallback callback, String host, String token)
            throws IOException {
        AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);
        OkHttpClient client = createClient(settings.getAiSocketTimeout());
        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestData);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(host)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
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
                            if (line.isEmpty())
                                continue;
                            if (!line.startsWith("data:"))
                                continue;

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
                                    List<Map<String, Object>> choices = (List<Map<String, Object>>) eventData
                                            .get("choices");
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
                if (!response.isSuccessful())
                    throw new IOException("Unexpected response " + response);

                CommonResponse commitResponse = gson.fromJson(
                        response.body().string(),
                        CommonResponse.class);
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
        // replace prompt {language} with locale language
        String defaultLanguage = settings.getLocale();
        if (defaultLanguage == null || defaultLanguage.isEmpty()) {
            defaultLanguage = "English";
        }
        String prompt = PromptManager.getDefaultPrompt().replace("{language}", defaultLanguage);
        Map<String, Object> requestData = buildRequestData(msg, prompt, settings.getAiModel(), callback != null);
        return sendRequest(requestData, callback, settings.getAiHost(), settings.getAiToken());
    }

    @Override
    public Boolean sayHello(String model, String host, String token) throws IOException {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "Say 'hello'");
        messages.add(userMessage);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("messages", messages);
        requestData.put("model", model);
        requestData.put("frequency_penalty", 0.5);
        requestData.put("temperature", 0.5);
        requestData.put("max_tokens", 4096);
        requestData.put("response_format", new HashMap<String, Object>() {
            {
                put("type", "text");
            }
        });
        requestData.put("stop", null);
        requestData.put("stream", false);

        OkHttpClient client = createClient(30);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestData);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(host)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }
}
