package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Uniform;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class StencilUtils implements Imports {

    private static Shader program;

    private static Framebuffer maskBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer imageBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    public static void startWriteMask() {
        maskBuffer.bindFramebuffer(true);
    }

    public static void endWriteMask() {
        GL13.glActiveTexture(GL13.GL_TEXTURE14);
        maskBuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void startWriteImage() {
        imageBuffer.bindFramebuffer(true);
    }


    public static void endWriteImage() {
        GL13.glActiveTexture(GL13.GL_TEXTURE15);
        imageBuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void draw(int test) {
        if (program == null) program = Client.INST.getShaderManager().getStencil();
        if (!Display.isVisible()) return;

        final int programID = program.getProgramId();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();

        mc.getFramebuffer().bindFramebuffer(true);
        program.start();
        Uniform.uniform1i(programID, "Image", 15);
        Uniform.uniform1i(programID, "Mask", 14);
        Uniform.uniform1i(programID, "Test", test);
        Shader.drawQuad();
        Shader.stop();

        update();
    }

    public static void update() {
        if (mc.displayWidth != maskBuffer.framebufferWidth || mc.displayHeight != maskBuffer.framebufferHeight) {
            maskBuffer.deleteFramebuffer();
            maskBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            maskBuffer.framebufferClear();
            maskBuffer.setFramebufferColor(0, 0, 0, 0);
        }

        if (mc.displayWidth != imageBuffer.framebufferWidth || mc.displayHeight != imageBuffer.framebufferHeight) {
            imageBuffer.deleteFramebuffer();
            imageBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            imageBuffer.framebufferClear();
            imageBuffer.setFramebufferColor(0, 0, 0, 0);
        }
    }
}
