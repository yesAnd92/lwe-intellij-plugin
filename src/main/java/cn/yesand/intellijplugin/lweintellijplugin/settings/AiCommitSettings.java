package cn.yesand.intellijplugin.lweintellijplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings",
    storages = @Storage("AiCommitSettings.xml")
)
@Service(Service.Level.PROJECT)
public final class AiCommitSettings implements PersistentStateComponent<AiCommitSettings> {
    private String aiHost = "https://api.openai.com/v1/";
    private String aiProxyUrl = "";
    private int aiSocketTimeout = 30;
    private String aiToken = "";
    private String aiModel = "gpt-3.5-turbo";
    private String locale = "";
    private String promptType = "Basic";

    @Override
    public @Nullable AiCommitSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AiCommitSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getAiHost() {
        return aiHost;
    }

    public void setAiHost(String aiHost) {
        this.aiHost = aiHost;
    }

    public String getAiProxyUrl() {
        return aiProxyUrl;
    }

    public void setAiProxyUrl(String aiProxyUrl) {
        this.aiProxyUrl = aiProxyUrl;
    }

    public int getAiSocketTimeout() {
        return aiSocketTimeout;
    }

    public void setAiSocketTimeout(int aiSocketTimeout) {
        this.aiSocketTimeout = aiSocketTimeout;
    }

    public String getAiToken() {
        return aiToken;
    }

    public void setAiToken(String aiToken) {
        this.aiToken = aiToken;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getPromptType() {
        return promptType;
    }

    public void setPromptType(String promptType) {
        this.promptType = promptType;
    }

}