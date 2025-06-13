package fuguriprivatecoding.autotoolrecode.guis.config;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.config.Config;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiText;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Util;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;

import static java.lang.Math.min;

public class ConfigGuiScreen extends GuiScreen {

    Vector2f pos, size, lastMouse, lastSize, lastPos;

    boolean moving, closing, creatingConfig;

    final Animation2D background, sizeBackground, scrolls;

    ClickGui clickGui;

    int delay = 10;

    Shadows shadows;

    int scroll, totalHeight;

    Config selectedConfig;

    private final AltManagerGuiText textField;

    public ConfigGuiScreen() {
        Client.INST.getEventManager().register(this);
        mc = Minecraft.getMinecraft();
        ScaledResolution sc = new ScaledResolution(mc);

        lastSize = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        lastPos = new Vector2f(50f, 50f);

        size = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        pos = new Vector2f(50f, 50f);

        lastMouse = new Vector2f(0, 0);
        textField = new AltManagerGuiText(1, mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);

        background = new Animation2D();
        sizeBackground = new Animation2D();
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (clickGui == null) clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

        boolean configScroll = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y + 15 && mouseY < background.y + sizeBackground.y;

        if (configScroll) scroll -= Mouse.getDWheel() / 120 * 10;

        float consoleVisibleHeight = sizeBackground.y - 55;
        float maxScroll = Math.max(totalHeight - consoleVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

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
        float widthCreate = fontRendererObj.getStringWidth("Create") / 2f;
        float widthLoad = fontRendererObj.getStringWidth("Load") / 2f;
        float widthDelete = fontRendererObj.getStringWidth("Delete") / 2f;
        float widthSave = fontRendererObj.getStringWidth("Save") / 2f;
        float widthFolder = fontRendererObj.getStringWidth("Folder") / 2f;
        float widthRefresh = fontRendererObj.getStringWidth("Refresh") / 2f;

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;
        scrolls.endY = scroll;

        background.update(15f);
        sizeBackground.update(15f);
        scrolls.update(15f);

        if (shadows.isToggled() && shadows.module.get("ConfigGui")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), Color.black));
        }

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clickGui.backgroundRadius.getValue(), new Color(0,0,0, clickGui.backgroundAlpha.getValue()));
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
        totalHeight = 0;
        for (Config config : Client.INST.getConfigManager().getConfigs()) {
            RoundedUtils.drawRect(background.x + 5 + offset, background.y + 20 + yOffset, 100, 30, clickGui.backgroundRadius.getValue(), selectedConfig != null ? selectedConfig == config ? new Color(50,50,50,150) : new Color(0,0,0,150) : new Color(0,0,0,150));
            fontRendererObj.drawString(config.getName(), background.x + 10 + offset, background.y + 30 + yOffset, -1);
            offset += 105;

            if (offset > background.x + sizeBackground.x - 200) {
                yOffset += 35;
                offset = 0;
                totalHeight += 35;
            }
        }

        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.WHITE);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.green);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.yellow);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.RED);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.blue);
        RoundedUtils.drawRect(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20 + 20 + 20, 50, 15, clickGui.backgroundRadius.getValue(), Color.gray);
        fontRendererObj.drawString("Create", background.x + sizeBackground.x - 55 + 25 - widthCreate, background.y + 20 + 3, -1, true);
        fontRendererObj.drawString("Load", background.x + sizeBackground.x - 55 + 25 - widthLoad, background.y + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Save", background.x + sizeBackground.x - 55 + 25 - widthSave, background.y + 20 + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Delete", background.x + sizeBackground.x - 55 + 25 - widthDelete, background.y + 20 + 20 + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Folder", background.x + sizeBackground.x - 55 + 25 - widthFolder, background.y + 20 + 20 + 20 + 20 + 20 + 3, -1, true);
        fontRendererObj.drawString("Refresh", background.x + sizeBackground.x - 55 + 25 - widthRefresh, background.y + 20 + 20 + 20 + 20 + 20 + 20 + 3, -1, true);

        ScissorUtils.disableScissor();

        if (creatingConfig) {
            RoundedUtils.drawRect(5,5,15,15,3f,Color.RED);
            textField.drawTextBox();
            textField.setMaxStringLength(16);
        }

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (creatingConfig) textField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == 1 && !closing && !creatingConfig) {
            ScaledResolution sc = new ScaledResolution(mc);
            lastPos.set(pos);
            lastSize.set(size);
            closing = true;
            size.set(0, 0);
            pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
        }

        if (keyCode == Keyboard.KEY_RETURN && creatingConfig && !textField.getText().isEmpty()) {
            Config config = new Config(textField.getText());
            Client.INST.getConfigManager().saveConfig(config);
            ClientUtils.chatLog("Successful created config: " + textField.getText() + ".");
            Client.INST.getConfigManager().refreshConfigs();
            textField.setText("");
            textField.setFocused(false);
            creatingConfig = false;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

        if (mouseX > 5 && mouseX < 20 && mouseY > 5 && mouseY < 20) {
            textField.setText("");
            textField.setFocused(false);
            creatingConfig = false;
        }

        if (creatingConfig) return;

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean move = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y && mouseY < background.y + 15;
        boolean create = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 && mouseY < background.y + 20 + 15;
        boolean load = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 && mouseY < background.y + 20 + 20 + 15;
        boolean save = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 15;
        boolean delete = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 20 + 15;
        boolean folder = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 20 + 20 + 15;
        boolean refresh = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 20 + 20 + 20 + 15;

        float offset = 0;
        float yOffset = scrolls.y;
        for (Config config : Client.INST.getConfigManager().getConfigs()) {
            boolean selectConfig = mouseX > background.x + 5 + offset && mouseX < background.x + 5 + offset + 100 && mouseY > background.y + 20 + yOffset && mouseY < background.y + 20 + yOffset + 30;
            if (load || save || delete || folder || refresh || create) break;
            if (mouseButton == 0 && selectConfig) selectedConfig = config;
            offset += 105;
            if (offset > background.x + sizeBackground.x - 200) {
                yOffset += 35;
                totalHeight += 35;
                offset = 0;
            }
        }

        if (mouseButton == 0) {
            if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y) return;

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
                String s = Client.INST.getConfigManager().getConfigsDirectory().getAbsolutePath();

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

            if (refresh) {
                Client.INST.getConfigManager().refreshConfigs();
                ClientUtils.chatLog("Successful refreshed.");
            }

            if (create) {
                creatingConfig = true;
                textField.setFocused(true);
            }

            if (selectedConfig != null) {
                if (load) {
                    Client.INST.getConfigManager().loadConfig(selectedConfig);
                    ClientUtils.chatLog("Successful loaded config: " + selectedConfig.getName() + ".");
                }

                if (delete) {
                    Client.INST.getConfigManager().deleteConfig(selectedConfig);
                    ClientUtils.chatLog("Successful deleted config: " + selectedConfig.getName() + ".");
                }

                if (save) {
                    Client.INST.getConfigManager().saveConfig(selectedConfig);
                    ClientUtils.chatLog("Successful saved config: " + selectedConfig.getName() + ".");
                }
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

    @EventTarget
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