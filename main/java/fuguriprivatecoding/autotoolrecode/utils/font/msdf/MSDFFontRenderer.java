package fuguriprivatecoding.autotoolrecode.utils.font.msdf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import lombok.SneakyThrows;
import net.minecraft.util.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MSDFFontRenderer implements Imports {

    private final int atlasWidth, atlasHeight;
    private final ResourceLocation atlasTexture;

    private final Map<Integer, MSDFGlyph> glyphs = new HashMap<>();
    private final Map<String, Integer> kernings = new HashMap<>();

    @SneakyThrows
    public MSDFFontRenderer(String fontName) {
        ResourceLocation dataId = new ResourceLocation("minecraft", "autotool/fonts/"+fontName+"/data.json");
        atlasTexture = new ResourceLocation("minecraft", "autotool/fonts/"+fontName+"/atlas.png");

        InputStream is = mc.getResourceManager().getResource(dataId).getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        JsonObject mainObject = new JsonParser().parse(reader).getAsJsonObject();
        is.close();

        JsonObject common = mainObject.getAsJsonObject("common");

        this.atlasWidth = common.get("scaleW").getAsInt();
        this.atlasHeight = common.get("scaleH").getAsInt();

        for (JsonElement element : mainObject.getAsJsonArray("chars")) {
            JsonObject object = element.getAsJsonObject();

            MSDFGlyph glyph = new MSDFGlyph(
                object.get("id").getAsInt(),
                object.get("x").getAsInt(),
                object.get("y").getAsInt(),
                object.get("width").getAsInt(),
                object.get("height").getAsInt(),
                object.get("xoffset").getAsInt(),
                object.get("yoffset").getAsInt(),
                object.get("xadvance").getAsInt(),
                object.get("page").getAsInt()
            );

            glyphs.put(glyph.id(), glyph);
        }

        if (mainObject.has("kernings")) {
            for (JsonElement element : mainObject.getAsJsonArray("kernings")) {
                JsonObject object = element.getAsJsonObject();
                int first = object.get("first").getAsInt();
                int second = object.get("second").getAsInt();
                int amount = object.get("amount").getAsInt();
                kernings.put(first + "," + second, amount);
            }
        }
    }

    public void drawString(String text, float x, float y, Color color) {
        drawString(text,x,y - 4,0.25f,color);
    }

    public void drawString(String text, float x, float y, float scale, Color color) {
        RenderUtils.start2D();
        Shader shader = Client.INST.getShaderManager().getFont();

//        GL11.gl

        shader.start();

        shader.uniform4f("texColor", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        shader.uniform1f("pxRange", 0.2f);
        shader.uniform1i("MSDFTex", 0);
        mc.getTextureManager().bindTexture(atlasTexture);

        int prev = -1;

        float currentX = x;
        float currentY = y;

        float heighest = 0;

        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);
            MSDFGlyph glyph = glyphs.get(codepoint);

            if (glyph == null) {
                continue;
            }

            if (prev != -1) {
                currentX += kernings.getOrDefault(prev + "," + codepoint, 0);
            }

            if (codepoint == '\n') {
                currentX = x;
                currentY += heighest;
                prev = codepoint;
                continue;
            }

            float gw = glyph.width() * scale;
            float gh = glyph.height() * scale;

            if (gh > heighest) {
                heighest = gh;
            }

            float x0 = currentX + glyph.xOffset() * scale;
            float y0 = currentY + glyph.yOffset() * scale;
            float x1 = x0 + gw;
            float y1 = y0 + gh;

            float u0 = (float) glyph.x() / atlasWidth;
            float v0 = (float) glyph.y() / atlasHeight;
            float u1 = (float) (glyph.x() + glyph.width()) / atlasWidth;
            float v1 = (float) (glyph.y() + glyph.height()) / atlasHeight;

            ColorUtils.resetColor();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(u0, v0);
            GL11.glVertex2f(x0, y0);

            GL11.glTexCoord2f(u0, v1);
            GL11.glVertex2f(x0, y1);

            GL11.glTexCoord2f(u1, v1);
            GL11.glVertex2f(x1, y1);

            GL11.glTexCoord2f(u1, v0);
            GL11.glVertex2f(x1, y0);

            GL11.glEnd();

            currentX += glyph.xAdvance() * scale;
            prev = codepoint;
        }

        Shader.stop();

        RenderUtils.stop2D();

//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableCull();
//
//        ShaderProgram shader = RenderSystem.setShader(SHADER_KEY);
//
//        shader.getUniformOrDefault("pxRange").set(0.5f);
//
//        shader.addSamplerTexture("MSDFTex", atlasTexture.getGlId());
//        RenderSystem.setShaderTexture(0, atlasTexture.getGlId());
//
//        float cursorX = x;
//        float cursorY = y;
//
//        int prev = -1;
//
//        for (int i = 0; i < text.length(); i++) {
//            int codepoint = text.codePointAt(i);
//            MSDFGlyph glyph = glyphs.get(codepoint);
//            if (glyph == null) continue;
//
//            // кернинг
//            if (prev != -1) {
//                cursorX += kernings.getOrDefault(prev + "," + codepoint, 0);
//            }
//
//            float gw = glyph.width * scale;
//            float gh = glyph.height * scale;
//
//            float x0 = cursorX + glyph.xOffset * scale;
//            float y0 = cursorY + glyph.yOffset * scale;
//            float x1 = x0 + gw;
//            float y1 = y0 + gh;
//
//            float u0 = (float) glyph.x / atlasWidth;
//            float v0 = (float) glyph.y / atlasHeight;
//            float u1 = (float) (glyph.x + glyph.width) / atlasWidth;
//            float v1 = (float) (glyph.y + glyph.height) / atlasHeight;
//
//            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
//
//            builder.vertex(matrix, x0, y0, 0).texture(u0, v0).color(color.getLeftUp().getRGB());
//            builder.vertex(matrix, x0, y1, 0).texture(u0, v1).color(color.getLeftDown().getRGB());
//            builder.vertex(matrix, x1, y1, 0).texture(u1, v1).color(color.getRightDown().getRGB());
//            builder.vertex(matrix, x1, y0, 0).texture(u1, v0).color(color.getRightUp().getRGB());
//
//            BufferRenderer.drawWithGlobalProgram(builder.end());
//
//            cursorX += glyph.xAdvance * scale;
//            prev = codepoint;
//        }
//
//
//        RenderSystem.disableBlend();

//        float currentX = x;
//        float currentY = y;
//
//        int prev = -1;
//
//        ShaderProgram shader = RenderSystem.setShader(SHADER_KEY);
//
//        if (shader == null) {
//            return;
//        }
//
//        float heighest = 0;
//
//        shader.addSamplerTexture("MSDFTex", atlasId);
//        shader.getUniformOrDefault("pxRange").set(4f);
//
//
//        for (int i = 0; i < text.length(); i++) {
//            int codepoint = text.codePointAt(i);
//            MSDFGlyph glyph = glyphs.get(codepoint);
//
//            if (glyph == null) {
//                continue;
//            }
//
//            if (prev != -1) {
//                currentX += kernings.getOrDefault(prev + "," + codepoint, 0) * scale;
//            }
//
//            if (text.charAt(i) == '\n') {
//                currentX = x;
//                currentY += heighest;
//                heighest = 0;
//                prev = codepoint;
//                continue;
//            }
//
//            float gw = glyph.width * scale;
//            float gh = glyph.height * scale;
//
//            if (gh > heighest) {
//                heighest = gh;
//            }
//
//            float x0 = currentX + glyph.xOffset * scale;
//            float y0 = currentY + glyph.yOffset * scale;
//            float x1 = x0 + gw;
//            float y1 = y0 + gh;
//
//            float u0 = (float) glyph.x / atlasWidth;
//            float v0 = (float) glyph.y / atlasHeight;
//            float u1 = (float) (glyph.x + glyph.width) / atlasWidth;
//            float v1 = (float) (glyph.y + glyph.height) / atlasHeight;
//
//            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
//
//            builder.vertex(matrix, x0, y0, 0).texture(u0, v0).color(color.getLeftUp().getRGB());
//            builder.vertex(matrix, x0, y1, 0).texture(u0, v1).color(color.getLeftDown().getRGB());
//            builder.vertex(matrix, x1, y1, 0).texture(u1, v1).color(color.getRightDown().getRGB());
//            builder.vertex(matrix, x1, y0, 0).texture(u1, v0).color(color.getRightUp().getRGB());
//
//            BufferRenderer.drawWithGlobalProgram(builder.end());
//
//            currentX += glyph.xAdvance * scale;
//            prev = codepoint;
//        }
    }
}
