package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.IOException;

public class SiliconFlowApi extends BaseLLMApiImpl {

    @Override
    public String chatMessage(String msg,String prompt) throws IOException {
        return chatMessage(msg, prompt,null);
    }

    @Override
    public String chatMessage(String msg, String prompt,StreamResponseCallback callback) throws IOException {
        return super.chatMessage(msg,prompt, callback);
    }
}