package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RectUtils {

    private static Shader program;

    private static void draw(final float x, final float y, final float width, final float height, final float factor, Color color) {
        if (program == null) program = Shaders.testelips;
        float factorPenis = (1 / factor) * height;

        program.start();
        program.uniform("Factor", factorPenis);
        program.uniform("Color", color);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        program.renderShader(x, y, width, height);
        GlStateManager.disableBlend();
        Shader.stop();
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float factor, final Color color) {
        draw(x, y, width, height, factor, color);
    }

}
