package cn.yesand.intellijplugin.lweintellijplugin.prompt;

import java.nio.charset.StandardCharsets;

public class PromptManager {
    private static final String DEFAULT_PROMPT;
    
    static {
        String prompt;
        try {
            prompt = new String(
                PromptManager.class.getClassLoader()
                    .getResourceAsStream("prompt/git_diff_summary.prompt")
                    .readAllBytes(),
                StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            e.printStackTrace();
            prompt = "";
        }
        DEFAULT_PROMPT = prompt;
    }
    
    public static String getDefaultPrompt() {
        return DEFAULT_PROMPT;
    }
}