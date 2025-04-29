package cn.yesand.intellijplugin.lweintellijplugin;

import com.intellij.openapi.ui.Messages;

public class GlobalNotifier {

    public static void showError(String message) {
        Messages.showErrorDialog(message, "错误");
    }

    public static void showInfo(String message) {
        Messages.showInfoMessage(message, "提示");
    }

    public static void showWarning(String message) {
        Messages.showWarningDialog(message, "警告");
    }
}