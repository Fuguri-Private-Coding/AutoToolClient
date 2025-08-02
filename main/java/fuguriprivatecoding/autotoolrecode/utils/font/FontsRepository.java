package fuguriprivatecoding.autotoolrecode.utils.font;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;

public class FontsRepository implements Imports {

    public HashMap<String, ClientFontRenderer> fonts = new HashMap<>();

    public FontsRepository() {
        initFonts();
    }

    public void initFonts() {
        fonts.put("JetBrains", new ClientFontRenderer(generateFont(getFile("JetBrains"), 32, true)));
        fonts.put("Roboto", new ClientFontRenderer(generateFont(getFile("Roboto"), 32, true)));
        fonts.put("MuseoSans", new ClientFontRenderer(generateFont(getFile("MuseoSans"), 32, true)));
    }

    public ResourceLocation getFile(String name) {
        return new ResourceLocation("minecraft", "hackclient/fonts/" + name + ".ttf");
    }

    public Font generateFont(ResourceLocation fontFile, float size, boolean bold) {
        try {
            InputStream inputStream = mc.getResourceManager().getResource(fontFile).getInputStream();
            Font font = Font.createFont(0, inputStream);
            return font.deriveFont(bold ? Font.BOLD : Font.PLAIN, size);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

}
