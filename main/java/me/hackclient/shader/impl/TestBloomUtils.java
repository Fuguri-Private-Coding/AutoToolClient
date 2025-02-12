package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.impl.visual.Bloom;
import me.hackclient.shader.Shader;
import me.hackclient.shader.Uniform;
import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class TestBloomUtils implements InstanceAccess, ConditionCallableObject {

    { callables.add(this); }

    static Framebuffer input = new Framebuffer(1, 1, true);
    static Framebuffer out = new Framebuffer(1, 1, true);

    public static void add(Runnable runnable) {
        input.bindFramebuffer(true);
        runnable.run();
        input.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            Shader shader = Client.INSTANCE.getShaderManager().getBloom();

            if (shader == null) {
                System.out.println("BLOOM ERROR -> NOT FOUND BLOOM SHADER");
                return;
            }

            final int id = shader.getProgramId();

            shader.start();
            ScaledResolution sc = new ScaledResolution(mc);

            Uniform.uniform1f(id, "radius", 10f);
            Uniform.uniform1i(id, "texture", 15);
            Uniform.uniform4f(id, "main_color", 1.0f, 0.0f, 0.0f, 1.0f);
            Uniform.uniform2f(id, "texel_size", 1f / mc.displayWidth, 1f / mc.displayHeight);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

            GL13.glActiveTexture(GL13.GL_TEXTURE15);
            input.bindFramebufferTexture();

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            Uniform.uniform2f(id, "direction", 1f, 0f);
            out.bindFramebufferTexture();
            shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
            Uniform.uniform2f(id, "direction", 0f, 1f);
            mc.getFramebuffer().bindFramebuffer(true);
            shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
            GlStateManager.disableBlend();
            shader.stop();

            input.framebufferClear();
            out.framebufferClear();
            input = updateFramebuffer(input);
            out = updateFramebuffer(out);
            mc.getFramebuffer().bindFramebuffer(true);

            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    @Override
    public boolean handleEvents() {
      //Bloom bloom = mm.getModule("Bloom");
      return Display.isActive() && Display.isVisible();
    }

    static Framebuffer updateFramebuffer(Framebuffer toUpdate) {
        if (toUpdate.framebufferWidth != mc.displayWidth || toUpdate.framebufferHeight != mc.displayHeight) {
            toUpdate.deleteFramebuffer();
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return toUpdate;
    }
}
