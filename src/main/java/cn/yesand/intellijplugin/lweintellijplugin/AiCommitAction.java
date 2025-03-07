package cn.yesand.intellijplugin.lweintellijplugin;

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
        if (project == null) return;

        AbstractCommitWorkflowHandler commitWorkflowHandler = (AbstractCommitWorkflowHandler) e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        List<Change> includedChanges = commitWorkflowHandler.getUi().getIncludedChanges();
        for (Change change : includedChanges) {
//            System.out.println(change.getBeforeRevision());
//            System.out.println(change.getAfterRevision());
//            System.out.println(change.getVirtualFile().getPath());
        }
        String diff = computeDiff(includedChanges, false, project);

        // get the commit message component
        CommitMessage commitMessage = (CommitMessage) VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());

        commitMessage.setCommitMessage(diff);
    }


    public static String computeDiff(List<Change> includedChanges, boolean reversePatch, Project project) {
        GitRepositoryManager gitRepositoryManager = GitRepositoryManager.getInstance(project);

        // 过滤变更并按仓库分组
        Map<GitRepository, List<Change>> changesByRepository = includedChanges.stream()
                .filter(change -> !Objects.isNull(change.getVirtualFile()))
                .map(change -> {
                    VirtualFile file = change.getVirtualFile();
                    if (file == null) return null;
                    GitRepository repository = gitRepositoryManager.getRepositoryForFileQuick(file);
                    if (repository == null) return null;
                    return new AbstractMap.SimpleEntry<>(repository, change);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        AbstractMap.SimpleEntry::getKey,
                        Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())
                ));

        // 为每个仓库计算差异
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
                                    true
                            );

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
