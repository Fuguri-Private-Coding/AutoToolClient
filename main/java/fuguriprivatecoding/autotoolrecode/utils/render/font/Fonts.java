package fuguriprivatecoding.autotoolrecode.utils.render.font;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

@UtilityClass
public class Fonts implements Imports {

    public HashMap<String, ClientFont> fonts = new HashMap<>();

    public final File FONT_DIRECTORY = new File(Client.CLIENT_DIR + "/fonts");

    public void init() {
        if (FONT_DIRECTORY.mkdirs()) ClientUtils.chatLog("Успешно создал директорию для шрифтов.");
        initFonts();
    }

    public void initFonts() {
        File[] fontFiles = FONT_DIRECTORY.listFiles((_, name) ->
            name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf")
        );

        if (fontFiles == null || fontFiles.length == 0) {
            ClientUtils.chatLog("Не удалось загрузить шрифты");
            return;
        }

        for (File fontFile : fontFiles) {
            try {
                String fontName = getFileNameWithoutExtension(fontFile);
                Font font = generateFontFromFile(fontFile, 32, true);
                if (font != null) fonts.put(fontName, new ClientFont(font));
            } catch (Exception e) {
                ClientUtils.chatLog("Ошибка загрузки шрифта " + fontFile.getName() + ": " + e.getMessage());
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
            ClientUtils.chatLog("Ошибка создания шрифта из файла " + fontFile.getName() + ": " + e.getMessage());
        }
        return null;
    }
}
