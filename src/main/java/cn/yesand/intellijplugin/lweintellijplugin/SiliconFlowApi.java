package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.IOException;

public class SiliconFlowApi extends BaseLLMApiImpl {

    @Override
    public String chatMessage(String msg) throws IOException {
        return chatMessage(msg, null);
    }

    @Override
    public String chatMessage(String msg, StreamResponseCallback callback) throws IOException {
        return super.chatMessage(msg, callback);
    }
}