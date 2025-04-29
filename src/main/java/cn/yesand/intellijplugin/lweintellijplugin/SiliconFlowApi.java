package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.intellij.openapi.application.ApplicationManager;

import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;

public class SiliconFlowApi extends BaseLLMApiImpl {

    @Override
    public String chatMessage(String msg) throws IOException {
        return chatMessage(msg, null);
    }

    @Override
    public String chatMessage(String msg, StreamResponseCallback callback) throws IOException {

        AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);

        String model = settings.getAiModel();
        String aiHost = settings.getAiHost();
        String aiToken = settings.getAiToken();
        boolean isStream = Objects.nonNull(callback);
        Map<String, Object> questData = buildRequestData(msg, model, isStream);
        String result = sendRequest(questData, callback, aiHost, aiToken);
        return result;
    }
}