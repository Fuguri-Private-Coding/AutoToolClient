package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.Color;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

public final class GlassUtils {

    private static Shader program;
    private static Framebuffer sceneCopy;
    private static boolean batchActive;

    private GlassUtils() {
    }

    /*
     * Вызывай один раз перед серией стеклянных drawRect() в этом кадре.
     * Если стеклянный элемент один, можно вообще не вызывать:
     * drawRect() сам сделает свежую копию сцены.
     */
    public static void beginBatch() {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return;
        }

        if (program == null) {
            program = Shaders.glass;
        }

        ensureSceneCopy();
        copyMainFramebuffer();
        batchActive = true;
    }

    public static void endBatch() {
        batchActive = false;
    }

    /*
     * Алиасы, если тебе удобнее старое название.
     */
    public static void beginFrame() {
        beginBatch();
    }

    public static void endFrame() {
        endBatch();
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return;
        }

        if (program == null) {
            program = Shaders.glass;
        }

        ensureSceneCopy();

        /*
         * Если batch не открыт, берем свежую копию сцены прямо сейчас.
         * Это гарантирует, что при одном элементе в кадре фон всегда живой.
         */
        if (!batchActive) {
            copyMainFramebuffer();
        }

        final ScaledResolution sr = new ScaledResolution(mc);
        final float scale = sr.getScaleFactor();

        /*
         * gl_FragCoord работает в framebuffer pixels,
         * поэтому размер элемента и радиус тоже передаем в framebuffer pixels.
         */
        final float fbWidth = width * scale;
        final float fbHeight = height * scale;
        final float fbRadius = radius * scale;

        program.start();

        GL13.glActiveTexture(GL13.GL_TEXTURE10);
        GlStateManager.bindTexture(sceneCopy.framebufferTexture);

        /*
         * В sampler2D передается texture unit, а не texture id.
         */
        program.uniform("Sampler0", 10);
        program.uniform("ScreenSize",
            (float) sceneCopy.framebufferTextureWidth,
            (float) sceneCopy.framebufferTextureHeight);
        program.uniform("Size", fbWidth, fbHeight);

        program.uniform("Radius", fbRadius, fbRadius, fbRadius, fbRadius);
        program.uniform("Smoothness", Math.max(1.0f, scale));
        program.uniform("CornerSmoothness", 4.0f);
        program.uniform("GlobalAlpha", color.getAlpha() / 255.0f);

        /*
         * Нейтральное стекло с легким tint от color.
         * Если хочешь совсем бесцветное стекло, передавай белый color.
         */
        program.uniform("FresnelPower", 6.0f);
        program.uniform("FresnelColor",
            color.getRed() / 255.0f,
            color.getGreen() / 255.0f,
            color.getBlue() / 255.0f);
        program.uniform("FresnelAlpha", 1.0f);
        program.uniform("BaseAlpha", 1.0f);
        program.uniform("FresnelInvert", 1);
        program.uniform("FresnelMix", 1f);
        program.uniform("DistortStrength", 1.25f * scale);

        /*
         * Для FBO Minecraft 1.8.9 чаще всего нужен flip по Y.
         * Если картинка окажется перевернутой, поменяй на 0.0f.
         */
        program.uniform("FlipY", 0f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        program.renderShader(x, y, width, height);

        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
        Shader.stop();
    }

    public static void invalidate() {
        batchActive = false;

        if (sceneCopy != null) {
            sceneCopy.deleteFramebuffer();
            sceneCopy = null;
        }
    }

    private static void ensureSceneCopy() {
        if (sceneCopy != null
            && sceneCopy.framebufferWidth == mc.displayWidth
            && sceneCopy.framebufferHeight == mc.displayHeight) {
            return;
        }

        if (sceneCopy != null) {
            sceneCopy.deleteFramebuffer();
        }

        sceneCopy = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        sceneCopy.setFramebufferFilter(GL11.GL_LINEAR);
        sceneCopy.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private static void copyMainFramebuffer() {
        final Framebuffer main = mc.getFramebuffer();

        sceneCopy.framebufferClear();
        sceneCopy.bindFramebuffer(true);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Рисуем содержимое main framebuffer в sceneCopy.
         * После этого стеклянный шейдер читает sceneCopy, а не текущий target.
         */
        main.framebufferRenderExt(sceneCopy.framebufferWidth, sceneCopy.framebufferHeight, true);

        main.bindFramebuffer(true);
        GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
    }
}