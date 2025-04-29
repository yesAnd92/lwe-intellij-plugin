package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.IOException;

public interface LLMApi {
    String chatMessage(String msg) throws IOException;

    String chatMessage(String msg, StreamResponseCallback callback) throws IOException;

    String sayHello() throws IOException;
}