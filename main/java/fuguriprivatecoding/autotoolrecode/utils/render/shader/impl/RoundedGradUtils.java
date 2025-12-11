package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@UtilityClass
public class RoundedGradUtils implements Imports {

    public void draw(float x, float y, float width, float height, float radius, Color firstColor, Color secondColor, boolean vertical) {
        Shader program = Shaders.roundedgrad;
        program.start();
        program.uniform("u_size", width, height);
        program.uniform("u_radius", radius);
        program.uniform("u_first_color", firstColor.getRed() / 255.0F, firstColor.getGreen() / 255.0F, firstColor.getBlue() / 255.0F, firstColor.getAlpha() / 255.0F);
        program.uniform("u_second_color", secondColor.getRed() / 255.0F, secondColor.getGreen() / 255.0F, secondColor.getBlue() / 255.0F, secondColor.getAlpha() / 255.0F);
        program.uniform("u_direction", vertical ? 1 : 0);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        Shader.drawQuad(x, y, width, height);
        Shader.stop();
    }

    public void drawRect(double x, double y, double width, double height, double radius, Color firstColor, Color secondColor, boolean vertical) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, firstColor, secondColor, vertical);
    }
}
