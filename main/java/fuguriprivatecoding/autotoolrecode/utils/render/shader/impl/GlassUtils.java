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

    private static void draw(final float x, final float y, final float width, final float height, final float factor, Color color) {
        if (program == null) program = Shaders.glass;

        program.start();
        program.uniform("Sampler0", 10);
        program.uniform("Size", width, height);
        program.uniform("Radius", factor, factor, factor, factor);
        program.uniform("Smoothness", 1f);
        program.uniform("CornerSmoothness", 2f);
        program.uniform("GlobalAlpha", 1f);
        program.uniform("FresnelPower", 1f);
        program.uniform("FresnelColor", 1f,1f,1f);
        program.uniform("FresnelAlpha", 0.3f);
        program.uniform("BaseAlpha", 1f);
        program.uniform("FresnelInvert", 0);
        program.uniform("FresnelMix", 10f);
        program.uniform("DistortStrength", 1f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        program.renderShader(x, y, width, height);
        GlStateManager.disableBlend();
        Shader.stop();
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float factor, final Color color) {
        draw(x, y, width, height, factor, color);
    }

}