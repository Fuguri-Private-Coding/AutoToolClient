package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.shader.GaussianKernel;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;

public class BloomUtils implements InstanceAccess {

    private static Shader program;
    private static Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static GaussianKernel gaussianKernel = new GaussianKernel(0);

    public static Shadows shadows;

    public static void addToDraw(Runnable run) {
        inputFramebuffer.bindFramebuffer(true);
        run.run();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void draw() {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (program == null) program = Client.INSTANCE.getShaderManager().getBloom();
        if (!Display.isActive() || !Display.isVisible() || !shadows.isToggled()) return;

        inputFramebuffer.bindFramebuffer(true);

        final int radius = shadows.radius.getValue();
        final int programId = program.getProgramId();

        outputFramebuffer.bindFramebuffer(true);
        program.start();

        if (gaussianKernel.getSize() != radius) {
            gaussianKernel = new GaussianKernel(radius);
            gaussianKernel.compute();

            final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
            buffer.put(gaussianKernel.getKernel());
            buffer.flip();

            Uniform.uniform1f(programId, "radius", radius);
            Uniform.uniformFB(programId, "kernel", buffer);
            Uniform.uniform1i(programId, "image", 0);
            Uniform.uniform1i(programId, "image2", 20);
        }

        Uniform.uniform4f(programId, "color", shadows.color.getRed(), shadows.color.getGreen(), shadows.color.getBlue(), shadows.color.getAlpha());
        Uniform.uniform1f(programId,"brightness", shadows.brightness.getValue());
        Uniform.uniform2f(programId, "texel_size", 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
        Uniform.uniform2f(programId, "direction", shadows.horizontal1Compress.getValue(), shadows.vertical1Compress.getValue());

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        inputFramebuffer.bindFramebufferTexture();
        Shader.drawQuad();

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Uniform.uniform2f(programId, "direction", shadows.horizontal2Compress.getValue(), shadows.vertical2Compress.getValue());
        outputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
        inputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        Shader.drawQuad();
        GlStateManager.disableBlend();

        Shader.stop();

        update();
    }

    public static void update() {
        if (mc.displayWidth != inputFramebuffer.framebufferWidth || mc.displayHeight != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            outputFramebuffer.deleteFramebuffer();
            outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            inputFramebuffer.framebufferClear();
            outputFramebuffer.framebufferClear();
        }

        inputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        outputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
    }

}
