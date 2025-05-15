package me.hackclient.utils.render.fontTest;

import me.hackclient.utils.color.ColorUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class FontRenderer {
    private int imageSize = 1024;
    private int charsAmount = 1024;

    private CharData[] chars = new CharData[charsAmount];
    private CharData[] bold = new CharData[charsAmount];
    private CharData[] italic = new CharData[charsAmount];

    private Font font;

    private boolean antiAlias;
    private boolean fractionalMetrics;

    private int fontHeight;
    private int charOffset;

    private DynamicTexture texture, textureBold, textureItalic;

    private final int fontScaleOffset = 2;
    private final int[] colorCode = new int[32];
    private final String colorCodeIdentifiers = "0123456789abcdefklmnor";

    public FontRenderer(Font font) {
        this(font, true, true);
    }

    public FontRenderer(Font font, boolean antiAlias) {
        this(font, antiAlias, true);
    }

    public FontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;

        this.texture = genDynamicTexture(font.deriveFont(Font.PLAIN), antiAlias, fractionalMetrics, chars);
        this.textureBold = genDynamicTexture(font.deriveFont(Font.BOLD), antiAlias, fractionalMetrics, chars);
        this.textureItalic = genDynamicTexture(font.deriveFont(Font.ITALIC), antiAlias, fractionalMetrics, chars);
    }

    private DynamicTexture genDynamicTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage image = genTexture(font, antiAlias, fractionalMetrics, chars);

        return new DynamicTexture(image);
    }

    private BufferedImage genTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage image = new BufferedImage(imageSize, imageSize, 2);
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        graphics.setFont(font);

        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, imageSize, imageSize);
        graphics.setColor(Color.white);

        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        FontMetrics fontMetrics = graphics.getFontMetrics();

        int charHeight = 0;
        int posX = 0;
        int posY = 0;

        int i = 0;

        while (i < chars.length) {
            char ch = (char) i;
            CharData charData = new CharData();
            Rectangle2D rect = fontMetrics.getStringBounds(String.valueOf(ch), graphics);

            charData.width = rect.getBounds().width + 8;
            charData.height = rect.getBounds().height;


            if (posX + charData.width >= imageSize) {
                posX = 0;
                posY += charHeight;
            }

            if (charData.width > charHeight) {
                charHeight = charData.height;
            }

            charData.x = posX;
            charData.y = posY;

            if (charData.height > fontHeight) {
                fontHeight = charData.height;
            }

            chars[i] = charData;
            graphics.drawString(String.valueOf(ch), posX + 2, posY + fontMetrics.getAscent());
            posX += charData.width;
            i++;
        }

        return image;
    }

    public float drawString(String text, float x, float y, Color color) {
        return this.drawString(text, x, y, color, false, 9f);
    }

    public float drawString(String text, float x, float y, Color color, boolean shadow, float kerning) {
        if (text == null) {
            return 0;
        }

        CharData[] currentData = chars;
        boolean random = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean render = true;
        x *= 2 * fontScaleOffset;
        y = (y - 3) * 2 * fontScaleOffset;
        GL11.glPushMatrix();
        GlStateManager.scale(0.5 / fontScaleOffset, 0.5 / fontScaleOffset, 0.5 / fontScaleOffset);
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        ColorUtils.glColor(color);
        int size = text.length();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.texture.getGlTextureId());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        int i2 = 0;

        while (i2 < size) {

            char character = text.charAt(i2);

            if (character == '\u00a7' && i2 < size) {
                int colorIndex = 21;

                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i2 + 1));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    random = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(this.texture.getGlTextureId());
                    currentData = this.chars;

                    if (colorIndex < 0 || colorIndex > 15) {
                        colorIndex = 15;
                    }

                    if (shadow) {
                        colorIndex += 16;
                    }

                    int colorcode = this.colorCode[colorIndex];

                    GlStateManager.color((float) (colorcode >> 16 & 255) / 255.0f, (float) (colorcode >> 8 & 255) / 255.0f, (float) (colorcode & 255) / 255.0f, color.getAlpha() / 255f);
                } else if (colorIndex == 16) {
                    random = true;
                } else if (colorIndex == 17) {
                    bold = true;

                    if (italic) {
                        GlStateManager.bindTexture(this.textureItalic.getGlTextureId());
                        currentData = this.italic;
                    } else {
                        GlStateManager.bindTexture(this.textureBold.getGlTextureId());
                        currentData = this.bold;
                    }
                } else if (colorIndex == 18) {
                    strikethrough = true;
                } else if (colorIndex == 19) {
                    underline = true;
                } else if (colorIndex == 20) {
                    italic = true;


                    GlStateManager.bindTexture(this.textureItalic.getGlTextureId());
                    currentData = this.italic;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    random = false;
                    underline = false;
                    strikethrough = false;
                    ColorUtils.glColor(color);
                    GlStateManager.bindTexture(this.texture.getGlTextureId());
                    currentData = this.chars;
                }

                ++i2;
            } else if (character < currentData.length && character >= '\u0000') {
                GL11.glBegin(GL11.GL_TRIANGLES);
                this.drawChar(currentData, character, x, y);
                GL11.glEnd();

                x += (double) (currentData[character].width - 8.3f + this.charOffset);
            }

            ++i2;
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);

        GL11.glPopMatrix();
        GlStateManager.resetColor();

        return x / 2.0f;
    }

    public void drawChar(CharData[] chars, char c2, float x2, float y2) throws ArrayIndexOutOfBoundsException {
        try {
            this.drawQuad(x2, y2, chars[c2].width, chars[c2].height, chars[c2].x, chars[c2].y, chars[c2].width, chars[c2].height);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void drawQuad(float x2, float y2, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight) {
        float renderSRCX = srcX / this.imageSize;
        float renderSRCY = srcY / this.imageSize;
        float renderSRCWidth = srcWidth / this.imageSize;
        float renderSRCHeight = srcHeight / this.imageSize;
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x2 + width, y2);
        GL11.glTexCoord2f(renderSRCX, renderSRCY);
        GL11.glVertex2d(x2, y2);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x2, y2 + height);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x2, y2 + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x2 + width, y2 + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x2 + width, y2);
    }
}
