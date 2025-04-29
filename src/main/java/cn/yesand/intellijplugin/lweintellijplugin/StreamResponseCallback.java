package cn.yesand.intellijplugin.lweintellijplugin;

// 添加一个新的接口用于处理流式响应
public interface StreamResponseCallback {
    void onMessage(String message);

    void onComplete();

    void onError(Throwable throwable);
}
