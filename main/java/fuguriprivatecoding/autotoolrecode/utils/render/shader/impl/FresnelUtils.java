package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FresnelUtils implements Imports {
    private static final Shader shader = Shaders.fresnelTexture;
    private static final TextureManager TEX_MANAGER = Minecraft.getMinecraft().getTextureManager();

    public static void drawScreen(float x, float y, float width, float height,
                                   float radius, float cornerSmoothness,
                                   float fresnelPower, Color fresnelColor, float fresnelAlpha,
                                   float baseAlpha, boolean fresnelInvert, float fresnelMix,
                                   float distortStrength, Color tint) {
        ScaledResolution sr = new ScaledResolution(mc);
        Framebuffer screenFBO = mc.getFramebuffer();

        float scaleX = (float) screenFBO.framebufferTextureWidth / sr.getScaledWidth();
        float scaleY = (float) screenFBO.framebufferTextureHeight / sr.getScaledHeight();

        float fx = x * scaleX;
        float fy = y * scaleY;
        float fwidth = width * scaleX;
        float fheight = height * scaleY;
        fy = screenFBO.framebufferTextureHeight - fy - fheight;

        float u0 = fx / screenFBO.framebufferTextureWidth;
        float v0 = fy / screenFBO.framebufferTextureHeight;
        float u1 = (fx + fwidth) / screenFBO.framebufferTextureWidth;
        float v1 = (fy + fheight) / screenFBO.framebufferTextureHeight;

        GlStateManager.bindTexture(screenFBO.framebufferTexture);

        setupUniforms(x, y, width, height, radius, cornerSmoothness, fresnelPower, fresnelColor,
                fresnelAlpha, baseAlpha, fresnelInvert, fresnelMix, distortStrength, tint);

        drawQuadWithUv(x, y, width, height, u0, v1, u0, v0, u1, v0, u1, v1);

        Shader.stop();
        GlStateManager.bindTexture(0);
    }

    public static void drawTexture(ResourceLocation textureLocation, float x, float y, float width, float height,
                                    float radius, float cornerSmoothness,
                                    float fresnelPower, Color fresnelColor, float fresnelAlpha,
                                    float baseAlpha, boolean fresnelInvert, float fresnelMix,
                                    float distortStrength, Color tint) {
        ITextureObject texture = toTexture(textureLocation);
        texture.setBlurMipmap(true, false);
        GlStateManager.bindTexture(texture.getGlTextureId());

        setupUniforms(x, y, width, height, radius, cornerSmoothness, fresnelPower, fresnelColor,
                fresnelAlpha, baseAlpha, fresnelInvert, fresnelMix, distortStrength, tint);

        drawQuadWithUv(x, y, width, height, 0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f);

        Shader.stop();
        GlStateManager.bindTexture(0);
    }

    private static void setupUniforms(float x, float y, float width, float height,
                                      float radius, float cornerSmoothness,
                                      float fresnelPower, Color fresnelColor, float fresnelAlpha,
                                      float baseAlpha, boolean fresnelInvert, float fresnelMix,
                                      float distortStrength, Color tint) {
        shader.start();
        shader.uniform("RectPos", x, y);
        shader.uniform("RectSize", width, height);
        shader.uniform("Size", width, height);
        shader.uniform("Radius", radius, radius, radius, radius);
        shader.uniform("Smoothness", 1.0f);
        shader.uniform("CornerSmoothness", cornerSmoothness);
        shader.uniform("GlobalAlpha", tint.getAlpha() / 255.0F);
        shader.uniform("FresnelPower", fresnelPower);
        shader.uniform("FresnelColor", fresnelColor.getRed() / 255.0F, fresnelColor.getGreen() / 255.0F, fresnelColor.getBlue() / 255.0F);
        shader.uniform("FresnelAlpha", fresnelAlpha);
        shader.uniform("BaseAlpha", baseAlpha);
        shader.uniform("FresnelInvert", fresnelInvert ? 1 : 0);
        shader.uniform("FresnelMix", fresnelMix);
        shader.uniform("DistortStrength", distortStrength);
        shader.uniform("Sampler0", 0);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
    }

    private static void drawQuadWithUv(float x, float y, float width, float height,
                                        float uTLx, float uTLy, float uBLx, float uBLy,
                                        float uBRx, float uBRy, float uTRx, float uTRy) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(uTLx, uTLy);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(uBLx, uBLy);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(uBRx, uBRy);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(uTRx, uTRy);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
    }

    public static ITextureObject toTexture(ResourceLocation location) {
        ITextureObject texture = TEX_MANAGER.getTexture(location);
        if (texture == null) {
            texture = new SimpleTexture(location);
            TEX_MANAGER.loadTexture(location, texture);
        }

        return texture;
    }
}
