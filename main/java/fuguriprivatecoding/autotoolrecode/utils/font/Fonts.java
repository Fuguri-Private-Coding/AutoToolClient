package fuguriprivatecoding.autotoolrecode.utils.font;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Fonts implements Imports {

    public HashMap<String, ClientFontRenderer> fonts = new HashMap<>();

    File fontsDirectory = new File(Client.INST.getName() + "/fonts");

    int fontsCount;

    private void init() {
        checkFonts();
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

    private void checkFonts() {
        MessageChannel fontsChannel = Client.INST.getIrc().getFontsChannel();
        List<Message> messages = fontsChannel.getIterableHistory().stream().toList();
        for (Message message : messages) {
            if (!message.getAttachments().isEmpty()) fontsCount++;
        }
    }

    private void downloadFonts() {
        try {
            MessageChannel fontsChannel = Client.INST.getIrc().getFontsChannel();
            List<Message> messages = fontsChannel.getIterableHistory().stream().toList();

            List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();

            for (Message message : messages) {
                if (message.getAttachments().isEmpty()) continue;

                message.getAttachments().forEach(attachment -> {
                    if (attachment.getFileName().endsWith(".ttf") || attachment.getFileName().endsWith(".otf")) {
                        CompletableFuture<Void> future = attachment.getProxy().downloadToFile(new File(fontsDirectory + "/" + attachment.getFileName()))
                            .thenAccept(_ -> System.out.println("Successful installed: " + attachment.getFileName()));
                        downloadFutures.add(future);
                    }
                });
            }

            if (!downloadFutures.isEmpty()) {
                CompletableFuture<Void> allDownloads = CompletableFuture.allOf(
                    downloadFutures.toArray(new CompletableFuture[0])
                );

                allDownloads.get(30, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            System.out.println("Failed download fonts: " + e.getMessage());
        }
    }

    public void initFonts() {
        int maxAttempts = 10;
        int attempt = 0;

        while (attempt < maxAttempts) {
            File[] fontFiles = fontsDirectory.listFiles((_, name) -> name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));

            if (fontFiles != null && fontFiles.length >= fontsCount) {
                System.out.println("Шрифты успешно загружены, найдено файлов: " + fontFiles.length);
                break;
            }

            attempt++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }

        File[] fontFiles = fontsDirectory.listFiles((_, name) ->
            name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf")
        );

        if (fontFiles == null || fontFiles.length == 0) {
            System.out.println("Не удалось загрузить шрифты");
            return;
        }

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
