package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

public class TextFadeUtils implements InstanceAccess {

    static Framebuffer framebuffer = new Framebuffer(0, 0, true);

    public static void draw(Runnable runnable, Color color, Color color1) {
        ScaledResolution sc = new ScaledResolution(mc);
        Shader shader = Client.INSTANCE.getShaderManager().getTextFade();
        final int id = shader.getProgramId();

        if (mc.displayWidth != framebuffer.framebufferWidth || mc.displayHeight != framebuffer.framebufferHeight) {
            framebuffer.deleteFramebuffer();
            framebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            framebuffer.framebufferClear();
        }

        framebuffer.bindFramebuffer(true);
        runnable.run();

        shader.start();

        Uniform.uniform1i(id, "texture", 19);
        Uniform.uniform1f(id, "texel_size", 1f / mc.displayHeight);
        Uniform.uniform4f(id, "startColor", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        Uniform.uniform4f(id, "endColor", color1.getRed() / 255.0F, color1.getGreen() / 255.0F, color1.getBlue() / 255.0F, color1.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL13.glActiveTexture(GL13.GL_TEXTURE19);
        framebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        mc.getFramebuffer().bindFramebuffer(true);
        shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
        GlStateManager.disableBlend();
        shader.stop();

    }
}
