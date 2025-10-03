package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.GaussianKernel;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Uniform;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

public class OutlineUtils implements Imports {

    private static Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static GaussianKernel gaussianKernel = new GaussianKernel(0);


    public static void startWrite() {
        inputFramebuffer.bindFramebuffer(true);
    }

    public static void endWrite() {
        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void draw(int radius, Color color) {
        if (!Display.isActive() || !Display.isVisible()) return;

        Shader program = Client.INST.getShaderManager().getBloom();

        inputFramebuffer.bindFramebuffer(true);

        final int programId = program.getProgramId();

        outputFramebuffer.bindFramebuffer(true);
        program.start();

        if (gaussianKernel.getSize() != radius) {
            gaussianKernel = new GaussianKernel(radius);
            gaussianKernel.compute();

            Uniform.uniform1f(programId, "radius", radius);
            Uniform.uniform1i(programId, "texture", 8);
        }

        Uniform.uniform4f(programId, "color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        Uniform.uniform2f(programId, "texelSize", 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
        Uniform.uniform2f(programId, "direction", 2, 0);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        inputFramebuffer.bindFramebufferTexture();
        Shader.drawQuad();

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Uniform.uniform2f(programId, "direction", 0.0f, 2);
        outputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE8);
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
