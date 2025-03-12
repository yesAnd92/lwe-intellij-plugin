package cn.yesand.intellijplugin.lweintellijplugin.vo;

import java.util.List;

public class CommonResponse {
    private String id;

    private List<Choice> choices;

    private long created;

    private String model;

    private String systemFingerprint;

    private String object;

    private Usage usage;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
    public long getCreated() { return created; }
    public void setCreated(long created) { this.created = created; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSystemFingerprint() { return systemFingerprint; }
    public void setSystemFingerprint(String systemFingerprint) { this.systemFingerprint = systemFingerprint; }
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }
}



