package fuguriprivatecoding.autotoolrecode.utils.render.shader.impl;

import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class TextureUtils {
    private static final Shader shader = Shaders.texture;

    private static final TextureManager TEX_MANAGER = Minecraft.getMinecraft().getTextureManager();

    public static void texture(ResourceLocation textureLocation, float x, float y, float width, float height, float radius, float smoothness, Color color) {
        ITextureObject texture = toTexture(textureLocation);

        texture.setBlurMipmap(true, false);

        GlStateManager.bindTexture(texture.getGlTextureId());

        shader.start();
        shader.uniform("Sampler0", 0);
        shader.uniform("Size", width, height);
        shader.uniform("Radius", radius, radius, radius, radius);
        shader.uniform("Color", color);
        shader.uniform("Smoothness", smoothness);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        drawQuad(x, y, width, height);
        GlStateManager.disableBlend();

        GlStateManager.bindTexture(0);
        Shader.stop();
    }

    private static void drawQuad(final double x, final double y, final double width, final double height) {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2d(x, y + height);

        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2d(x + width, y + height);

        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex2d(x + width, y);

        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex2d(x, y);

        GL11.glEnd();
    }

    public static ITextureObject toTexture(ResourceLocation location) {
        ITextureObject texture = TEX_MANAGER.getTexture(location);
        if (texture == null) {
            texture = new SimpleTexture(location);
            TEX_MANAGER.loadTexture(location, texture);
        }

        return texture;
    }
}
