package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf;

import com.google.gson.Gson;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class MsdfFont implements Imports {

    private final String name;
    private final ResourceLocation texture;

    private final FontUtils.AtlasData atlas;
    private final FontUtils.MetricsData metrics;

    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;

    @Setter private float size;

    public void draw(String text, float x, float y, float size, float thickness, float spacing, Color color) {
        Shader program = Shaders.msdf;





        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();

        program.start();

        mc.getTextureManager().bindTexture(texture);

        program.uniform("Sampler0", 0);
        program.uniform("Range", atlas.getRange());
        program.uniform("Thickness", thickness);
        program.uniform("Smoothness", 0.007f);
        program.uniform("Color", color);

        GL11.glBegin(GL11.GL_QUADS);

        int prevChar = -1;

        for (char c : text.toCharArray()) {
            MsdfGlyph msdfGlyph = glyphs.get((int) c);

            if (msdfGlyph == null) continue;

            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                x += kerning.getOrDefault((int) c, 0.0f) * size;
            }

            x += msdfGlyph.apply(x, y, size, color) + thickness + spacing;
            prevChar = c;

        }

        GL11.glEnd();

        ColorUtils.resetColor();
        GlStateManager.disableBlend();
        Shader.stop();
    }

    public void draw(String text, float x, float y, float thickness, float spacing, Color color) {
        draw(text, x, y, size, thickness, spacing, color);
    }

    public void draw(String text, float x, float y, float thickness, Color color) {
        draw(text, x, y, size, thickness, 0, color);
    }

    public void draw(String text, float x, float y, Color color) {
        draw(text, x, y, size, 1, 0, color);
    }

    public float width(String text) {
        return width(text, size);
    }

    public float width(String text, float size) {
        int prevChar = -1;
        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            int _char = text.charAt(i);
            MsdfGlyph glyph = this.glyphs.get(_char);

            if (glyph == null) continue;

            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                width += kerning.getOrDefault(_char, 0.0f) * size;
            }

            width += glyph.getWidth(size);
            prevChar = _char;
        }

        return width;
    }

    private static final ResourceLocation FONTS_LOCATION = Client.INST.of("fonts/");

    public static MsdfFont create(String name) {
        ResourceLocation fontLocation = new ResourceLocation(FONTS_LOCATION + name + "/");

        ResourceLocation dataLocation = new ResourceLocation(fontLocation + "data.json");
        ResourceLocation atlasLocation = new ResourceLocation(fontLocation + "atlas.png");

        FontUtils.FontData data = fromJsonToInstance(dataLocation, FontUtils.FontData.class);

        float aWidth = data.getAtlas().getWidth();
        float aHeight = data.getAtlas().getHeight();
        Map<Integer, MsdfGlyph> glyphs = data.getGlyphs().stream()
            .collect(Collectors.<FontUtils.GlyphData, Integer, MsdfGlyph>toMap(
                FontUtils.GlyphData::getUnicode,
                (glyphData) -> new MsdfGlyph(glyphData, aWidth, aHeight)
            ));

        Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();
        data.getKernings().forEach((kerning) -> {
            Map<Integer, Float> map = kernings.computeIfAbsent(kerning.getLeftChar(), _ -> new HashMap<>());
            map.put(kerning.getRightChar(), kerning.getAdvance());
        });

        return new MsdfFont(name, atlasLocation, data.getAtlas(), data.getMetrics(), glyphs, kernings, 16);
    }

    private static final IResourceManager RESOURCE_MANAGER = mc.getResourceManager();
    private static final Gson GSON = new Gson();

    private static <T> T fromJsonToInstance(ResourceLocation identifier, Class<T> clazz) {
        return GSON.fromJson(toString(identifier), clazz);
    }

    public static String toString(ResourceLocation identifier) {
        return toString(identifier, "\n");
    }

    public static String toString(ResourceLocation identifier, String delimiter) {
        try (InputStream inputStream = RESOURCE_MANAGER.getResource(identifier).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining(delimiter));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
