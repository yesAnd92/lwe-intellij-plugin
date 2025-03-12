package cn.yesand.intellijplugin.lweintellijplugin.vo;

public class Usage {
    private long completionTokens;

    private long promptTokens;

    private long promptCacheHitTokens;

    private long promptCacheMissTokens;

    private long totalTokens;

    // Getters and Setters
    public long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public long getPromptCacheHitTokens() {
        return promptCacheHitTokens;
    }

    public void setPromptCacheHitTokens(long promptCacheHitTokens) {
        this.promptCacheHitTokens = promptCacheHitTokens;
    }

    public long getPromptCacheMissTokens() {
        return promptCacheMissTokens;
    }

    public void setPromptCacheMissTokens(long promptCacheMissTokens) {
        this.promptCacheMissTokens = promptCacheMissTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }
}
