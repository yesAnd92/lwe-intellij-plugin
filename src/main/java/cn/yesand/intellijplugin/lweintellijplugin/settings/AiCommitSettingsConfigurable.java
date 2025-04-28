package cn.yesand.intellijplugin.lweintellijplugin.settings;

import com.intellij.openapi.application.ApplicationManager; 
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AiCommitSettingsConfigurable implements Configurable {
    private JPanel mainPanel;
    private JBTextField openAiHostField;
    private JBTextField openAiSocketTimeoutField;
    private JBTextField openAiTokenField;
    private JBTextField openAiModelComboBox;
    private ComboBox<String> localeComboBox;
    private ComboBox<String> promptTypeComboBox;

    public AiCommitSettingsConfigurable() {
    }


    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Lwe";
    }

    @Override
    public @Nullable JComponent createComponent() {
        JBLabel aiApiLabel = new JBLabel("AI api");
        aiApiLabel.setFont(aiApiLabel.getFont().deriveFont(Font.BOLD, 14));

        JBLabel promptLabel = new JBLabel("Prompt");
        promptLabel.setFont(promptLabel.getFont().deriveFont(Font.BOLD, 14));

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(aiApiLabel, 0)
                .addLabeledComponent("AI host", openAiHostField = new JBTextField())
                .addLabeledComponent("AI model", openAiModelComboBox = new JBTextField())
                .addLabeledComponent("AI token", openAiTokenField = new JBTextField())
                .addLabeledComponent("AI socket timeout", openAiSocketTimeoutField = new JBTextField())
                //添加prompt分割线
                .addVerticalGap(15)
                .addComponent(new JSeparator(SwingConstants.HORIZONTAL), 15)
                .addVerticalGap(15)
                .addComponent(promptLabel, 0)
                .addLabeledComponent("Locale", localeComboBox = new ComboBox<>(new String[]{"", "en", "zh"}))
                .addLabeledComponent("Prompt", promptTypeComboBox = new ComboBox<>(new String[]{"Basic", "Conventional", "Emoji"}))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        loadSettings();
        return mainPanel;
    }

    private AiCommitSettings getSettings() { 
        return ApplicationManager.getApplication().getService(AiCommitSettings.class);
    }


    private void loadSettings() {
        AiCommitSettings settings = getSettings(); 
        openAiHostField.setText(settings.getAiHost());
        openAiSocketTimeoutField.setText(String.valueOf(settings.getAiSocketTimeout()));
        openAiTokenField.setText(settings.getAiToken());
        openAiModelComboBox.setText(settings.getAiModel());
        localeComboBox.setSelectedItem(settings.getLocale());
        promptTypeComboBox.setSelectedItem(settings.getPromptType());
    }

    @Override
    public boolean isModified() {
        AiCommitSettings settings = getSettings(); 
        return !openAiHostField.getText().equals(settings.getAiHost())
                || !openAiSocketTimeoutField.getText().equals(String.valueOf(settings.getAiSocketTimeout()))
                || !openAiTokenField.getText().equals(settings.getAiToken())
                || !openAiModelComboBox.getText().equals(settings.getAiModel())
                || !localeComboBox.getSelectedItem().equals(settings.getLocale())
                || !promptTypeComboBox.getSelectedItem().equals(settings.getPromptType());
    }

    @Override
    public void apply() throws ConfigurationException {
        AiCommitSettings settings = getSettings(); 
        settings.setAiHost(openAiHostField.getText());
        try {
            settings.setAiSocketTimeout(Integer.parseInt(openAiSocketTimeoutField.getText()));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Socket timeout must be a valid integer");
        }
        settings.setAiToken(openAiTokenField.getText());
        settings.setAiModel( openAiModelComboBox.getText());
        // 移除了空的 try-catch 块
        settings.setLocale((String) localeComboBox.getSelectedItem());
        settings.setPromptType((String) promptTypeComboBox.getSelectedItem());
    }

    @Override
    public void reset() {
        loadSettings();
    }
}