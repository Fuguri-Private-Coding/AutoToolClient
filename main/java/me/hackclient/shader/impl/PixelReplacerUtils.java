package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.CallableObject;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.impl.visual.ClientShader;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class PixelReplacerUtils implements CallableObject, InstanceAccess {

    {
        callables.add(this);
        timer = new StopWatch();
    }

    static StopWatch timer;
    static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    static ClientShader pixelReplacer;

    public static void addToDraw(Runnable runnable) {
        framebuffer.bindFramebuffer(true);
        runnable.run();
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            if (!Display.isVisible() || !Display.isActive()) {
                return;
            }
            if (pixelReplacer == null) {
                pixelReplacer = Client.INSTANCE.getModuleManager().getModule(ClientShader.class);
                return;
            }

            ScaledResolution sc = new ScaledResolution(mc);
            final Shader shader = Client.INSTANCE.getShaderManager().getPixelReplacer();

            if (shader == null) {
                return;
            }

            final int id = shader.getProgram();

            shader.start();
            Uniform.uniform1f(id, "r_offset", pixelReplacer.getColor().getRed() / 255f);
            Uniform.uniform1f(id, "g_offset", pixelReplacer.getColor().getGreen() / 255f);
            Uniform.uniform1f(id, "b_offset", pixelReplacer.getColor().getBlue() / 255f);
            Uniform.uniform1f(id, "time", timer.reachedMS() / (1000f * 10));
            Uniform.uniform1i(id, "texture", 19);
            Uniform.uniform1f(id, "texel_size", 1f / mc.displayHeight);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
            GL13.glActiveTexture(GL13.GL_TEXTURE19);
            framebuffer.bindFramebufferTexture();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            mc.getFramebuffer().bindFramebuffer(true);
            shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            shader.stop();

            framebuffer = updateFramebuffer(framebuffer);
            framebuffer.framebufferClear();
            mc.getFramebuffer().bindFramebuffer(true);
        }
    }

    static Framebuffer updateFramebuffer(Framebuffer toUpdate) {
        if (toUpdate.framebufferWidth != mc.displayWidth || toUpdate.framebufferHeight != mc.displayHeight) {
            toUpdate.deleteFramebuffer();
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return toUpdate;
    }
}
