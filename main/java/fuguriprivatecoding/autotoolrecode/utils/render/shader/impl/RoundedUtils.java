package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import java.awt.*;

public class RoundedUtils implements Imports {

    private static Shader program;

    private static void draw(final float x, final float y, final float width, final float height, final float leftDown, final float leftUp, final float rightUp, final float rightDown, Color color) {
        if (program == null) program = Shaders.rounded;
        program.start();
        program.uniform("u_size", width, height);
        program.uniform("u_radius", leftUp, leftDown, rightUp, rightDown);
        program.uniform("u_smooth", 1f);
        program.uniform("u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        program.renderShader(x, y, width, height);
        GlStateManager.disableBlend();
        Shader.stop();
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        draw(x - 1f, y - 1f, width + 2, height + 2, radius, radius, radius, radius, color);
    }

    public static void drawCenteredXRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        drawRect(x - width / 2f, y, width, height, radius, color);
    }

    public static void drawCenteredYRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        drawRect(x, y - height / 2f, width, height, radius, color);
    }

    public static void drawCenteredXYRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        drawRect(x - width / 2f, y - height / 2f, width, height, radius, color);
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float leftDown, final float leftUp, final float rightUp, final float rightDown, final Color color) {
        draw(x - 1f, y - 1f, width + 2, height + 2, leftUp, leftDown, rightUp, rightDown, color);
    }
}
