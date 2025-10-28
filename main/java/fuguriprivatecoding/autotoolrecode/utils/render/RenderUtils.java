package fuguriprivatecoding.autotoolrecode.utils.render;

import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils implements Imports {

    public static void start3D() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
    }

    public static void stop3D() {
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }

    public static void drawRoundedOutLineRectangle(float x, float y, float width, float height, float radius, Color bgColor, Color outLineColor1, Color outLineColor2) {
        drawRoundedGradientOutlinedRectangle(x,y,x + width,y + height, radius, bgColor.getRGB(), outLineColor1.getRGB(), outLineColor2.getRGB());
    }

    public static void drawRoundedOutLineRectangle(float x, float y, float width, float height, float radius, int bgColor, int outLineColor1, int outLineColor2) {
        drawRoundedGradientOutlinedRectangle(x,y,x + width,y + height, radius, bgColor, outLineColor1, outLineColor2);
    }

    public static void drawRoundedGradientOutlinedRectangle(float n, float n2, float n3, float n4, final float n5, final int n6, final int n7, final int n8) { // credit to the creator of raven b4
        n *= 2.0F;
        n2 *= 2.0F;
        n3 *= 2.0F;
        n4 *= 2.0F;
        GL11.glPushAttrib(1);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glBegin(9);
        glColor(n6);
        for (int i = 0; i <= 90; i += 3) {
            final double n9 = i * 0.017453292f;
            GL11.glVertex2d((double) (n + n5) + Math.sin(n9) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n9) * n5 * -1.0);
        }
        for (int j = 90; j <= 180; j += 3) {
            final double n10 = j * 0.017453292f;
            GL11.glVertex2d((double) (n + n5) + Math.sin(n10) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n10) * n5 * -1.0);
        }
        for (int k = 0; k <= 90; k += 3) {
            final double n11 = k * 0.017453292f;
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n11) * n5, (double) (n4 - n5) + Math.cos(n11) * n5);
        }
        for (int l = 90; l <= 180; l += 3) {
            final double n12 = l * 0.017453292f;
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n12) * n5, (double) (n2 + n5) + Math.cos(n12) * n5);
        }
        GL11.glEnd();
        GL11.glPushMatrix();
        GL11.glShadeModel(7425);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(2);
        if (n7 != 0L) {
            glColor(n7);
        }
        for (int n13 = 0; n13 <= 90; n13 += 3) {
            final double n14 = n13 * 0.017453292f;
            GL11.glVertex2d((double) (n + n5) + Math.sin(n14) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n14) * n5 * -1.0);
        }
        for (int n15 = 90; n15 <= 180; n15 += 3) {
            final double n16 = n15 * 0.017453292f;
            GL11.glVertex2d((double) (n + n5) + Math.sin(n16) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n16) * n5 * -1.0);
        }
        if (n8 != 0) {
            glColor(n8);
        }
        for (int n17 = 0; n17 <= 90; n17 += 3) {
            final double n18 = n17 * 0.017453292f;
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n18) * n5, (double) (n4 - n5) + Math.cos(n18) * n5);
        }
        for (int n19 = 90; n19 <= 180; n19 += 3) {
            final double n20 = n19 * 0.017453292f;
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n20) * n5, (double) (n2 + n5) + Math.cos(n20) * n5);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL_BLEND);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void glColor(final int n) { // credit to the creator of raven b4
        GL11.glColor4f((float) (n >> 16 & 0xFF) / 255.0f, (float) (n >> 8 & 0xFF) / 255.0f, (float) (n & 0xFF) / 255.0f, (float) (n >> 24 & 0xFF) / 255.0f);
    }

    public static void quickDrawHead(ResourceLocation skin, float x, float y, float width, float height) {
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect(x, y, 8f, 8f, 8, 8, width, height, 64f, 64f);
        Gui.drawScaledCustomSizeModalRect(x, y, 40f, 8f, 8, 8, width, height, 64f, 64f);
    }

    public static void start3DNameTag() {
        glPushAttrib(GL_ENABLE_BIT);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void stop3DNameTag() {
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);

        glPopAttrib();

        ColorUtils.resetColor();
    }

    public static void renderWithAbsolutePosition(Runnable runnable) {
        final RenderManager renderManager = mc.getRenderManager();
        double x = renderManager.viewerPosX, y = renderManager.viewerPosY, z = renderManager.viewerPosZ;

        GlStateManager.translate(-x, -y, -z);
        runnable.run();
        GlStateManager.translate(x, y, z);
    }

    public static void drawBlockESP(BlockPos blockPos, Color color) {
        drawBlockESP(blockPos, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void drawBlockESP(BlockPos blockPos, float red, float green, float blue, float alpha) {
        glColor4f(red,green,blue,alpha);
        double x = blockPos.getX() - mc.getRenderManager().viewerPosX;
        double y = blockPos.getY() - mc.getRenderManager().viewerPosY;
        double z = blockPos.getZ() - mc.getRenderManager().viewerPosZ;
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        drawBoundingBox(new AxisAlignedBB(x + block.getBlockBoundsMinX(), y + block.getBlockBoundsMinY(), z + block.getBlockBoundsMinZ(), x + block.getBlockBoundsMaxX(), y + block.getBlockBoundsMaxY(), z + block.getBlockBoundsMaxZ()));
        ColorUtils.resetColor();
    }

    public static void drawDot(Vec3 pos, double size, Color color) {
        GlStateManager.pushMatrix();
        AxisAlignedBB box = new AxisAlignedBB(pos, pos).expand(size, size, size);

        glBlendFunc(770, 771);
        glEnable(3042);
        glDisable(GL_TEXTURE_2D);
        glDisable(2929);
        glDepthMask(false);
        glLineWidth(2.0F);
        renderWithAbsolutePosition(() -> drawBoundingBox(box, color));
        glEnable(GL_TEXTURE_2D);
        glEnable(2929);
        glDepthMask(true);
        glDisable(GL_BLEND);
        GlStateManager.popMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB abb, Color color) {
        drawBoundingBox(abb, color.getRed() / 255f,color.getGreen() / 255f,color.getBlue() / 255f,color.getAlpha() / 255f);
    }

    public static void drawHitBox(AxisAlignedBB bb, Color color, float lineWidth) {
        drawBoundingBox(bb.expand(0.1f,0.1f,0.1f), color);

        if (lineWidth > 0) {
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
            renderHitBox(bb.expand(0.1f,0.1f,0.1f), lineWidth);
            ColorUtils.resetColor();
        }
    }

    public static void drawBoundingBox(AxisAlignedBB abb, float r, float g, float b, float a) {
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer vb = ts.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        GlStateManager.resetColor();
    }

    public static void drawBoundingBox(AxisAlignedBB var0) {
        Tessellator var1 = Tessellator.getInstance();
        WorldRenderer var2 = var1.getWorldRenderer();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var1.draw();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var1.draw();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var1.draw();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var1.draw();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var1.draw();
        var2.begin(7, DefaultVertexFormats.POSITION);
        var2.pos(var0.minX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.maxZ).endVertex();
        var2.pos(var0.minX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.minX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.minZ).endVertex();
        var2.pos(var0.maxX, var0.maxY, var0.maxZ).endVertex();
        var2.pos(var0.maxX, var0.minY, var0.maxZ).endVertex();
        var1.draw();
    }

    public static void drawFilledBox(AxisAlignedBB bb, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        glColor4f(red, green, blue, alpha);

        drawFilledBoundingBox(bb);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    public static void drawOutlinedBox(AxisAlignedBB bb, float lineWidth, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;

        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glLineWidth(lineWidth);
        glEnable(GL_LINE_SMOOTH);

        glColor4f(red, green, blue, alpha);

        drawOutlinedBoundingBox(bb);

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    private static void drawFilledBoundingBox(AxisAlignedBB bb) {
        glBegin(GL_QUADS);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);

        glVertex3d(bb.minX, bb.maxY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);

        glEnd();
    }

    public static void glColor(Color color) {
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void glColor(Color color, float alpha) {
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
    }

    private static void drawOutlinedBoundingBox(AxisAlignedBB bb) {
        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);

        glEnd();

        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.minX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glEnd();

        glBegin(GL_LINES);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glEnd();
    }

    public static void renderHitBox(AxisAlignedBB bb, int type) {
        glBegin(type);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.minY, bb.minZ);

        glEnd();

        glBegin(type);

        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();

        glBegin(type);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(type);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(type);
        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(type);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glEnd();
    }

    public static void renderHitBox(AxisAlignedBB bb) {
        renderHitBox(bb, GL_LINE_LOOP);
    }

    public static void renderHitBox(AxisAlignedBB bb, float lineWidth) {
        glLineWidth(lineWidth);
        renderHitBox(bb, GL_LINE_LOOP);
        glLineWidth(1f);
    }
    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        drawImage(image, x,y,width,height,false);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, boolean resetColor) {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        if (!resetColor) ColorUtils.resetColor();
        mc.getTextureManager().bindTexture(image);
        Gui.drawScaledCustomSizeModalRect(x, y, 0f, 0f, width, height, width, height, width, height);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        if (resetColor) ColorUtils.resetColor();
    }

    public static void stop2D() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void start2D() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
    }

    public static void drawMixedRoundedRect(double x, double y, double width, double height, double radius, Color color1, Color color2, double offset1, double offset2, double offset3, double offset4, float speed) {
        drawMixedRoundedRect(x, y, x + width, y + height, color1, color2, radius, offset1, offset2, offset3, offset4, speed);
    }

    public static void drawMixedRoundedRect(double x, double y, double width, double height, double radius, Color color1, Color color2, float speed) {
        drawMixedRoundedRect(x, y, x + width, y + height, color1, color2, radius, 90, 180, 270, 360, speed);
    }

    public static void drawCenteredMixedRoundedRect(double x, double y, double width, double height, double radius, Color color1, Color color2, float speed) {
        drawMixedRoundedRect(x - width / 2f, y, x + width, y + height, color1, color2, radius, 90, 180, 270, 360, speed);
    }

    public static void drawMixedRoundedRect(double x, double y, double x1, double y1, Color color1, Color color2, double radius, double offset1, double offset2, double offset3, double offset4, float speed) {
        if (x1 < x) {
            double temp = x;
            x = x1;
            x1 = temp;
        }

        if (y1 < y) {
            double temp = y;
            y = y1;
            y1 = temp;
        }

        start2D();
        GL11.glShadeModel(7425);
        GL11.glBegin(6);
        double xs = x + radius;
        double ys = y + radius;

        ColorUtils.glColor(ColorUtils.mixColor(color1,color2, 1, offset1, speed));
        for (double i = 270.0F; i < 360.0F; i += 0.1F) {
            GL11.glVertex2d(xs + Math.sin(i * Math.PI / 180.0F) * radius, ys - Math.cos(i * Math.PI / 180.0F) * radius);
        }

        xs = x1 - radius;
        ys = y + radius;

        ColorUtils.glColor(ColorUtils.mixColor(color1,color2, 0, offset2, speed));
        for (double i = 0.0F; i < 90.0F; i += 0.1F) {
            GL11.glVertex2d(xs + Math.sin(i * Math.PI / 180.0F) * radius, ys - Math.cos(i * Math.PI / 180.0F) * radius);
        }

        xs = x1 - radius;
        ys = y1 - radius;

        ColorUtils.glColor(ColorUtils.mixColor(color1,color2, 1, offset3, speed));
        for (double i = 90.0F; i < 180.0F; i += 0.1F) {
            GL11.glVertex2d(xs + Math.sin(i * Math.PI / 180.0F) * radius, ys - Math.cos(i * Math.PI / 180.0F) * radius);
        }

        xs = x + radius;
        ys = y1 - radius;

        ColorUtils.glColor(ColorUtils.mixColor(color1,color2, 2, offset4, speed));
        for (double i = 180.0F; i < 270.0F; i += 0.1F) {
            GL11.glVertex2d(xs + Math.sin(i * Math.PI / 180.0F) * radius, ys - Math.cos(i * Math.PI / (double)180.0F) * radius);
        }

        GL11.glEnd();
        GL11.glShadeModel(7424);
        ColorUtils.resetColor();
        stop2D();
    }

    public static void drawMixedRect(double x, double y, double x1, double y1, int color1, int color2) {
        if (x1 < x) {
            double temp = x;
            x = x1;
            x1 = temp;
        }

        if (y1 < y) {
            double temp = y;
            y = y1;
            y1 = temp;
        }

        start2D();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians((double)(System.nanoTime() / 1000000L / 10L))) + (double)1.0F, 2.0F), 100);
        GL11.glVertex2d(x, y);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians((double)(System.nanoTime() / 1000000L / 10L + 90L))) + (double)1.0F, 2.0F), 100);
        GL11.glVertex2d(x1, y);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians((double)(System.nanoTime() / 1000000L / 10L + 180L))) + (double)1.0F, 2.0F), 100);
        GL11.glVertex2d(x1, y1);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians((double)(System.nanoTime() / 1000000L / 10L + 260L))) + (double)1.0F, 2.0F), 100);
        GL11.glVertex2d(x, y1);
        GL11.glEnd();
        stop2D();
    }
}
