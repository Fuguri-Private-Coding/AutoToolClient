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
import java.util.List;

public class BloomUtils implements InstanceAccess {

    private static Shader program;
    private static Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static GaussianKernel gaussianKernel = new GaussianKernel(0);

    private static Shadows shadows;

    public static void run(List<Runnable> runnable) {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (program == null) program = Client.INSTANCE.getShaderManager().getBloom();
        if (!Display.isVisible()) return;

        inputFramebuffer.bindFramebuffer(true);
        try {
            runnable.forEach(Runnable::run);
        } catch (Exception ignored) {}

        final int radius = shadows.radius.getValue();
        final float compression = shadows.compression.getValue();
        final int programId = program.getProgramId();

        outputFramebuffer.bindFramebuffer(true);
        program.start();

        if (gaussianKernel.getSize() != radius) {
            gaussianKernel = new GaussianKernel(radius);
            gaussianKernel.compute();

            final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
            buffer.put(gaussianKernel.getKernel());
            buffer.flip();

            Uniform.uniform1f(programId, "u_radius", radius);
            Uniform.uniformFB(programId, "u_kernel", buffer);
            Uniform.uniform1i(programId, "u_diffuse_sampler", 0);
            Uniform.uniform1i(programId, "u_other_sampler", 20);
        }

        Uniform.uniform2f(programId, "u_texel_size", 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
        Uniform.uniform2f(programId, "u_direction", compression, 0.0F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        inputFramebuffer.bindFramebufferTexture();
        Shader.drawQuad();

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Uniform.uniform2f(programId, "u_direction", 0.0F, compression);
        outputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
        inputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        Shader.drawQuad();
        GlStateManager.disableBlend();

        Shader.stop();
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
