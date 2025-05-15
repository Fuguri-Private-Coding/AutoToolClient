package me.hackclient.utils.render.fontTest;

import me.hackclient.utils.interfaces.Imports;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.*;

@Deprecated
public class Fonts implements Imports {
    private static final int MAX_FONT_SIZE = 48;

    public static FontRenderer[] roboto = new FontRenderer[MAX_FONT_SIZE];
    public static FontRenderer[] jetBrains = new FontRenderer[MAX_FONT_SIZE];

    public static void init() {
        processFont(roboto, "Roboto");
        processFont(jetBrains, "JetBrains");
    }

    private static void processFont(FontRenderer[] fontRenderer, String name) {
        for (int i = 1; i <= MAX_FONT_SIZE; i++) {
            fontRenderer[i - 1] = new FontRenderer(getResource(i, new ResourceLocation(getLoc(name))));
        }
    }

    private static String getLoc(String fontName) {
        return "hackclient/fonts/" + fontName + ".ttf";
    }

    private static Font getResource(float size, ResourceLocation location) {
        Font font = null;
        size *= 2;


        try {
            InputStream inputStream = mc.getResourceManager()
                    .getResource(location).getInputStream();

            font = Font.createFont(Font.PLAIN, inputStream);
            font = font.deriveFont(Font.PLAIN, size);
        } catch (IOException | FontFormatException e) {
            System.out.println(e.getMessage());
        }

        return font;
    }
}
