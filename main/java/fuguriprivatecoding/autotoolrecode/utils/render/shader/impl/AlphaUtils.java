package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
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
        GL13.glActiveTexture(GL13.GL_TEXTURE12);
        tempFBO.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void draw(float alpha) {
        if (program == null) program = Shaders.alpha;
        if (!Display.isVisible()) return;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();

        mc.getFramebuffer().bindFramebuffer(true);
        program.start();
        program.uniform("image", 12);
        program.uniform("alpha", alpha);
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

