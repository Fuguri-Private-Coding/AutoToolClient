package fuguriprivatecoding.autotoolrecode.utils.font;

import fuguriprivatecoding.autotoolrecode.Client;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FontRender {
    private final Font font;
    private final float size;
    private final boolean antiAlias;
    private final int superSampling;

    private final Map<String, Integer> textureCache = new ConcurrentHashMap<>();
    private final Map<Character, CharMeta> charMetaCache = new ConcurrentHashMap<>();
    private final int[] colorCode = new int[32];

    private static final int SUPER_SAMPLING = 8;

    public FontRender(File fontFile, float size, int superSampling, boolean antiAlias) {
        this.size = size;
        this.antiAlias = antiAlias;
        this.superSampling = superSampling;

        try {
            System.out.println("Loading Ultra HD 8x font: " + fontFile.getName());
            this.font = Font.createFont(Font.TRUETYPE_FONT, fontFile)
                    .deriveFont(size * superSampling);

            preloadEssentialCharacters();
            setupColorCodes();
            System.out.println("Ultra HD 8x Font loaded. Size: " + size + ", SuperSampling: " + superSampling);

        } catch (Exception e) {
            System.err.println("Failed to load font: " + e.getMessage());
            throw new RuntimeException("Failed to load font", e);
        }
    }

    private void preloadEssentialCharacters() {
        String essentialChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:',.<>/?\\\" ";

        System.out.println("Preloading essential characters in 8x HD...");
        for (char c : essentialChars.toCharArray()) {
            if (font.canDisplay(c)) {
                getCharTexture(c, 0xFFFFFF, false, false);
            }
        }
    }

    private void setupColorCodes() {
        for (int i = 0; i < 32; i++) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) k += 85;
            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    public void drawCenteredString(String text, float x, float y, Color color, boolean dropShadow) {
        drawString(text, x - getStringWidth(text) / 2f, y, color, dropShadow);
    }

    public void drawCenteredString(String text, float x, float y, Color color) {
        drawString(text, x - getStringWidth(text) / 2f, y, color, false);
    }

    public void drawString(String text, float x, float y, Color color) {
        drawString(text, x, y, color, false);
    }

    public float drawString(String text, float x, float y, Color color, boolean dropShadow) {
        if (text == null || text.isEmpty()) return 0;

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        try {
            GlStateManager.scale(1.0 / superSampling, 1.0 / superSampling, 1.0);
            x *= superSampling;
            y *= superSampling;

            y += getVerticalOffset();

            if (dropShadow) {
                renderText(text, x + superSampling, y + superSampling - 15, 0x99000000, true);
            }

            return (renderText(text, x, y - 15, color.getRGB(), true) / superSampling);
        } finally {
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    private float getVerticalOffset() {
        return (superSampling - 1) * 2f;
    }

    private float renderText(String text, float x, float y, int color, boolean render) {
        float originalX = x;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;

        char[] chars = text.toCharArray();

        if (render) {
            setupUltraQualityRendering();
        }

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '§' && i + 1 < chars.length) {
                handleFormatting(chars[i + 1], color, bold, italic, strikethrough, underline);
                i++;
                continue;
            }

            if (render) {
                renderUltraQualityChar(c, x, y, color, bold, italic);
            }

            CharMeta meta = getCharMeta(c, bold, italic);
            x += meta.width / (float) superSampling;
        }

        return x - originalX;
    }

    private void handleFormatting(char formatChar, int currentColor, boolean bold, boolean italic,
                                  boolean strikethrough, boolean underline) {
        int codeIndex = "0123456789abcdefklmnor".indexOf(formatChar);
        if (codeIndex >= 0 && codeIndex <= 15) {
            currentColor = this.colorCode[codeIndex] | (currentColor & 0xFF000000);
            bold = false;
            italic = false;
            strikethrough = false;
            underline = false;
        } else if (codeIndex == 16) {
            bold = true;
        } else if (codeIndex == 17) {
            italic = true;
        } else if (codeIndex == 18) {
            strikethrough = true;
        } else if (codeIndex == 19) {
            underline = true;
        } else if (codeIndex == 20) {
            italic = true;
        } else if (codeIndex == 21) {
            currentColor = 0xFFFFFF;
            bold = false;
            italic = false;
            strikethrough = false;
            underline = false;
        }
    }

    private void setupUltraQualityRendering() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
    }

    private void renderUltraQualityChar(char c, float x, float y, int color, boolean bold, boolean italic) {
        if (!font.canDisplay(c) || Character.isWhitespace(c)) return;

        int textureId = getCharTexture(c, color, bold, italic);
        if (textureId == -1) return;

        CharMeta meta = getCharMeta(c, bold, italic);

        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        GlStateManager.color(
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f,
                alpha
        );

        GlStateManager.bindTexture((int) textureId);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        float width = meta.width / (float) superSampling;
        float height = meta.height / (float) superSampling;

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0).tex(0, 1).endVertex();
        worldRenderer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        worldRenderer.pos(x + width, y, 0).tex(1, 0).endVertex();
        worldRenderer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    private int getCharTexture(char c, int color, boolean bold, boolean italic) {
        String textureKey = c + "|" + color + "|" + bold + "|" + italic;
        return textureCache.computeIfAbsent(textureKey, k -> (int) createUltraQualityTexture(c, color, bold, italic));
    }

    private float createUltraQualityTexture(char c, int color, boolean bold, boolean italic) {
        try {
            BufferedImage hdImage = renderCharToHDImage(c, color, bold, italic);
            if (hdImage == null) return -1;

            ByteBuffer buffer = convertImageToBuffer(hdImage);

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, hdImage.getWidth(), hdImage.getHeight(),
                    0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            return textureId;

        } catch (Exception e) {
            System.err.println("Failed to create ultra quality texture for: " + c);
            return -1;
        }
    }

    private BufferedImage renderCharToHDImage(char c, int color, boolean bold, boolean italic) {
        try {
            Font renderFont = font;
            int style = Font.PLAIN;
            if (bold) style |= Font.BOLD;
            if (italic) style |= Font.ITALIC;
            renderFont = renderFont.deriveFont(style);

            // Создаем временный Graphics для точного измерения
            BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempG = temp.createGraphics();
            setupUltraQualityGraphics(tempG);
            tempG.setFont(renderFont);

            FontMetrics metrics = tempG.getFontMetrics();

            // Увеличиваем padding для 8x суперсэмплинга
            int width = Math.max(metrics.charWidth(c) + 16, 1);
            int height = Math.max(metrics.getHeight() + 16, 1);

            tempG.dispose();

            // Создаем HD изображение
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            setupUltraQualityGraphics(g);

            g.setFont(renderFont);
            g.setColor(new java.awt.Color(color, true));

            // Точное позиционирование для 8x качества
            float xPos = 8.0f;
            float yPos = metrics.getAscent() + 8.0f;

            g.drawString(String.valueOf(c), xPos, yPos);
            g.dispose();

            // Сохраняем метаданные с правильными размерами
            CharMeta meta = new CharMeta();
            meta.width = width;
            meta.height = height;
            meta.baseLine = metrics.getAscent();
            charMetaCache.put(c, meta);

            return image;

        } catch (Exception e) {
            System.err.println("Failed to render HD char: " + c);
            return null;
        }
    }

    private void setupUltraQualityGraphics(Graphics2D g) {
        // Максимальные настройки качества для 8x рендеринга
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        }
    }

    private ByteBuffer convertImageToBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();
        return buffer;
    }

    private CharMeta getCharMeta(char c, boolean bold, boolean italic) {
        return charMetaCache.getOrDefault(c, new CharMeta());
    }

    public float getStringWidth(String text) {
        if (text == null) return 0;
        return (renderText(text, 0, 0, 0xFFFFFF, false) / superSampling);
    }

    public float getCharWidth(char c) {
        return ((float) getCharMeta(c, false, false).width / superSampling);
    }

    public float getCharHeight() {
        return (size * 1.5f);
    }

    public float FONT_HEIGHT() {
        return getCharHeight();
    }

    public void cleanup() {
        textureCache.values().forEach(GL11::glDeleteTextures);
        textureCache.clear();
        charMetaCache.clear();
    }

    private static class CharMeta {
        public int width = 8;
        public int height = 8;
        public int baseLine = 6;
    }

    public static class FontManager {
        private static final Map<String, FontRender> fonts = new ConcurrentHashMap<>();

        public static FontRender getFont(String name, float size, int superSampling, boolean antiAlias) {
            String key = name + "|" + size + "|" + antiAlias;
            return fonts.computeIfAbsent(key, k -> {
                try {
                    File fontFile = new File(Client.INST.getClientDirectory() + "/fonts/" + name + ".ttf");
                    return new FontRender(fontFile, size, superSampling, antiAlias);
                } catch (Exception e) {
                    System.err.println("Failed to create Ultra HD font: " + e.getMessage());
                    return null;
                }
            });
        }

        public static FontRender getFont(String name, float size, boolean antiAlias) {
            return getFont(name, size, SUPER_SAMPLING, antiAlias);
        }

        public static void cleanupAll() {
            fonts.values().forEach(FontRender::cleanup);
            fonts.clear();
        }
    }
}