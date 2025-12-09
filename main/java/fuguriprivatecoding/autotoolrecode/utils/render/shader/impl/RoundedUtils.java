package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Uniform;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RoundedUtils implements Imports {

    private static Shader program;

    private static void draw(final float x, final float y, final float width, final float height, final float leftDown, final float leftUp, final float rightUp, final float rightDown, Color color) {
        if (program == null) program = Shaders.rounded;
        int id = program.getProgramId();
        program.start();
        Uniform.uniform2f(id, "u_size", width, height);
        Uniform.uniform4f(id, "u_radius", leftUp, leftDown, rightUp, rightDown);
        Uniform.uniform1f(id, "u_smooth", 1f);
        Uniform.uniform4f(id, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
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

    public static void drawRect(final float x, final float y, final float width, final float height, final float leftDown, final float leftUp, final float rightUp, final float rightDown, final Color color) {
        draw(x - 1f, y - 1f, width + 2, height + 2, leftUp, leftDown, rightUp, rightDown, color);
    }

    public static void drawCenteredRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        draw(x - 1f - width / 2f, y - 1f, width + 2, height + 2, radius, radius, radius, radius, color);
    }

}
