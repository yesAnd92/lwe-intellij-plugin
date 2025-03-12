package cn.yesand.intellijplugin.lweintellijplugin.vo;

public class Choice {
    private String finishReason;
    
    private int index;
    
    private Message message;
    
    private Logprobs logprobs;

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Logprobs getLogprobs() {
        return logprobs;
    }

    public void setLogprobs(Logprobs logprobs) {
        this.logprobs = logprobs;
    }
}