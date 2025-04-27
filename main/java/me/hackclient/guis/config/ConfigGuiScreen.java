package me.hackclient.guis.config;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.impl.visual.ClickGui;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.render.scissor.ScissorUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Util;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.min;

public class ConfigGuiScreen extends GuiScreen implements ConditionCallableObject {

    Vector2f pos, size, lastMouse;

    Vector2f lastSize = new Vector2f(200, 200);
    Vector2f lastPos = new Vector2f(200, 200);

    boolean moving, closing;
    final Animation2D background, sizeBackground, scrolls;
    ClickGui clickGui;
    int delay = 10;
    Shadows shadows;
    int scroll, totalHeight;
    File selectedConfig;

    public ConfigGuiScreen() {
        callables.add(this);
        pos = new Vector2f(0, 0);
        size = new Vector2f(200, 200);
        lastMouse = new Vector2f(0, 0);

        background = new Animation2D();
        sizeBackground = new Animation2D();
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (clickGui == null) clickGui = Client.INSTANCE.getModuleManager().getModule(ClickGui.class);
        scroll -= Mouse.getDWheel() / 120 * 10;

        if (scroll > 0) scroll = 0;

        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 2) {
                closing = false;
                mc.displayGuiScreen(null);
                mc.currentScreen = null;
            }
        }

        String name = switch (delay) {
            case 0, 1, 2 -> "Config_";
            case 3, 4 -> "Config";
            case 5, 6 -> "Confi";
            case 7, 8 -> "Conf";
            case 9, 10 -> "Con";
            case 11, 12, 13 -> "Co";
            case 14, 15, 16 -> "C";
            case 17, 18, 19 -> "c";
            case 20 -> "_";
            default -> "§k" + "Config".substring(0, min(delay - 20, 6));
        };

        float widthName = mc.fontRendererObj.getStringWidth(name);
        float widthLoad = fontRendererObj.getStringWidth("Load") / 2f;
        float widthDelete = fontRendererObj.getStringWidth("Delete") / 2f;
        float widthSave = fontRendererObj.getStringWidth("Save") / 2f;
        float widthFolder = fontRendererObj.getStringWidth("Folder") / 2f;

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;
        scrolls.endY = scroll;

        background.update(15f);
        sizeBackground.update(15f);
        scrolls.update(15f);

        if (shadows.isToggled() && shadows.config.isToggled()) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), Color.black));
        }

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), new Color(15,15,15,150));
        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, clickGui.backgroundRadius.getValue(), new Color(0,0,0,200));

        fontRendererObj.drawString(name, background.x + sizeBackground.x / 2f - widthName / 2 - 5, background.y + 4,-1);

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15f, sizeBackground.x, sizeBackground.y - 15);

        float offset = 0;
        float yOffset = scrolls.y;
        for (File file : Client.INSTANCE.getConfigsDirectory().listFiles()) {
            RoundedUtils.drawRect(background.x + 5 + offset, background.y + 20 + yOffset, 100, 30, clickGui.backgroundRadius.getValue(), selectedConfig != null ? selectedConfig.getName().equals(file.getName()) ? new Color(50,50,50,150) : new Color(0,0,0,150) : new Color(0,0,0,150));
            fontRendererObj.drawString(file.getName().replaceAll(".json", ""), background.x + 10 + offset, background.y + 30 + yOffset, -1);
            offset += 105;

            if (offset > background.x + sizeBackground.x - 200) {
                yOffset += 35;
                totalHeight += 35;
                offset = 0;
            }
        }

        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.green);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.yellow);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.RED);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.blue);
        fontRendererObj.drawString("Load", background.x + sizeBackground.x - 55 + 25 - widthLoad, background.y + 20 + 3, -1, true);
        fontRendererObj.drawString("Save", background.x + sizeBackground.x - 55 + 25 - widthSave, background.y + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Delete", background.x + sizeBackground.x - 55 + 25 - widthDelete, background.y + 20 + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Folder", background.x + sizeBackground.x - 55 + 25 - widthFolder, background.y + 20 + 20 + 20 + 20 + 3, -1, true);

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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean move = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y && mouseY < background.y + 15;
        boolean load = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 && mouseY < background.y + 20 + 15;
        boolean save = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 && mouseY < background.y + 20 + 20 + 15;
        boolean delete = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 15;
        boolean folder = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 20 + 15;

        float offset = 0;
        float yOffset = scrolls.y;
        for (File file : Client.INSTANCE.getConfigsDirectory().listFiles()) {
            boolean selectConfig = mouseX > background.x + 5 + offset && mouseX < background.x + 5 + offset + 100 && mouseY > background.y + 20 + yOffset && mouseY < background.y + 20 + yOffset + 30;
            if (mouseButton == 0 && selectConfig) selectedConfig = file;
            offset += 105;
            if (offset > background.x + sizeBackground.x - 200) {
                yOffset += 35;
                totalHeight += 35;
                offset = 0;
            }
        }

        if (mouseButton == 0) {
            if (quit) {
                lastPos.set(pos);
                lastSize.set(size);
                closing = true;
                size.set(0, 0);
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

            if (folder) {
                String s = Client.INSTANCE.getConfigsDirectory().getAbsolutePath();

                if (Util.getOSType() == Util.EnumOS.OSX) {
                    try {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", s});
                    } catch (IOException ignored) {}
                } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
                    String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", s);

                    try {
                        Runtime.getRuntime().exec(s1);
                    } catch (IOException ignored) {}
                }
            }

            if (selectedConfig != null) {
                if (load) Client.INSTANCE.getConfigManager().loadAsync(selectedConfig);
                if (delete) Client.INSTANCE.getConfigManager().delete(selectedConfig);
                if (save) Client.INSTANCE.getConfigManager().save(selectedConfig);
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
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (delay > 0) {
                delay--;
                return;
            }
            if (delay == 0) delay = 30;
        }
    }

    @Override
    public boolean handleEvents() {
        return mc.theWorld != null & mc.thePlayer != null && mc.currentScreen instanceof ConfigGuiScreen;
    }
}