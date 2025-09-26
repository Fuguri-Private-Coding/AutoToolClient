package fuguriprivatecoding.autotoolrecode.utils.font;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class Fonts implements Imports {

    public HashMap<String, ClientFontRenderer> fonts = new HashMap<>();

    File fontsDirectory = new File(Client.INST.getName() + "/fonts");

    int fontsCount;

    public Fonts() {
        if (!fontsDirectory.exists()) {
            fontsDirectory.mkdirs();
            downloadFonts();
            initFonts();
            return;
        }

        if (fontsDirectory.listFiles() != null && fontsDirectory.listFiles().length > 0) {
            initFonts();
        } else {
            downloadFonts();
            initFonts();
        }
    }

    private void addDownloadDelay() {
        try {
            Thread.sleep(1000);

            int maxAttempts = 10;
            int attempt = 0;
            while (attempt < maxAttempts) {
                if (fontsDirectory.listFiles() != null && fontsDirectory.listFiles().length >= fontsCount) {
                    System.out.println("Шрифты успешно загружены, найдено файлов: " + fontsDirectory.listFiles().length);
                    break;
                }
                Thread.sleep(500);
                attempt++;
            }
        } catch (InterruptedException e) {
            System.out.println("Задержка загрузки шрифтов прервана: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void initFonts() {
        File[] fontFiles = fontsDirectory.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".ttf")
        );

        if (fontFiles == null) return;

        for (File fontFile : fontFiles) {
            try {
                String fontName = getFileNameWithoutExtension(fontFile);
                Font font = generateFontFromFile(fontFile, 32, true);
                if (font != null) fonts.put(fontName, new ClientFontRenderer(font));
            } catch (Exception e) {
                System.out.println("Ошибка загрузки шрифта " + fontFile.getName() + ": " + e.getMessage());
            }
        }
    }

    private void downloadFonts() {
        try {
            MessageChannel fontsChannel = Client.INST.getIrc().getFontsChannel();

            List<Message> messages = fontsChannel.getIterableHistory().stream().toList();

            for (Message message : messages) {
                if (message.getAttachments().isEmpty()) continue;

                fontsCount++;

                message.getAttachments().forEach(attachment -> {
                    if (attachment.getFileName().endsWith(".ttf") || attachment.getFileName().endsWith(".otf")) {
                        attachment.getProxy().downloadToFile(new File(fontsDirectory + "/" + attachment.getFileName()))
                            .thenAccept(_ -> System.out.println("Successful installed: " + attachment.getFileName()));
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Failed download fonts: " + e.getMessage());
        }
        addDownloadDelay();
    }

    private String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex > 0) return name.substring(0, lastIndex);
        return name;
    }

    public Font generateFontFromFile(File fontFile, float size, boolean bold) {
        try {
            InputStream inputStream = new FileInputStream(fontFile);
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            return font.deriveFont(bold ? Font.BOLD : Font.PLAIN, size);
        } catch (Exception e) {
            System.out.println("Ошибка создания шрифта из файла " + fontFile.getName() + ": " + e.getMessage());
        }
        return null;
    }
}
