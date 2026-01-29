package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.GaussianKernel;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import java.nio.FloatBuffer;

public class BloomUtils implements Imports {

    private static Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private static GaussianKernel gaussianKernel = new GaussianKernel(0);

    public static Glow shadows;

    public static void addToDraw(Runnable run) {
        if (shadows != null && !shadows.isToggled()) return;
        inputFramebuffer.bindFramebuffer(true);
        run.run();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static void draw() {
        if (shadows == null) shadows = Modules.getModule(Glow.class);
        if (!Display.isActive() || !Display.isVisible() || !shadows.isToggled()) return;
        Shader program = Shaders.bloom;

        inputFramebuffer.bindFramebuffer(true);

        final int radius = shadows.radius.getValue();

        outputFramebuffer.bindFramebuffer(true);
        program.start();

        if (gaussianKernel.getSize() != radius) {
            gaussianKernel = new GaussianKernel(radius);
            gaussianKernel.compute();

            final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
            buffer.put(gaussianKernel.getKernel());
            buffer.flip();

            program.uniform("radius", (float) radius);
            program.uniform("kernel", buffer);
            program.uniform("image", 0);
            program.uniform("image2", 20);
        }

        program.uniform("brightness", shadows.brightness.getValue());
        program.uniform("texel_size", 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
        program.uniform("direction", shadows.offset1.getValue(), 0);
        program.uniform("time", System.currentTimeMillis());

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        inputFramebuffer.bindFramebufferTexture();
        Shader.drawQuad();

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        program.uniform("direction", 0.0f, shadows.offset2.getValue());
        outputFramebuffer.bindFramebufferTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE20);
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
