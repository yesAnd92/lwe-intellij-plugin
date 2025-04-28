package cn.yesand.intellijplugin.lweintellijplugin.prompt;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

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
            Notification notification = new Notification(
                    "AI Commit Error", // Changed group display ID
                    "AI Commit Failed", // Changed title
                    "Error: " + e.getMessage(),
                    NotificationType.ERROR
            );
            prompt = "";
        }
        DEFAULT_PROMPT = prompt;
    }
    
    public static String getDefaultPrompt() {
        return DEFAULT_PROMPT;
    }
}