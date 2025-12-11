package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class BackgroundUtils implements Imports {

    private static Shader program;

    public static void run() {
        if (program == null) program = Shaders.background;
        if (!Display.isVisible() || !Display.isActive()) return;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();

        mc.getFramebuffer().bindFramebuffer(true);
        program.start();
        program.uniform("resolution", (float) mc.displayWidth, (float)  mc.displayHeight);
        program.uniform("time", (System.currentTimeMillis() - mc.getStartMillisTime()) / 1000F);
        Shader.drawQuad();
        Shader.stop();
    }
}

