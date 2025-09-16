package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Uniform;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class AlphaUtils implements Imports {

    private static Shader program;

    private static Framebuffer tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    public static void startWrite() {
        tempFBO.bindFramebuffer(true);

    }

    public static void endWrite() {
        GL13.glActiveTexture(GL13.GL_TEXTURE19);
        tempFBO.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void draw(float alpha) {
        if (program == null) program = Client.INST.getShaderManager().getAlpha();
        if (!Display.isVisible()) return;

        final int programID = program.getProgramId();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();

        mc.getFramebuffer().bindFramebuffer(true);
        program.start();
        Uniform.uniform1i(programID, "image", 19);
        Uniform.uniform1f(programID, "alpha", alpha);
        Shader.drawQuad();
        Shader.stop();

        update();
    }

    public static void update() {
        if (mc.displayWidth != tempFBO.framebufferWidth || mc.displayHeight != tempFBO.framebufferHeight) {
            tempFBO.deleteFramebuffer();
            tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            tempFBO.framebufferClear();
            tempFBO.setFramebufferColor(0,0,0,0);
        }
    }
}

