package me.hackclient.utils.render.shader.impl;

import me.hackclient.Client;
import me.hackclient.utils.render.shader.Shader;
import me.hackclient.utils.render.shader.Uniform;
import me.hackclient.utils.interfaces.Imports;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RoundedUtils implements Imports {

    private static Shader program;

    private static void draw(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        if (program == null) program = Client.INST.getShaderManager().getRounded();
        int id = program.getProgramId();
        program.start();
        Uniform.uniform2f(id, "u_size", width, height);
        Uniform.uniform1f(id, "u_radius", radius);
        Uniform.uniform4f(id, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        program.renderShader(x, y, width, height);
        GlStateManager.disableBlend();
        Shader.stop();
    }

    public static void drawRect(final float x, final float y, final float width, final float height, final float radius, final Color color) {
        draw(x, y, width, height, radius, color);
    }

}
