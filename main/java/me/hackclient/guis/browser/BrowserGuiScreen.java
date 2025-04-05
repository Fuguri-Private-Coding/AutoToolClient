package me.hackclient.guis.browser;

import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.render.scissor.ScissorUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class BrowserGuiScreen extends GuiScreen {

    Vector2f pos, size, lastMouse;
    Vector2f lastSize = new Vector2f(800, 600);
    Vector2f lastPos = new Vector2f(200, 200);

    boolean moving, closing;
    final Animation2D background, sizeBackground;

    public BrowserGuiScreen() {
        pos = new Vector2f(200, 200);
        size = new Vector2f(800, 600);
        lastMouse = new Vector2f(0, 0);

        background = new Animation2D();
        sizeBackground = new Animation2D();

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 1) {
                closing = false;
                mc.displayGuiScreen(null);
                mc.currentScreen = null;
                return;
            }
        }

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;

        background.update(15f);
        sizeBackground.update(15f);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, 7f, new Color(15,15,15,150));
        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 7f, new Color(0,0,0,200));

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }

        ScissorUtils.disableScissor();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 && !closing) {
            ScaledResolution sc = new ScaledResolution(mc);
            lastPos.set(pos);
            lastSize.set(size);
            closing = true;
            size.set(0, 0);
            pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
            return;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sc = new ScaledResolution(mc);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean move = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y && mouseY < background.y + 15;

        if (mouseButton == 0) {
            if (quit) {
                lastPos.set(pos);
                lastSize.set(size);
                size.set(0, 0);
                closing = true;
                pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
            }

            if (fullscreen) {
                size.set(sc.getScaledWidth() - 10, sc.getScaledHeight() - 10);
                pos.set(5f, 5f);
            }

            if (collapse) {
                size.set(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
                pos.set(50f, 50f);
            }

            if (move) {
                if (quit || fullscreen || collapse) return;
                moving = true;
                lastMouse.set(mouseX, mouseY);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        moving = false;
    }

    @Override
    public void initGui() {
        sizeBackground.reset();
        background.reset();
        pos.set(lastPos);
        size.set(lastSize);
    }

    @Override
    public void onGuiClosed() {
    }
}