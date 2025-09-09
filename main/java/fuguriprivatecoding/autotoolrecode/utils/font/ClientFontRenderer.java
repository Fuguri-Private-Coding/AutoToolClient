package fuguriprivatecoding.autotoolrecode.utils.font;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ClientFontRenderer implements Imports {

    CharData[] charData = new CharData[65536];
    CharData[] boldChars = new CharData[65536];
    CharData[] italicChars = new CharData[65536];
    CharData[] boldItalicChars = new CharData[65536];

    boolean antiAlias, fractionalMetrics;
    final float imgSize = 1024f;
    Font font;

    public int FONT_HEIGHT;
    int charOffset;

    public String name;

    protected DynamicTexture tex;
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;

    public static double fontScaleOffset = 2;
    private final int[] colorCode = new int[32];

    public ClientFontRenderer(Font font) {
        this(font, true, true);
        this.name = font.getName();
        this.setupMinecraftColorCodes();
    }

    public ClientFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        this.setupMinecraftColorCodes();
        this.tex = setupTexture(font, antiAlias, fractionalMetrics, charData);

        this.texBold = this.setupTexture(font.deriveFont(Font.BOLD), antiAlias, fractionalMetrics, boldChars);
        this.texItalic = this.setupTexture(font.deriveFont(Font.ITALIC), antiAlias, fractionalMetrics, italicChars);
        this.texItalicBold = this.setupTexture(font.deriveFont(Font.BOLD | Font.ITALIC), antiAlias, fractionalMetrics, boldItalicChars);
    }

    private void setupMinecraftColorCodes() {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    protected DynamicTexture setupTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage img = this.generateFontImage(font, antiAlias, fractionalMetrics, chars);

        try {
            return new DynamicTexture(img);
        } catch (Exception e2) {
            System.out.println(e2.getMessage());
            return null;
        }
    }

    public double getStringWidth(String text) {
        if (text == null) {
            return 0;
        }

        char COLOR_INVOKER = '§';
        int width = 0;
        boolean bold = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == COLOR_INVOKER) {
                i++;
                if (i < text.length()) {
                    char colorChar = text.charAt(i);
                    if (colorChar == 'l') {
                        bold = true;
                    } else if (colorChar == 'r') {
                        bold = false;
                    }
                }
                continue;
            }

            if (c < charData.length && charData[c] != null) {
                CharData data = bold ? boldChars[c] != null ? boldChars[c] : charData[c] : charData[c];
                width += (int) (data.width - 8 + charOffset * fontScaleOffset);
            }
        }

        return width / (2 * fontScaleOffset);
    }

    BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        int imgSize = (int) this.imgSize;
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, 2);
        Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
        g2.setFont(font);
        g2.setColor(new Color(255, 255, 255, 0));
        g2.fillRect(0, 0, imgSize, imgSize);
        g2.setColor(Color.WHITE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        FontMetrics fontMetrics = g2.getFontMetrics();

        int charHeight = 0;
        int positionX = 0;
        int positionY = 0;

        for (int i = 0; i < chars.length; i++) {
            if (i > 255 && (i < 1024 || i > 1279)) {
                continue;
            }

            char ch = (char) i;
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g2);
            charData.width = (int) Math.ceil(dimensions.getWidth()) + 8;
            charData.height = (int) Math.ceil(dimensions.getHeight());

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight + 2;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > this.FONT_HEIGHT) {
                this.FONT_HEIGHT = 9;
            }

            chars[i] = charData;
            g2.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
        }

        g2.dispose();
        return bufferedImage;
    }

    public float drawString(String text, double x, double y, Color color) {
        return drawString(text, x, y, color.getRGB(), false);
    }

    public float drawCenteredString(String text, double x, double y, Color color) {
        return drawString(text, x - getStringWidth(text) / 2f, y, color.getRGB(), true);
    }

    public float drawString(String text, double x, double y, Color color, boolean shadow) {
        return drawString(text, x, y, color.getRGB(), shadow);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        if (text == null) {
            return 0.0f;
        }

        x -= 1.0;

        CharData[] currentData = this.charData;
        float alpha = (float) (color >> 24 & 255) / 255.0f;
        x *= 2.0 * fontScaleOffset;
        y = (y - 3.0) * 2.0 * fontScaleOffset;

        GL11.glPushMatrix();
        GlStateManager.scale(0.5 / fontScaleOffset, 0.5 / fontScaleOffset, 0.5 / fontScaleOffset);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        GlStateManager.color((float) (color >> 16 & 255) / 255.0f,
                (float) (color >> 8 & 255) / 255.0f,
                (float) (color & 255) / 255.0f, alpha);

        int size = text.length();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.tex.getGlTextureId());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tex.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glBegin(GL11.GL_QUADS);

        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);

            if (character == '§') {
                if (i + 1 < size) {
                    i++;
                    char colorChar = text.charAt(i);
                    int colorIndex = "0123456789abcdefklmnor".indexOf(colorChar);

                    if (colorIndex >= 0 && colorIndex <= 15) {
                        int colorcode = this.colorCode[colorIndex + (shadow ? 16 : 0)];
                        GlStateManager.color((float) (colorcode >> 16 & 255) / 255.0f,
                                (float) (colorcode >> 8 & 255) / 255.0f,
                                (float) (colorcode & 255) / 255.0f, alpha);

                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                    } else if (colorIndex == 20) {
                        GlStateManager.color((float) (color >> 16 & 255) / 255.0f,
                                (float) (color >> 8 & 255) / 255.0f,
                                (float) (color & 255) / 255.0f, alpha);
                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                    }
                }
            } else if (character < currentData.length && currentData[character] != null) {
                if (shadow) {
                    GlStateManager.color(0, 0, 0, alpha);
                    this.drawChar(currentData, character, (float) x + 1.5f, (float) y + 1.5f);
                    GlStateManager.color((float) (color >> 16 & 255) / 255.0f,
                            (float) (color >> 8 & 255) / 255.0f,
                            (float) (color & 255) / 255.0f, alpha);
                }

                this.drawChar(currentData, character, (float) x, (float) y);

                x += currentData[character].width - 8.3f + this.charOffset;
            }
        }

        GL11.glEnd();
        GL11.glPopMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        return (float) x / 2.0f;
    }

    public void drawChar(CharData[] chars, char c2, float x2, float y2) {
        try {
            this.drawQuad(x2, y2, chars[c2].width, chars[c2].height,
                    chars[c2].storedX, chars[c2].storedY,
                    chars[c2].width, chars[c2].height);
        } catch (Exception e2) {
            System.out.println("Error drawing char: " + e2.getMessage());
        }
    }

    protected void drawQuad(float x2, float y2, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight) {
        float renderSRCX = srcX / this.imgSize;
        float renderSRCY = srcY / this.imgSize;
        float renderSRCWidth = srcWidth / this.imgSize;
        float renderSRCHeight = srcHeight / this.imgSize;

        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2f(x2 + width, y2);

        GL11.glTexCoord2f(renderSRCX, renderSRCY);
        GL11.glVertex2f(x2, y2);

        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2f(x2, y2 + height);

        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        GL11.glVertex2f(x2 + width, y2 + height);
    }

    static class CharData {
        public int width;
        public int height;
        public int storedX;
        public int storedY;
    }
}