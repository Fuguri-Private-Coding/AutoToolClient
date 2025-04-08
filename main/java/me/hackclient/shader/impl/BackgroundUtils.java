package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class BackgroundUtils implements InstanceAccess {

    private static Shader program;

    private static Framebuffer tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    public static void run() {
        if (program == null) program = Client.INSTANCE.getShaderManager().getBackground();
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

