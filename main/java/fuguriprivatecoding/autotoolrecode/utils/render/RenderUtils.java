package fuguriprivatecoding.autotoolrecode.utils.render;

import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderUtils implements Imports {

    public void start3D() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
    }

    public void stop3D() {
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }

    public Vec3 getAbsoluteSmoothPos(Vec3 lastPos, Vec3 currentPos) {
        return getAbsoluteSmoothPos(lastPos, currentPos, mc.timer.renderPartialTicks);
    }

    public Vec3 getAbsoluteSmoothPos(Vec3 lastPos, Vec3 currentPos, float partialTicks) {
        return lastPos.add(currentPos.subtract(lastPos).multiple(partialTicks));
    }

    public void renderBed(final BlockPos[] blockPos, Color color) {
        double posX = blockPos[0].getX() - mc.getRenderManager().viewerPosX;
        double posY = blockPos[0].getY() - mc.getRenderManager().viewerPosY;
        double posZ = blockPos[0].getZ() - mc.getRenderManager().viewerPosZ;
        GL11.glDepthMask(false);
        AxisAlignedBB axisAlignedBB;

        final float HEIGHT = 0.5625F;

        if (blockPos[0].getX() != blockPos[1].getX()) {
            boolean isFirstBlockRight = blockPos[0].getX() > blockPos[1].getX();
            double minX = isFirstBlockRight ? posX - 1.0 : posX;
            double maxX = isFirstBlockRight ? posX + 1.0 : posX + 2.0;

            axisAlignedBB = new AxisAlignedBB(minX, posY, posZ, maxX, posY + HEIGHT, posZ + 1.0);
        } else {
            boolean isFirstBlockBack = blockPos[0].getZ() > blockPos[1].getZ();
            double minZ = isFirstBlockBack ? posZ - 1.0 : posZ;
            double maxZ = isFirstBlockBack ? posZ + 1.0 : posZ + 2.0;

            axisAlignedBB = new AxisAlignedBB(posX, posY, minZ, posX + 1.0, posY + HEIGHT, maxZ);
        }
        RenderUtils.drawBoundingBox(axisAlignedBB, color);
        GlStateManager.resetColor();
    }

    public void drawRect(float x, float y, float width, float height, Color color) {
        Gui.drawRect(x, y, x + width, y + height, color.getRGB());
    }

    public void drawBlockESP(BlockPos blockPos, Color color) {
        drawBlockESP(blockPos, color, 1);
    }

    public void drawBlockESP(BlockPos blockPos, Color color, float expand) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        float expandValue = 1 - expand;

        AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, blockPos)
            .offset(RenderManager.getRenderPosition().invert()).expand(expandValue, expandValue, expandValue);

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);

        drawBoundingBox(bb, color);
    }

    public void drawDot(Vec3 pos, double size, Color color) {
        GlStateManager.pushMatrix();

        AxisAlignedBB box = new AxisAlignedBB(pos, pos)
            .offset(RenderManager.getRenderPosition().invert())
            .expand(size, size, size);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glDepthMask(false);
        glLineWidth(2.0F);

        drawBoundingBox(box, color);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glDepthMask(true);
        glDisable(GL_BLEND);

        GlStateManager.popMatrix();
    }

    public void drawHitBox(AxisAlignedBB bb, Color color, float lineWidth) {
        bb = bb.expand(0.1f);

        drawBoundingBox(bb, color);

        if (lineWidth >= 1) {
            ColorUtils.glColor(color, 1f);
            renderHitBox(bb, lineWidth);
            ColorUtils.resetColor();
        }
    }

    public void startHitBoxBegin(int lineWidth) {
        glLineWidth(lineWidth);
        glBegin(GL_LINES);
    }

    public void endHitBoxBegin() {
        glEnd();
        glLineWidth(1f);
    }

    public void renderHitBoxBatch(AxisAlignedBB bb) {
        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);

        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.minY, bb.minZ);

        glVertex3d(bb.minX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    }

    public void drawBoundingBox(AxisAlignedBB abb, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

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

    public void renderPlayer(Entity target, Vec3 pos, float rotationYawHead, float partialTicks, Color color) {
        RendererLivingEntity.setShaderBrightness(color);
        renderPlayer(target, pos, rotationYawHead, partialTicks);
        RendererLivingEntity.unsetShaderBrightness();
    }

    public void renderPlayer(Entity target, Vec3 pos, float rotationYawHead, float partialTicks) {
        startPlayer();
        drawPlayerModel(target, pos, rotationYawHead, partialTicks);
        stopPlayer();
    }

    public void startPlayer() {
        mc.entityRenderer.enableLightmap();
        RenderHelper.enableStandardItemLighting();
    }

    public void drawPlayerModel(Entity target, Vec3 pos, float rotationYawHead, float partialTicks) {
        mc.getRenderManager().doRenderEntity(target, pos.xCoord, pos.yCoord, pos.zCoord, rotationYawHead, partialTicks, true);
    }

    public void stopPlayer() {
        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderHitBox(AxisAlignedBB bb, float lineWidth) {
        glLineWidth(lineWidth);
        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.minY, bb.minZ);

        glEnd();

        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();

        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(GL_LINE_LOOP);

        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(GL_LINE_LOOP);
        glVertex3d(bb.minX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.minY, bb.minZ);
        glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        glVertex3d(bb.minX, bb.maxY, bb.minZ);

        glEnd();
        glBegin(GL_LINE_LOOP);
        glVertex3d(bb.minX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        glEnd();
        glLineWidth(1f);
    }

    public void start2D() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
    }

    public void stop2D() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public void drawMixedRoundedRect(double x, double y, double width, double height, double radius, Color color1, Color color2, float speed) {
        drawMixedRoundedRect(x, y, x + width, y + height, color1, color2, radius, speed);
    }

    public void drawMixedRect(double x, double y, double width, double height, Color color1, Color color2, float speed) {
        drawMixedRect(x, y, x + width, y + height, speed, color1, color2);
    }

    public void drawMixedRoundedRect(double x, double y, double x1, double y1, Color color1, Color color2, double radius, float speed) {
        start2D();

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        double time = ((double) System.nanoTime() / 1_000_000L / 10.0D) * speed;

        double centerX = x + radius;
        double centerY = y + radius;

        ColorUtils.glColor(
            ColorUtils.mix(
                color1, color2,
                Math.sin(Math.toRadians(time)) + 1.0F,
                2.0F
            )
        );

        for (double angle = 270.0D; angle < 360.0D; angle += 10.0D) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(centerX + Math.sin(radians) * radius, centerY - Math.cos(radians) * radius);
        }

        centerX = x1 - radius;
        centerY = y + radius;

        ColorUtils.glColor(
            ColorUtils.mix(
                color1, color2,
                Math.sin(Math.toRadians(time + 90.0D)) + 1.0F,
                2.0F
            )
        );

        for (double angle = 0.0D; angle < 90.0D; angle += 10.0D) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(centerX + Math.sin(radians) * radius, centerY - Math.cos(radians) * radius);
        }

        centerX = x1 - radius;
        centerY = y1 - radius;

        ColorUtils.glColor(
            ColorUtils.mix(
                color1, color2,
                Math.sin(Math.toRadians(time + 180.0D)) + 1.0F,
                2.0F
            )
        );

        for (double angle = 90.0D; angle < 180.0D; angle += 10.0D) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(centerX + Math.sin(radians) * radius, centerY - Math.cos(radians) * radius);
        }

        centerX = x + radius;
        centerY = y1 - radius;

        ColorUtils.glColor(
            ColorUtils.mix(
                color1, color2,
                Math.sin(Math.toRadians(time + 270.0D)) + 1.0F,
                2.0F
            )
        );

        for (double angle = 180.0D; angle < 270.0D; angle += 10.0D) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(centerX + Math.sin(radians) * radius, centerY - Math.cos(radians) * radius);
        }

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        ColorUtils.resetColor();
        stop2D();
    }

    public void drawMixedRect(double x, double y, double x1, double y1, float speed, Color color1, Color color2) {
        double time = (double) (System.nanoTime() / 1000000L / 10L) * speed;

        start2D();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians(time)) + (double)1.0F, 2.0F));
        GL11.glVertex2d(x, y);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians(time + 90L)) + (double)1.0F, 2.0F));
        GL11.glVertex2d(x1, y);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians(time + 180L)) + (double)1.0F, 2.0F));
        GL11.glVertex2d(x1, y1);
        ColorUtils.glColor(ColorUtils.mix(color1, color2, Math.sin(Math.toRadians(time + 260L)) + (double)1.0F, 2.0F));
        GL11.glVertex2d(x, y1);
        GL11.glEnd();
        stop2D();
    }
}
