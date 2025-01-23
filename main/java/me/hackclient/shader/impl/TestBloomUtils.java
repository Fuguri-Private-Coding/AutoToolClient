package me.hackclient.shader.impl;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.shader.Shader;
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

    public static void add(Runnable runnable) {
        input.bindFramebuffer(true);
        runnable.run();
        input.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof Render2DEvent))
            return;

        Shader shader = Client.INSTANCE.getShaderManager().getBloom();

        if (shader == null) {
            System.out.println("BLOOM ERROR -> NOT FOUND BLOOM SHADER");
            return;
        }

        shader.start();
        ScaledResolution sc = new ScaledResolution(mc);

        shader.uniform1f("radius", 10);
        shader.uniform1i("texture", 15);
        shader.uniform4f("main_color", 1.0f, 0.0f, 0.0f, 1.0f);
        shader.uniform2f("texel_size", 1f / mc.displayWidth, 1f / mc.displayHeight);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

        GL13.glActiveTexture(GL13.GL_TEXTURE15);
        input.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        mc.getFramebuffer().bindFramebuffer(true);
        shader.renderShader(0, 0, sc.getScaledWidth(), sc.getScaledHeight());
        GlStateManager.disableBlend();
        shader.stop();


        input.framebufferClear();
        input = updateFramebuffer(input);
        mc.getFramebuffer().bindFramebuffer(true);
    }

    @Override
    public boolean handleEvents() {
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
