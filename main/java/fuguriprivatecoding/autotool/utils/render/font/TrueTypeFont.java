package fuguriprivatecoding.autotool.utils.render.font;

import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class TrueTypeFont implements Imports {
    enum GlyphType {
        NORMAL, COLOR, RANDOM, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET, OTHER;
    }

    private static final List<Font> allFonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());

    private List<Font> usedFonts = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, GlyphCache> textcache = (LinkedHashMap<String, GlyphCache>)new LRUHashMap(100);

    private Map<Character, Glyph> glyphcache = new HashMap<>();

    private List<TextureCache> textures = new ArrayList<>();

    private Font font;

    private int lineHeight = 1;

    private Graphics2D globalG = (Graphics2D)(new BufferedImage(1, 1, 2)).getGraphics();

    public float scale = 1.0F;

    private int specialChar = 167;

    public TrueTypeFont(Font font, float scale) {
        this.font = font;
        this.scale = scale;
        this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
    }

    public TrueTypeFont(ResourceLocation resource, int fontSize, float scale) throws IOException, FontFormatException {
        InputStream stream = mc.getResourceManager().getResource(resource).getInputStream();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font font = Font.createFont(0, stream);
        ge.registerFont(font);
        this.font = font.deriveFont(0, fontSize);
        this.scale = scale;
        this.globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.lineHeight = this.globalG.getFontMetrics(font).getHeight();
    }

    public void setSpecial(char c) {
        this.specialChar = c;
    }

    public void draw(String text, float x, float y, int color) {
        GlyphCache cache = getOrCreateCache(text);
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        GlStateManager.color(r, g, b, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(this.scale, this.scale, 1.0F);
        float i = 0.0F;
        for (Glyph gl : cache.glyphs) {
            if (gl.type != GlyphType.NORMAL) {
                if (gl.type == GlyphType.RESET) {
                    GlStateManager.color(r, g, b, 1.0F);
                    continue;
                }
                if (gl.type == GlyphType.COLOR)
                    GlStateManager.color((gl.color >> 16 & 0xFF) / 255.0F, (gl.color >> 8 & 0xFF) / 255.0F, (gl.color & 0xFF) / 255.0F, 1.0F);
                continue;
            }
            GlStateManager.bindTexture(gl.texture);
            drawTexturedModalRect(i, 0.0F, gl.x * textureScale(), gl.y * textureScale(), gl.width * textureScale(), gl.height * textureScale());
            i += gl.width * textureScale();
        }
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private GlyphCache getOrCreateCache(String text) {
        GlyphCache cache = this.textcache.get(text);
        if (cache != null)
            return cache;
        cache = new GlyphCache();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == this.specialChar && i + 1 < text.length()) {
                char next = text.toLowerCase(Locale.ENGLISH).charAt(i + 1);
                int index = "0123456789abcdefklmnor".indexOf(next);
                if (index >= 0) {
                    Glyph glyph = new Glyph();
                    if (index < 16) {
                        glyph.type = GlyphType.COLOR;
                        glyph.color = mc.fontRendererObj.getColorCode(next);
                    } else if (index == 16) {
                        glyph.type = GlyphType.RANDOM;
                    } else if (index == 17) {
                        glyph.type = GlyphType.BOLD;
                    } else if (index == 18) {
                        glyph.type = GlyphType.STRIKETHROUGH;
                    } else if (index == 19) {
                        glyph.type = GlyphType.UNDERLINE;
                    } else if (index == 20) {
                        glyph.type = GlyphType.ITALIC;
                    } else {
                        glyph.type = GlyphType.RESET;
                    }
                    cache.glyphs.add(glyph);
                    i++;
                    continue;
                }
            }
            Glyph g = getOrCreateGlyph(c);
            cache.glyphs.add(g);
            cache.width += g.width;
            cache.height = Math.max(cache.height, g.height);
            continue;
        }
        this.textcache.put(text, cache);
        return cache;
    }

    private Glyph getOrCreateGlyph(char c) {
        Glyph g = this.glyphcache.get(Character.valueOf(c));
        if (g != null)
            return g;
        TextureCache cache = getCurrentTexture();
        Font font = getFontForChar(c);
        FontMetrics metrics = this.globalG.getFontMetrics(font);
        g = new Glyph();
        g.width = Math.max(metrics.charWidth(c), 1);
        g.height = Math.max(metrics.getHeight(), 1);
        if (cache.x + g.width >= 512) {
            cache.x = 0;
            cache.y += this.lineHeight + 1;
            if (cache.y >= 512) {
                cache.full = true;
                cache = getCurrentTexture();
            }
        }
        g.x = cache.x;
        g.y = cache.y;
        cache.x += g.width + 3;
        this.lineHeight = Math.max(this.lineHeight, g.height);
        cache.g.setFont(font);
        cache.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cache.g.drawString(c + "", g.x, g.y + metrics.getAscent());
        g.texture = cache.textureId;
        TextureUtil.uploadTextureImage(cache.textureId, cache.bufferedImage);
        this.glyphcache.put(Character.valueOf(c), g);
        return g;
    }

    private TextureCache getCurrentTexture() {
        TextureCache cache = null;
        for (TextureCache t : this.textures) {
            if (!t.full) {
                cache = t;
                break;
            }
        }
        if (cache == null)
            this.textures.add(cache = new TextureCache());
        return cache;
    }

    public void drawCentered(String text, float x, float y, int color) {
        draw(text, x - width(text) / 2.0F, y, color);
    }

    private Font getFontForChar(char c) {
        if (this.font.canDisplay(c))
            return this.font;
        for (Font f : this.usedFonts) {
            if (f.canDisplay(c))
                return f;
        }
        Font fa = new Font("Arial Unicode MS", 0, this.font.getSize());
        if (fa.canDisplay(c))
            return fa;
        for (Font f : allFonts) {
            if (f.canDisplay(c)) {
                this.usedFonts.add(f = f.deriveFont(0, this.font.getSize()));
                return f;
            }
        }
        return this.font;
    }

    public void drawTexturedModalRect(float x, float y, float textureX, float textureY, float width, float height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        int zLevel = 0;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.noColor();
        worldRenderer.pos(x, (y + height), zLevel).tex((textureX * f), ((textureY + height) * f1)).endVertex();
        worldRenderer.pos((x + width), (y + height), zLevel).tex(((textureX + width) * f), ((textureY + height) * f1)).endVertex();
        worldRenderer.pos((x + width), y, zLevel).tex(((textureX + width) * f), (textureY * f1)).endVertex();
        worldRenderer.pos(x, y, zLevel).tex((textureX * f), (textureY * f1)).endVertex();
        Tessellator.getInstance().draw();
    }

    public int width(String text) {
        GlyphCache cache = getOrCreateCache(text);
        return (int)(cache.width * this.scale * textureScale());
    }

    public int height(String text) {
        if (text == null || text.trim().isEmpty())
            return (int)(this.lineHeight * this.scale * textureScale());
        GlyphCache cache = getOrCreateCache(text);
        return Math.max(1, (int)(cache.height * this.scale * textureScale()));
    }

    private float textureScale() {
        return 0.5F;
    }

    public void dispose() {
        for (TextureCache cache : this.textures)
            GlStateManager.bindTexture(cache.textureId);
        this.textcache.clear();
    }

    class TextureCache {
        int x;

        int y;

        int textureId = GlStateManager.generateTexture();

        BufferedImage bufferedImage = new BufferedImage(512, 512, 2);

        Graphics2D g = (Graphics2D)this.bufferedImage.getGraphics();

        boolean full;
    }

    class Glyph {
        GlyphType type = GlyphType.NORMAL;

        int color = -1;

        int x;

        int y;

        int height;

        int width;

        int texture;
    }

    class GlyphCache {
        public int width;

        public int height;

        List<Glyph> glyphs = new ArrayList<>();
    }

    public String getFontName() {
        return this.font.getFontName();
    }
}
