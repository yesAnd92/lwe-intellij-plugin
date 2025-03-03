package cn.yesand.intellijplugin.lweintellijplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.CommitMessage;

import javax.swing.*;
import java.awt.*;

public class AiCommitAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Project project = e.getProject();
        if (project == null) return;

        // get the commit message component
        Object commitMessageData = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());


        CommitMessage commitMessage = null;
        if (commitMessageData instanceof CommitMessage) {
            commitMessage = (CommitMessage) commitMessageData;
        }

        commitMessage.setCommitMessage("yesand msg");
    }

}
