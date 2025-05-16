package cn.yesand.intellijplugin.lweintellijplugin.settings;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import cn.yesand.intellijplugin.lweintellijplugin.LLMApiFactory;

public class AiCommitSettingsConfigurable implements Configurable {
    private JPanel mainPanel;
    private JBTextField openAiHostField;
    private JBTextField openAiSocketTimeoutField;
    private JBTextField openAiTokenField;
    private JBTextField openAiModelComboBox;
    private ComboBox<String> providerNameComboBox;
    private ComboBox<String> localeComboBox;
    private JButton verifyButton;

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

        JBLabel localeLabel = new JBLabel("Locale");
        localeLabel.setFont(localeLabel.getFont().deriveFont(Font.BOLD, 14));

        verifyButton = new JButton("Connection test");
        verifyButton.addActionListener(e -> verifyConfiguration());

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(aiApiLabel, 0)
                .addLabeledComponent("AI", providerNameComboBox = new ComboBox<>(new String[]{LLMApiFactory.PROVIDER_SILICONFLOW}))
                .addLabeledComponent("AI host", openAiHostField = new JBTextField())
                .addLabeledComponent("AI model", openAiModelComboBox = new JBTextField())
                .addLabeledComponent("AI token", openAiTokenField = new JBTextField())
                .addLabeledComponent("AI socket timeout", openAiSocketTimeoutField = new JBTextField())
                .addComponent(verifyButton)
                //添加prompt分割线
                .addVerticalGap(15)
                .addComponent(new JSeparator(SwingConstants.HORIZONTAL), 15)
                .addVerticalGap(15)
                .addComponent(localeLabel, 0)
                .addLabeledComponent("Locale", localeComboBox = new ComboBox<>(new String[]{ "English", "Chinese"}))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        loadSettings();
        return mainPanel;
    }

    private void verifyConfiguration() {
        String host = openAiHostField.getText();
        String token = openAiTokenField.getText();
        String model = openAiModelComboBox.getText();

        String provider = (String) providerNameComboBox.getSelectedItem();
        
        if (host.isEmpty() || token.isEmpty()) {
            Messages.showErrorDialog("AI host 和 token 不能为空", "验证失败");
            return;
        }
        
        // 在后台线程中执行验证，避免阻塞UI
        SwingUtilities.invokeLater(() -> {
            try {

                boolean isValid =  LLMApiFactory.getLLMApi(provider).sayHello(model, host, token);
                if (isValid) {
                    Messages.showInfoMessage("配置验证成功！连接到 AI 服务正常。", "验证成功");
                } else {
                    Messages.showErrorDialog("无法连接到 AI 服务，请检查配置。", "验证失败");
                }
            } catch (Exception e) {
                Messages.showErrorDialog("验证失败: " + e.getMessage(), "验证失败");
            }
        });
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
    }

    @Override
    public boolean isModified() {
        AiCommitSettings settings = getSettings(); 
        return !openAiHostField.getText().equals(settings.getAiHost())
                || !openAiSocketTimeoutField.getText().equals(String.valueOf(settings.getAiSocketTimeout()))
                || !openAiTokenField.getText().equals(settings.getAiToken())
                || !openAiModelComboBox.getText().equals(settings.getAiModel())
                || !localeComboBox.getSelectedItem().equals(settings.getLocale());
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
    }

    @Override
    public void reset() {
        loadSettings();
    }
}