package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Uniform;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class BackgroundUtils implements Imports {

    private static Shader program;

    private static Framebuffer tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    public static void run() {
        if (program == null) program = Client.INST.getShaderManager().getBackground();
        if (!Display.isVisible() || !Display.isActive()) return;

        update();

        final int programID = program.getProgramId();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();

        mc.getFramebuffer().bindFramebuffer(true);
        program.start();
        Uniform.uniform2f(programID, "resolution", mc.displayWidth, mc.displayHeight);
        Uniform.uniform1f(programID, "time", (System.currentTimeMillis() - mc.getStartMillisTime()) / 1000F);
        Shader.drawQuad();
        Shader.stop();
    }

    public static void update() {
        if (mc.displayWidth != tempFBO.framebufferWidth || mc.displayHeight != tempFBO.framebufferHeight) {
            tempFBO.deleteFramebuffer();
            tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            tempFBO.framebufferClear();
        }
    }
}

