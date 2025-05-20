package cn.yesand.intellijplugin.lweintellijplugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import javax.swing.*;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        }

        try {
            // stream call
            LLMApiFactory.getLLMApi().chatMessage(diff,  new StreamResponseCallback() {
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
                            // 直接设置完整的消息
                            commitMessage.setCommitMessage(messageBuffer.toString());
                            
                            // 滚动到最后
                            commitMessage.getEditorField().getCaretModel().moveToOffset(messageBuffer.length());
        
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
                            "AI Commit LWE Notification Group", // Changed group display ID
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

    public static String computeDiff(List<Change> includedChanges, boolean reversePatch, Project project) {
        GitRepositoryManager gitRepositoryManager = GitRepositoryManager.getInstance(project);

        // filter out changes that don't have a virtual file or a repository
        Map<GitRepository, List<Change>> changesByRepository = includedChanges.stream()
                .filter(change -> !Objects.isNull(change.getVirtualFile()))
                .map(change -> {
                    VirtualFile file = change.getVirtualFile();
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
