package cn.yesand.intellijplugin.lweintellijplugin.action;

import cn.yesand.intellijplugin.lweintellijplugin.GlobalNotifier;
import cn.yesand.intellijplugin.lweintellijplugin.LLMApiFactory;
import cn.yesand.intellijplugin.lweintellijplugin.converter.ConverterFactory;
import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager;
import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager.PromptType;
import cn.yesand.intellijplugin.lweintellijplugin.util.ClipboardUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.cert.PKIXReason;

/**
 * 结构体转JSON动作
 * 将选中的代码或剪贴板中的代码转换为JSON结构
 */
public class ConvertToJsonAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String text = getSelectedOrClipboardText(e);
        if (text == null || text.trim().isEmpty()) {
            GlobalNotifier.notifyWarning(project, "请先选择代码或复制代码到剪贴板");
            return;
        }

        String json = ConverterFactory.convertToJson(text);

        if  (json == null) {
            GlobalNotifier.notifyError(project, "未选中完整的结构");
            return;
        }

        //提交给LLM完善value值
        String adviceJson = json;
        //LLM补全效果并不好，耗时长，暂且不处理
//        try {
//            String prompt=PromptManager.getPrompt(PromptType.JSON_VALUE);
//            adviceJson = LLMApiFactory.getLLMApi().chatMessage(json,prompt);
//        } catch (IOException ex) {
//            GlobalNotifier.notifyError(project, ex.getMessage());
//        }

        if (adviceJson != null) {
            ClipboardUtil.setClipboardText(adviceJson);
            GlobalNotifier.notifyInfo(project, "已成功转换为JSON并复制到剪贴板");
        } else {
            GlobalNotifier.notifyWarning(project, "无法识别所选代码的结构");
        }
    }

    /**
     * 获取选中的文本或剪贴板中的文本
     *
     * @param e 动作事件
     * @return 文本内容
     */
    private String getSelectedOrClipboardText(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        // 如果有编辑器且有选中的文本，则使用选中的文本
        if (editor != null) {
            SelectionModel selectionModel = editor.getSelectionModel();
            if (selectionModel.hasSelection()) {
                return selectionModel.getSelectedText();
            }
        }

        // 否则尝试从剪贴板获取
        return ClipboardUtil.getClipboardText();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 控制动作的可用性
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}