package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.rect.Rect;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RectUtils {

    private static final Shader program = Shaders.roundedRectTest;

    private static void draw(final float x, final float y, final float width, final float height, final float factor, Color color) {
        if (color.getAlpha() == 0)
            return;

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

    public static void drawRect(final Rect rect, final float factor, final Color color) {
        draw(rect.x(), rect.y(), rect.width(), rect.height(), factor, color);
    }

}
