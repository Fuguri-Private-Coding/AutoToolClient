package me.hackclient.utils.render;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Iterator;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.renderer.GlStateManager.resetColor;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

public class RenderUtils implements InstanceAccess {

    public static void start3D() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

        glDepthMask(false);
        GlStateManager.disableCull();
    }

    public static void stop3D() {
        GlStateManager.enableCull();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void renderHitBoxWithYaw(AxisAlignedBB box, float yaw) {
        final double yawR = Math.toRadians(yaw);

        final double sin = Math.sin(yawR);
        final double cos = Math.cos(yawR);

        renderHitBox(new AxisAlignedBB(
                        box.minX - sin,
                        box.minY + 0,
                        box.minZ + cos,
                        box.maxX - sin,
                        box.maxX + 0,
                        box.maxZ + cos
        ));
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

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        resetColor();
        mc.getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(
                x,
                y,
                0f,
                0f,
                width,
                height,
                width,
                height
        );
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawRoundedRect(final double x, final double y, double width, double height, double radius, final Color color) {
        if (width <= 0 || width <= radius) {
            width = radius;
        }
        if (height <= 0 || height <= radius) {
            height = radius;
        }

        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glDepthMask(false);

        glShadeModel(GL_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i <= 90; i++) {
            double sin = Math.sin(Math.toRadians(i)) * radius;
            double cos = Math.cos(Math.toRadians(i)) * radius;
            glVertex2d(x + width - radius + sin, y + radius - cos);
        }

        for (int i = 90; i <= 180; i++) {
            double sin = Math.sin(Math.toRadians(i)) * radius;
            double cos = Math.cos(Math.toRadians(i)) * radius;
            glVertex2d(x + width - radius + sin, y + height - radius - cos);

        }

        for (int i = 180; i <= 270; i++) {
            double sin = Math.sin(Math.toRadians(i)) * radius;
            double cos = Math.cos(Math.toRadians(i)) * radius;
            glVertex2d(x + radius + sin, y + height - radius - cos);
        }

        for (int i = 270; i <= 360; i++) {
            double sin = Math.sin(Math.toRadians(i)) * radius;
            double cos = Math.cos(Math.toRadians(i)) * radius;
            glVertex2d(x + radius + sin, y + radius - cos);
        }

        glEnd();

        glShadeModel(GL_FLAT);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        GlStateManager.resetColor();
    }
}
