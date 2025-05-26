package cn.yesand.intellijplugin.lweintellijplugin.prompt;

import cn.yesand.intellijplugin.lweintellijplugin.GlobalNotifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PromptManager {
    /**
     * Prompt类型枚举
     */
    public enum PromptType {
        GIT_DIFF("prompt/git_diff_summary.prompt"),
        JSON_VALUE("prompt/json_value.prompt");
        
        private final String resourcePath;
        
        PromptType(String resourcePath) {
            this.resourcePath = resourcePath;
        }
        
        public String getResourcePath() {
            return resourcePath;
        }
    }
    
    private static final Map<PromptType, String> PROMPT_MAP;
    
    static {
        Map<PromptType, String> promptMap = new HashMap<>();
        
        try {
            for (PromptType type : PromptType.values()) {
                promptMap.put(type, readResourceFile(type.getResourcePath()));
            }
        } catch (IOException e) {
            GlobalNotifier.showWarning("Failed to load prompt resources: " + e.getMessage());
        }
        
        PROMPT_MAP = Collections.unmodifiableMap(promptMap);
    }

    private static String readResourceFile(String path) throws IOException {
        try (InputStream is = PromptManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    
    /**
     * 根据提供的prompt类型返回对应的prompt内容
     * @param promptType prompt类型
     * @return 对应的prompt内容，如果类型不存在则返回空字符串
     */
    public static String getPrompt(PromptType promptType) {
        if (promptType == null) {
            return "";
        }
        
        return PROMPT_MAP.getOrDefault(promptType, "");
    }
}