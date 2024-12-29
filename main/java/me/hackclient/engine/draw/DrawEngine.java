package me.hackclient.engine.draw;

import me.hackclient.event.CallableObject;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DrawEngine implements CallableObject {

    private final CopyOnWriteArrayList<Runnable> renders;
    private final Tessellator tessellator;
    private final WorldRenderer worldRenderer;

    public DrawEngine() {
        renders = new CopyOnWriteArrayList<>();
        tessellator = Tessellator.getInstance();
        worldRenderer = tessellator.getWorldRenderer();
    }

    public void drawRect(double x, double y, double width, double height, Color color) {
        renders.add(() -> {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    color.getAlpha() / 255f
            );
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            worldRenderer.pos(x, y + height, 0).endVertex();
            worldRenderer.pos(x + width, y + height, 0).endVertex();
            worldRenderer.pos(x + width, y, 0).endVertex();
            worldRenderer.pos(x, y, 0).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        });
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent) {
            for (Runnable currentRender : renders) {
                currentRender.run();
            }
            renders.clear();
        }
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
