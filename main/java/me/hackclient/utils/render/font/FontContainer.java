package me.hackclient.utils.render.font;

import me.hackclient.utils.interfaces.Imports;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class FontContainer implements Imports {
    private TrueTypeFont textFont = null;

    public boolean useCustomFont = true;

    private FontContainer() {}

    public FontContainer(ResourceLocation dir, String fontType, int fontSize) {
        this.textFont = new TrueTypeFont(new Font(fontType, 0, fontSize), 1.0F);
        this.useCustomFont = !fontType.equalsIgnoreCase("minecraft");
        try {
            if (!this.useCustomFont || fontType.isEmpty() || fontType.equalsIgnoreCase("default"))
                this.textFont = new TrueTypeFont(dir, fontSize, 1.0F);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public int height(String text) {
        if (this.useCustomFont)
            return this.textFont.height(text);
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    public int width(String text) {
        if (this.useCustomFont)
            return this.textFont.width(text);
        return mc.fontRendererObj.getStringWidth(text);
    }

    public FontContainer copy() {
        FontContainer font = new FontContainer();
        font.textFont = this.textFont;
        font.useCustomFont = this.useCustomFont;
        return font;
    }

    public void drawString(String text, float x, float y, int color) {
        if (this.useCustomFont) {
            this.textFont.draw(text, x, y, color);
        } else {
            mc.fontRendererObj.drawString(text, (int) x, (int) y, color);
        }
    }

    public void drawString(String text, float x, float y, int color, boolean shadow) {
        if (shadow) drawString(text, x + 0.5f, y + 0.5f, color);
        drawString(text, x, y, color);
    }

    public String getName() {
        if (!this.useCustomFont)
            return "Minecraft";
        return this.textFont.getFontName();
    }

    public void clear() {
        if (this.textFont != null)
            this.textFont.dispose();
    }
}
