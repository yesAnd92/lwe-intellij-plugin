package cn.yesand.intellijplugin.lweintellijplugin.vo;

import java.util.List;

public class ContentItem {
    private String token;

    private double logprob;

    private List<Integer> bytes;

    private List<TopLogprob> topLogprobs;

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getLogprob() {
        return logprob;
    }

    public void setLogprob(double logprob) {
        this.logprob = logprob;
    }

    public List<Integer> getBytes() {
        return bytes;
    }

    public void setBytes(List<Integer> bytes) {
        this.bytes = bytes;
    }

    public List<TopLogprob> getTopLogprobs() {
        return topLogprobs;
    }

    public void setTopLogprobs(List<TopLogprob> topLogprobs) {
        this.topLogprobs = topLogprobs;
    }
}
