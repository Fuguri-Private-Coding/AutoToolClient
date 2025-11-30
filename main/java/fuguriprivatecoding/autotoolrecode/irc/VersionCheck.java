package fuguriprivatecoding.autotoolrecode.irc;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientVersion;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class VersionCheck {

    public static void validateClientVersion(MessageChannel versionChannel) {
        String serverVersion = versionChannel.getIterableHistory().stream()
            .map(Message::getContentRaw)
            .findFirst()
            .orElse("0.0.0");

        String[] ver = serverVersion.split("\\.");

        int global = Integer.parseInt(ver[0]);
        int version = Integer.parseInt(ver[1]);
        int microUpdate = Integer.parseInt(ver[2]);

        ClientVersion newVersion = new ClientVersion(global, version, microUpdate);

        if (Client.INST.CLIENT_VERSION.isLowerThen(newVersion)) {
            showVersionMismatchDialog(Client.INST.CLIENT_VERSION.toString(), serverVersion);
        }
    }

    private static void showVersionMismatchDialog(String currentVersion, String requiredVersion) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("<html><h3 style='color: #d35400;'>Требуется обновление клиента!</h3></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel versionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        versionPanel.setBackground(Color.WHITE);
        versionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        versionPanel.add(new JLabel("<html><b>Текущая версия:</b> <span style='color: red;'>" + currentVersion + "</span></html>"));
        versionPanel.add(new JLabel("<html><b>Доступна версия:</b> <span style='color: green;'>" + requiredVersion + "</span></html>"));

        JButton downloadButton = new JButton("📥 Скачать обновление");
        downloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downloadButton.setBackground(new Color(52, 152, 219));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        downloadButton.addActionListener(_ -> openDownloadPage(requiredVersion));

        JLabel instructionLabel = new JLabel("<html><div style='text-align: center; margin-top: 10px; color: #666;'>"
            + "Нажмите кнопку ниже для скачивания новой версии</div></html>");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(versionPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(instructionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(downloadButton);

        panel.add(contentPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
            null,
            panel,
            "А, ой что-то пошло не так :3",
            JOptionPane.PLAIN_MESSAGE
        );

        System.exit(-1);
    }

    private static void openDownloadPage(String version) {
        String url = "https://github.com/Fuguri-Private-Coding/AutoToolClient/releases/download/" + version + "/AutoToolClient.jar";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Скопируйте ссылку в браузер:"), BorderLayout.NORTH);

            JTextField urlField = new JTextField(url);
            urlField.setSelectionStart(0);
            urlField.setSelectionEnd(urlField.getText().length());
            errorPanel.add(urlField, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(null, errorPanel, "Открыть в браузере",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}