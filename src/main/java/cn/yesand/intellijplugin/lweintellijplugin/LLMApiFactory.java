package cn.yesand.intellijplugin.lweintellijplugin;

import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import com.intellij.openapi.application.ApplicationManager;

public class LLMApiFactory {
    public static LLMApi getLLMApi() {

        AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);
        if (settings.getProviderName() == null && !settings.getProviderName().isEmpty()) {
            GlobalNotifier.showInfo("LLM info not found");
        }

        switch (settings.getProviderName()) {
            case "siliconflow":
                return new SiliconFlowApi();
            default:
                GlobalNotifier.showError("LLM info is wrong");
        }
        return null;
    }

    public static LLMApi getLLMApi(String providerName) {

        if (providerName == null || providerName.isEmpty()) {
            GlobalNotifier.showInfo("LLM info not found");
        }

        switch (providerName) {
            case "siliconflow":
                return new SiliconFlowApi();
            default:
                GlobalNotifier.showError("LLM info is wrong");
        }
        return null;
    }
}