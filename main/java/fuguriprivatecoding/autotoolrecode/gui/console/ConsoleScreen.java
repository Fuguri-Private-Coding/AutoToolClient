package fuguriprivatecoding.autotoolrecode.gui.console;

import fuguriprivatecoding.autotoolrecode.command.Commands;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.lang.Math.min;

public class ConsoleScreen extends GuiScreen implements EventListener {

    boolean moving, closing;
    public boolean changed = false;
    boolean fullScreen = false;
    int scroll, totalHeight = 0;
    int delay = 30;

    public static ConsoleScreen INST;

    public static void init() {
        INST = new ConsoleScreen();
    }

    Vector2f pos, size, lastMouse, lastSize, lastPos;

    Color mainColor;
    final Animation2D background, sizeBackground, scrolls;

    private final GuiTextField textField = new GuiTextField(0, null, 0, 0, 0, 0);
    public static final List<String> history = new CopyOnWriteArrayList<>();

    ClickGui clickGui;
    ClientSettings clientSettings;

    private ConsoleScreen() {
        Events.register(this);
        mc = Minecraft.getMinecraft();

        ScaledResolution sc = new ScaledResolution(mc);
        init(sc);

        scrolls = new Animation2D();
        background = new Animation2D();
        sizeBackground = new Animation2D();
    }

    private void init(ScaledResolution resolution) {
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();

        lastSize = new Vector2f(screenWidth - 100, screenHeight - 100);
        lastPos = new Vector2f(50f, 50f);
        lastMouse = new Vector2f(0, 0);

        size = new Vector2f(screenWidth - 100, screenHeight - 100);
        pos = new Vector2f(50f, 50f);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (clickGui == null) clickGui = Modules.getModule(ClickGui.class);
        if (clientSettings == null) clientSettings = Modules.getModule(ClientSettings.class);

        float scale = clientSettings.scale.getValue();

        ScaledResolution sc = ScaleUtils.getScaledResolution(scale);

        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale);

        GL11.glScaled(scale, scale, 1f);

        mainColor = clickGui.color.getFadedColor();

        boolean consoleScroll = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y + (fullScreen ? 20 : 2) + 15 && mouseY < background.y + sizeBackground.y + (fullScreen ? 20 : 2);

        if (consoleScroll) scroll -= ClientSettings.getScroll();

        float consoleVisibleHeight = sizeBackground.y - 35;
        float maxScroll = Math.max(0, totalHeight - consoleVisibleHeight);

        if (scroll < -maxScroll) scroll = (int) -maxScroll;
        if (scroll > 0) scroll = 0;
        if (history.isEmpty()) scroll = 0;

        if (closing) {
            closing = false;
            mc.displayGuiScreen(null);
            mc.currentScreen = null;
        }

        textField.setFocused(true);
        textField.setMaxStringLength(100);

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

        ClientFont font = Fonts.fonts.get(clickGui.fonts.getMode());

        double widthName = font.getStringWidth(name);
        double width = font.getStringWidth("Clear History");

        scrolls.endY = scroll;
        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;

        scrolls.update(15f);
        background.update(15f);
        sizeBackground.update(15f);

        if (clickGui.glow.isToggled()) {
            BloomUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
            });
        }

        if (clickGui.blur.isToggled()) {
            BlurUtils.addToDraw(() -> {
                RenderUtils.drawRoundedOutLineRectangle(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), Color.BLACK.getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());
            });
        }

        RenderUtils.drawRoundedOutLineRectangle(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue() * 1.7f, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, background.x, background.y - 1, sizeBackground.x + 2, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 0,clientSettings.backgroundRadius.getValue() / 1.25f,clientSettings.backgroundRadius.getValue() / 1.25f,0, Color.BLACK);

        RoundedUtils.drawRect(background.x, background.y + sizeBackground.y - 18, sizeBackground.x, 18, clientSettings.backgroundRadius.getValue() / 1.25f, 0, 0, clientSettings.backgroundRadius.getValue() / 1.25f, new Color(0,0,0,255));

        font.drawString(name, background.x + sizeBackground.x / 2f - widthName / 2 - 5, background.y + 3.5f + 2, Color.WHITE);
        font.drawString(textField.getText() + (delay > 15 ? "" : "_"), background.x + 2 + 2, background.y + sizeBackground.y - 12.5f + 1, Color.WHITE);
        font.drawString("Clear History", background.x + sizeBackground.x - 5 - width, background.y + 3.5f + 2, Color.WHITE);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, quit ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, fullscreen ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, collapse ? Color.WHITE : Color.BLACK);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);
        ScissorUtils.disableScissor();

        float offset = scrolls.y;
        totalHeight = 0;

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, background.x, background.y + 15, sizeBackground.x, sizeBackground.y - 35);

        for (String s : history.reversed()) {
            font.drawString(s, background.x + 4, background.y + sizeBackground.y - 28 + 2 - offset, Color.WHITE, false);
            totalHeight += 10;
            offset += 10;
        }

        ScissorUtils.disableScissor();

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            background.translatePos(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }
        GL11.glScaled(1f / scale, 1f / scale, 1f);
    }

    public static void log(String msg) {
        history.add(ClientUtils.prefixLog + msg);
        ClientUtils.chatLog(msg);
        System.out.println("[Console]: " + msg);
    }

    public static void logWithoutPrefix(String msg) {
        history.add(msg);
        ClientUtils.chatLogWithoutPrefix(msg);
        System.out.println("[Console]: " + msg);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        String username = System.getProperty("user.name");

        if (keyCode == 1 && !closing) {
            closing = true;
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN && !textField.getText().isEmpty()) {
            if (changed) return;
            if (!Commands.handle(textField.getText())) {
                history.add(username + ": " + textField.getText());
            }
            textField.setText("");
        }

        textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float scale = clientSettings.scale.getValue();

        ScaledResolution sc = ScaleUtils.getScaledResolution(scale);

        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale);

        ClientFont font = Fonts.fonts.get(clickGui.fonts.getMode());

        double width = font.getStringWidth("Clear History");

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean clearHistory = mouseX > background.x + sizeBackground.x - 5 - width && mouseX < background.x + sizeBackground.x - 5 + width && mouseY > background.y && mouseY < background.y + 3.5 + 9;
        boolean move = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y && mouseY < background.y + 15;

        if (mouseButton == 0) {
            if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y) return;

            if (quit) {
                closing = true;
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

            if (clearHistory) {
                history.clear();
                scroll = 0;
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
    public boolean listen() {
        return mc.currentScreen == this;
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
}