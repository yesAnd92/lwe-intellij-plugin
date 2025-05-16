package cn.yesand.intellijplugin.lweintellijplugin.action;

import com.intellij.execution.impl.ConsoleViewUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import cn.yesand.intellijplugin.lweintellijplugin.GlobalNotifier;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertToSqlAction extends AnAction {

    private static final String SEPARATOR_PREPARING = "Preparing:";
    private static final String SEPARATOR_PARAMETER = "Parameters:";

    private static final String PREPARING_PATTERN = SEPARATOR_PREPARING + "(.*?)(?=\\n|\\r|\\r\\n)";
    private static final String PARAMETER_PATTERN = SEPARATOR_PARAMETER + "(.*?)(?=\\n|\\r|\\r\\n)";
    private static final String[] TYPES_NEED_QUOTES = { "String", "Timestamp", "Date", "Time" };

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            return;

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null)
            return;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            Messages.showInfoMessage(project, "请先在控制台中选择文本", "提示");
            return;
        }

        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.isEmpty())
            return;

        String sqlText = null;
        try {
            sqlText = parseMybatisSqlLog(selectedText);
        } catch (Exception ex) {
            GlobalNotifier.notifyError(project, ex.getMessage());
        }

        // 复制到剪贴板
        CopyPasteManager.getInstance().setContents(new StringSelection(sqlText));

        GlobalNotifier.notifyInfo(project, "SQL converted successfully and copied to the clipboard!");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean isConsoleEditor = editor != null && editor.getSelectionModel().hasSelection()
                && ConsoleViewUtil.isConsoleViewEditor(editor);
        e.getPresentation().setEnabled(isConsoleEditor);
    }

    private String parseMybatisSqlLog(String sqlLog) throws Exception {
        if (!sqlLog.endsWith("\n") && !sqlLog.endsWith("\r\n")) {
            sqlLog += "\n";
        }

        String prepare = extractPattern(PREPARING_PATTERN, sqlLog);
        if (prepare == null) {
            throw new Exception("parsing Preparing section: no match found");
        }

        prepare = prepare.replace(SEPARATOR_PREPARING, "");

        String param = extractPattern(PARAMETER_PATTERN, sqlLog);
        if (param == null) {
            throw new Exception("parsing Parameters section: no match found");
        }

        String[] params = param.replace(SEPARATOR_PARAMETER, "").split(",");

        List<String> values = new ArrayList<>(params.length);
        for (String p : params) {
            values.add(extractValue(p));
        }

        int placeholderCount = countOccurrences(prepare, '?');
        if (placeholderCount != values.size()) {
            throw new Exception("mismatch between placeholders (?) and parameters count");
        }

        // Replace ? in prepare with values from values
        StringBuilder result = new StringBuilder();
        int paramIndex = 0;
        for (int i = 0; i < prepare.length(); i++) {
            char c = prepare.charAt(i);
            if (c == '?') {
                result.append(values.get(paramIndex));
                paramIndex++;
            } else {
                result.append(c);
            }
        }

        return result.toString().trim();
    }

    private static boolean needQuotes(String s) {
        for (String t : TYPES_NEED_QUOTES) {
            if (s.contains(t)) {
                return true;
            }
        }
        return false;
    }

    private static String extractPattern(String patternStr, String log) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(log);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    private static int countOccurrences(String str, char target) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private static String extractValue(String s) {
        s = s.trim();
        int lastOpenParen = -1;
        int parenCount = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                if (parenCount == 0) {
                    lastOpenParen = i;
                }
                parenCount++;
            } else if (c == ')') {
                parenCount--;
                if (parenCount == 0 && lastOpenParen != -1) {
                    String innerContent = s.substring(lastOpenParen + 1, i);
                    String result = s.substring(0, lastOpenParen).trim();
                    if (needQuotes(innerContent)) {
                        return "'" + result + "'";
                    }
                    return result;
                }
            }
        }

        return s;
    }
}