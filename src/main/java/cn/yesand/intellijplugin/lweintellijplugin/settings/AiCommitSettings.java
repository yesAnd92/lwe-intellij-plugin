package cn.yesand.intellijplugin.lweintellijplugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import cn.yesand.intellijplugin.lweintellijplugin.LLMApiFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings",
    storages = @Storage("LwePluginSettings.xml")
)
@Service(Service.Level.APP)
public final class AiCommitSettings implements PersistentStateComponent<AiCommitSettings> {
    private String providerName = LLMApiFactory.PROVIDER_SILICONFLOW;
    private String aiHost = "https://api.siliconflow.cn/v1/chat/completions";
    private String aiProxyUrl = "";
    private int aiSocketTimeout = 30;
    private String aiToken = "";
    private String aiModel = "deepseek-ai/DeepSeek-V3";
    private String locale = "";

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

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}