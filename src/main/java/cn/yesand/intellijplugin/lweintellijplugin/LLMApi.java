package cn.yesand.intellijplugin.lweintellijplugin;

import java.io.IOException;

public interface LLMApi {
    String chatMessage(String msg,String prompt) throws IOException;

    String chatMessage(String msg,String prompt, StreamResponseCallback callback) throws IOException;

    Boolean sayHello(String model, String host, String token) throws IOException;
}