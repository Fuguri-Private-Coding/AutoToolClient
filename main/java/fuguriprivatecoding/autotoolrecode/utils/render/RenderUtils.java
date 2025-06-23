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

    public static void quickDrawHead(ResourceLocation skin, int x, int y, int width, int height) {
        mc.getTextureManager().bindTexture(skin);
        Gui.drawScaledCustomSizeModalRect(x, y, 8f, 8f, 8, 8, width, height, 64f, 64f);
        Gui.drawScaledCustomSizeModalRect(x, y, 40f, 8f, 8, 8, width, height, 64f, 64f);
    }

    public static void start3DNameTag() {
        glPushAttrib(GL_ENABLE_BIT);
        glPushMatrix();
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void stop3DNameTag() {
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glPopMatrix();
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
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        ColorUtils.resetColor();
        mc.getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(
                x, y, 0f, 0f, width, height, width, height
        );
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }
}
