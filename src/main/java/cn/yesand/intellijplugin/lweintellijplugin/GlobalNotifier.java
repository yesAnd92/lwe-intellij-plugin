package cn.yesand.intellijplugin.lweintellijplugin;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class GlobalNotifier {

    public static void showError(String message) {
        Messages.showErrorDialog(message, "Error");
    }

    public static void showInfo(String message) {
        Messages.showInfoMessage(message, "Info");
    }

    public static void showWarning(String message) {
        Messages.showWarningDialog(message, "Warning");
    }
    
    // 右下角通知方法
    public static void notifyError(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("LWE Notification Group")
                .createNotification(content, NotificationType.ERROR)
                .notify(project);
    }
    
    public static void notifyInfo(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("LWE Notification Group")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }
    
    public static void notifyWarning(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("LWE Notification Group")
                .createNotification(content, NotificationType.WARNING)
                .notify(project);
    }
}