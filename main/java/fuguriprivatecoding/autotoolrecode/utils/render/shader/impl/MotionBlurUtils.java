package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.MotionBlur;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

@UtilityClass
public class MotionBlurUtils implements Imports {
    public static Framebuffer inputFramebuffer = mc.getFramebuffer();

    private static Framebuffer historyA = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer historyB = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    private static boolean ping = false;
    private static boolean firstFrame = true;

    public void draw() {
        update();

        MotionBlur mb = Modules.getModule(MotionBlur.class);
        Shader program = Shaders.mb;

        Framebuffer prev = ping ? historyA : historyB;
        Framebuffer curr = ping ? historyB : historyA;
        ping = !ping;

        if (firstFrame) {
            prev.bindFramebuffer(true);

            GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
            GlStateManager.disableDepth();

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            inputFramebuffer.bindFramebufferTexture();
            Shader.drawQuad();

            GlStateManager.enableDepth();
            prev.unbindFramebuffer();

            firstFrame = false;
        }

        curr.bindFramebuffer(true);

        GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
        GlStateManager.disableDepth();

        program.start();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        inputFramebuffer.bindFramebufferTexture();
        program.uniform("DiffuseSampler", 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        prev.bindFramebufferTexture();
        program.uniform("PrevSampler", 2);

        program.uniform(
            "Phosphor",
            1f - mb.blurAmount.getValue() / 100f,
            0, 0
        );

        Shader.drawQuad();

        GlStateManager.enableDepth();
        curr.unbindFramebuffer();

        mc.getFramebuffer().bindFramebuffer(true);
        GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        curr.bindFramebufferTexture();
        Shader.drawQuad();
        Shader.stop();
    }

    public static void update() {
        if (mc.displayWidth != historyA.framebufferWidth
            || mc.displayHeight != historyA.framebufferHeight) {

            historyA.deleteFramebuffer();
            historyB.deleteFramebuffer();

            historyA = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            historyB = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            historyA.framebufferClear();
            historyB.framebufferClear();

            ping = false;
            firstFrame = true;
        }
    }
}
