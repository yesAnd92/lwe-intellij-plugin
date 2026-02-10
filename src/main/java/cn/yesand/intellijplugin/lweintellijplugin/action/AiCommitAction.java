package cn.yesand.intellijplugin.lweintellijplugin.action;

import cn.yesand.intellijplugin.lweintellijplugin.LLMApiFactory;
import cn.yesand.intellijplugin.lweintellijplugin.StreamResponseCallback;
import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager;
import cn.yesand.intellijplugin.lweintellijplugin.prompt.PromptManager.PromptType;
import cn.yesand.intellijplugin.lweintellijplugin.settings.AiCommitSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import javax.swing.*;
import java.io.File;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AiCommitAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();
        if (project == null)
            return;

        AbstractCommitWorkflowHandler commitWorkflowHandler = (AbstractCommitWorkflowHandler) e
                .getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        List<Change> includedChanges = commitWorkflowHandler.getUi().getIncludedChanges();

        String diff = computeDiff(includedChanges, false, project);

        // get the commit message component
        CommitMessage commitMessage = (CommitMessage) VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());
        if (commitMessage != null) {
            commitMessage.setCommitMessage("");
            // 启用软换行，使大模型返回的长 commit message 在输入框内自动换行显示
            Editor editor = commitMessage.getEditorField().getEditor();
            if (editor != null) {
                editor.getSettings().setUseSoftWraps(true);
            }
        }

        try {
            AiCommitSettings settings = ApplicationManager.getApplication().getService(AiCommitSettings.class);
            // replace prompt {language} with locale language
            String defaultLanguage = settings.getLocale();
            if (defaultLanguage == null || defaultLanguage.isEmpty()) {
                defaultLanguage = "English";
            }
            String prompt = PromptManager.getPrompt(PromptType.GIT_DIFF).replace("{language}", defaultLanguage);
            // stream call
            LLMApiFactory.getLLMApi().chatMessage(diff, prompt, new StreamResponseCallback() {
                // 用于跟踪消息状态的变量
                private StringBuilder messageBuffer = new StringBuilder();
                
                public void onMessage(String message) {
                    if (message == null || message.trim().isEmpty()) {
                        return;
                    }
                    
                    // 将消息添加到缓冲区
                    messageBuffer.append(message);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (commitMessage != null && commitMessage.isDisplayable()) {
                            String normalized = normalizeCommitMessage(messageBuffer.toString());
                            commitMessage.setCommitMessage(normalized);
                            commitMessage.getEditorField().getCaretModel().moveToOffset(normalized.length());
                        }
                    });
                }
                
                @Override
                public void onComplete() {
                    SwingUtilities.invokeLater(() -> {
                        if (commitMessage != null) {
                            commitMessage.requestFocus();
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    Notification notification = new Notification(
                            "LWE Notification Group", // Changed group display ID
                            "AI Commit Failed", // Changed title
                            "Error: " + throwable.getMessage(),
                            NotificationType.ERROR
                    );
                    Notifications.Bus.notify(notification);
                }
            });
        } catch (Exception ex) {
            // 添加错误处理
            Notification notification = new Notification(
                    "LWE Notification Group",
                    "AI Commit Failed",
                    "发生错误: " + ex.getMessage(),
                    NotificationType.ERROR
            );
            Notifications.Bus.notify(notification);
        }
    }

    /**
     * 规范化 commit 消息：统一换行符为 \n，并去除模型可能输出的列表前缀（如 "1." "-"），
     * 确保每条 commit 在编辑框中单独一行显示。
     */
    private static String normalizeCommitMessage(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String unified = raw.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = unified.split("\n");
        Pattern listPrefix = Pattern.compile("^(\\d+[.)]\\s*|-\\s*)");
        String result = Arrays.stream(lines)
                .map(String::trim)
                .map(line -> listPrefix.matcher(line).replaceFirst(""))
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
        return result.isEmpty() ? unified.trim() : result;
    }

    /**
     * 从 Change 解析出对应的 VirtualFile，兼容新增文件（getVirtualFile() 可能为 null 时从 ContentRevision 取路径）。
     */
    private static VirtualFile getVirtualFileForChange(Change change) {
        VirtualFile vf = change.getVirtualFile();
        if (vf != null) return vf;
        ContentRevision after = change.getAfterRevision();
        if (after != null && after.getFile() != null) {
            vf = LocalFileSystem.getInstance().findFileByIoFile(new File(after.getFile().getPath()));
            if (vf != null) return vf;
        }
        ContentRevision before = change.getBeforeRevision();
        if (before != null && before.getFile() != null) {
            vf = LocalFileSystem.getInstance().findFileByIoFile(new File(before.getFile().getPath()));
            if (vf != null) return vf;
        }
        return null;
    }

    public static String computeDiff(List<Change> includedChanges, boolean reversePatch, Project project) {
        GitRepositoryManager gitRepositoryManager = GitRepositoryManager.getInstance(project);

        // 使用 getVirtualFileForChange 以便包含新增文件（getVirtualFile() 可能为 null 的 change）
        Map<GitRepository, List<Change>> changesByRepository = includedChanges.stream()
                .map(change -> {
                    VirtualFile file = getVirtualFileForChange(change);
                    if (file == null)
                        return null;
                    GitRepository repository = gitRepositoryManager.getRepositoryForFileQuick(file);
                    if (repository == null)
                        return null;
                    return new AbstractMap.SimpleEntry<>(repository, change);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        AbstractMap.SimpleEntry::getKey,
                        Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())));

        // get repo diff
        return changesByRepository.entrySet().stream()
                .map(entry -> {
                    GitRepository repository = entry.getKey();
                    List<Change> changes = entry.getValue();

                    if (repository != null) {
                        try {
                            List<FilePatch> filePatches = IdeaTextPatchBuilder.buildPatch(
                                    project,
                                    changes,
                                    repository.getRoot().toNioPath(),
                                    reversePatch,
                                    true);

                            StringWriter stringWriter = new StringWriter();
                            UnifiedDiffWriter.write(project, filePatches, stringWriter, "\n", null);
                            return stringWriter.toString();
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

}
