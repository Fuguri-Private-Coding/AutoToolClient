package me.hackclient.guis.console;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.callable.ConditionCallableObject;
import me.hackclient.event.events.TickEvent;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.animation.Animation2D;
import me.hackclient.utils.render.scissor.ScissorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class ConsoleGuiScreen extends GuiScreen implements ConditionCallableObject {

    Vector2f pos, size, lastMouse;

    Vector2f lastSize = new Vector2f(200, 200);
    Vector2f lastPos = new Vector2f(200, 200);

    boolean moving, closing;

    int scroll = 0;

    boolean fullScreen = false;

    final Animation2D background, sizeBackground;

    public ConsoleGuiScreen() {
        callables.add(this);
        mc = Minecraft.getMinecraft();
        lastMouse = new Vector2f(0, 0);

        pos = new Vector2f(0, 0);
        size = new Vector2f(350, 150);

        background = new Animation2D();
        sizeBackground = new Animation2D();
    }

    private int totalHeight = 0;

    boolean openBrowser = false;

    int delay = 30;

    private final GuiTextField textField = new GuiTextField(0, null, 0,0,0,0);

    private final List<String> history = new ArrayList<>();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scroll -= Mouse.getDWheel() / 120 * 10;

        if (scroll > 0) {
            scroll = 0;
        }

        float consoleVisibleHeight = sizeBackground.y - (fullScreen ? 20 : 2) - 15;
        float maxScroll = Math.max(0, totalHeight - consoleVisibleHeight);

        if (scroll < -maxScroll) {
            scroll = (int) -maxScroll;
        }

        if (history.isEmpty()) {
            scroll = 0;
        }

        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 1) {
                closing = false;
                mc.displayGuiScreen(null);
                mc.currentScreen = null;
//                if (openBrowser) {
//                    mc.displayGuiScreen(new BrowserGuiScreen());
//                }
            }
        }

        textField.setFocused(true);

        String username = System.getProperty("user.name");

        String name = switch (delay) {
            case 0, 1, 2 -> "Console_";
            case 3, 4 -> "Console";
            case 5, 6 -> "Consol";
            case 7, 8 -> "Conso";
            case 9, 10 -> "Cons";
            case 11, 12, 13 -> "Con";
            case 14, 15, 16 -> "Co";
            case 17, 18, 19 -> "C";
            case 20 -> "_";
            default -> "§k" + "Console_".substring(0, min(delay - 20, 8));
        };

        float widthName = mc.fontRendererObj.getStringWidth(name);

        float width = mc.fontRendererObj.getStringWidth("Clear History");

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;

        background.update(15f);
        sizeBackground.update(15f);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, 7f, new Color(15,15,15,150));
        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 7f, new Color(0,0,0,200));
        RoundedUtils.drawRect(background.x, background.y + sizeBackground.y + (fullScreen ? -10 - 3 - 4.5f : 2f), sizeBackground.x, 18, 7f, new Color(0,0,0,200));

//        ScissorUtils.enableScissor();
//
//        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15, sizeBackground.x, sizeBackground.y - (fullScreen ? 35 : 18));
        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15, sizeBackground.x, sizeBackground.y - (fullScreen ? 35 : 18));

        float offset = scroll;
        totalHeight = 0;

        for (String s : history.reversed()) {
            mc.fontRendererObj.drawString(s, background.x + 4, background.y + sizeBackground.y - 12 - offset + (fullScreen ? -10 - 5 - 1 : 0), -1);
            totalHeight += 10;
            offset += 10;
        }

        ScissorUtils.disableScissor();

        mc.fontRendererObj.drawString(name, background.x + sizeBackground.x / 2f - widthName / 2 - 5, background.y + 4,  -1);
        mc.fontRendererObj.drawString("C:\\Users\\" + username + ">" + textField.getText() + (delay > 15 ? "" : "_"), background.x + 2 + 2, background.y + sizeBackground.y + (fullScreen ? -10 - 3 : 7), -1);
        mc.fontRendererObj.drawString("Clear History", background.x + sizeBackground.x - 5 - width, background.y + 3.5f, -1);

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);
        RoundedUtils.drawRect(background.x + 34.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, Color.black);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);
        RoundedUtils.drawRect(background.x + 35, background.y + 4, 6.5f, 6.5f, 3f, Color.blue);

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }
    }

    public void log(String msg) {
        history.add("C:\\System32:§3 " + msg);
        System.out.println("[Console]: " + msg);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        String username = System.getProperty("user.name");
        if (keyCode == 1 && !closing) {
            ScaledResolution sc = new ScaledResolution(mc);
            lastPos.set(pos);
            lastSize.set(size);
            closing = true;
            size.set(0, 0);
            pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN && !textField.getText().isEmpty()) {
            history.add("C:\\Users\\" + username + ": "  + textField.getText());
            Client.INSTANCE.getCommandManager().handle(textField.getText());
            textField.setText("");
        }

        textField.textboxKeyTyped(typedChar, keyCode);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

        float width = mc.fontRendererObj.getStringWidth("Clear History");

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean browser = mouseX > background.x + 35 && mouseX < background.x + 35 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean clearHistory = mouseX > background.x + sizeBackground.x - 5 - width && mouseX < background.x + sizeBackground.x - 5 + width && mouseY > background.y && mouseY < background.y + 3.5 + 9;
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
                fullScreen = true;
                size.set(sc.getScaledWidth() - 10, sc.getScaledHeight() - 10);
                pos.set(5f, 5f);
            }

            if (collapse) {
                fullScreen = false;
                size.set(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
                pos.set(50f, 50f);
            }
//            if (browser) {
//                lastPos.set(pos);
//                lastSize.set(size);
//                size.set(0, 0);
//                closing = true;
//                openBrowser = true;
//                pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
//            }

            if (clearHistory) {
                history.clear();
                scroll = 0;
            }

            if (move) {
                if (quit || fullscreen || collapse || browser) return;
                moving = true;
                lastMouse.set(mouseX, mouseY);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (delay > 0) {
                delay--;
                return;
            }
            if (delay == 0) {
                delay = 30;
            }
        }
    }

    @Override
    public void initGui() {
        sizeBackground.reset();
        background.reset();
        pos.set(lastPos);
        size.set(lastSize);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        moving = false;
    }

    @Override
    public boolean handleEvents() {
        return mc.thePlayer != null && mc.theWorld != null && mc.currentScreen instanceof ConsoleGuiScreen;
    }
}
