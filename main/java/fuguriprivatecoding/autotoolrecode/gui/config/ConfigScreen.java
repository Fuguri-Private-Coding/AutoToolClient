package fuguriprivatecoding.autotoolrecode.gui.config;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.config.Config;
import fuguriprivatecoding.autotoolrecode.config.Configs;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.buttons.TextButton;
import fuguriprivatecoding.autotoolrecode.irc.ClientIRC;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Util;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.Math.min;

public class ConfigScreen extends GuiScreen implements EventListener {

    ClickGui clickGui = Modules.getModule(ClickGui.class);
    ClientSettings clientSettings = Modules.getModule(ClientSettings.class);
    public final TextButton textField;

    Vector2f pos, size, lastMouse, lastSize, lastPos;
    boolean moving, closing, creatingConfig;
    int delay = 10;
    int scroll, totalHeight;
    Config selectedConfig;

    final Animation2D background, sizeBackground, scrolls;

    Color mainColor;

    public static ConfigScreen INST;

    public static void init() {
        INST = new ConfigScreen();
    }

    private ConfigScreen() {
        Events.register(this);
        mc = Minecraft.getMinecraft();
        ScaledResolution sc = new ScaledResolution(mc);

        lastSize = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        lastPos = new Vector2f(50f, 50f);
        size = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        pos = new Vector2f(50f, 50f);
        lastMouse = new Vector2f(0, 0);

        textField = new TextButton(1, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);

        background = new Animation2D();
        sizeBackground = new Animation2D();
        scrolls = new Animation2D();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mainColor = clickGui.color.getFadedColor();

        float scale = clientSettings.scale.getValue();

        ScaledResolution sc = ScaleUtils.getScaledResolution(scale);

        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale);

        GL11.glScaled(scale,scale,1f);

        boolean configScroll = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y + 15 && mouseY < background.y + sizeBackground.y;

        if (configScroll) scroll -= ClientSettings.getScroll();

        float consoleVisibleHeight = sizeBackground.y - 55;
        float maxScroll = Math.max(totalHeight - consoleVisibleHeight, 0);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        if (closing) {
            closing = false;
            mc.displayGuiScreen(null);
            mc.currentScreen = null;
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

        ClientFontRenderer font = Fonts.fonts.get(clickGui.fonts.getMode());

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;
        scrolls.endY = scroll;

        background.update(15f);
        sizeBackground.update(15f);
        scrolls.update(15f);

        if (clickGui.glow.isToggled()) {
            BloomUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
            });
        }

        if (clickGui.blur.isToggled()) {
            BlurUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), clickGui.colorShadow.getColor(), clickGui.colorShadow.getFadeColor(), clickGui.colorShadow.getSpeed());
            });
        }

        RenderUtils.drawRoundedOutLineRectangle(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue() * 1.7f, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, background.x, background.y - 1, sizeBackground.x + 2, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 0,clientSettings.backgroundRadius.getValue() / 1.25f,clientSettings.backgroundRadius.getValue() / 1.25f,0, Color.BLACK);

        font.drawCenteredString(name, background.x + sizeBackground.x / 2f - 5, background.y + 3.5f + 2, Color.white);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, quit ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, fullscreen ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, collapse ? Color.WHITE : Color.BLACK);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

        final float buttonX = background.x + sizeBackground.x - 55;
        final float startY = background.y + 20;
        final float buttonWidth = 50;
        final float buttonHeight = 15;
        final float buttonSpacing = 20;
        final float borderRadius = clientSettings.backgroundRadius.getValue() * 1.7f;
        final int borderColor = Color.BLACK.getRGB();
        final Color textColor = Color.WHITE;
        final float textOffset = 3 + 2;
        final float centerX = buttonX + buttonWidth / 2f;

        final String[] buttonLabels = {"Create", "Load", "Save", "Delete", "Folder", "Refresh", "Online"};

        for (int i = 0; i < buttonLabels.length; i++) {
            float buttonY = startY + i * buttonSpacing;
            RenderUtils.drawRoundedOutLineRectangle(buttonX, buttonY, buttonWidth, buttonHeight, borderRadius, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(), borderColor, borderColor);
            font.drawCenteredString(buttonLabels[i], centerX, buttonY + textOffset, textColor);
        }

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc, background.x, background.y + 15f, sizeBackground.x, sizeBackground.y - 15);

        float offset = 0;
        float yOffset = scrolls.y;
        totalHeight = 0;
        for (Config config : Configs.getConfigs()) {
            Color selectedColor = selectedConfig != null ? selectedConfig == config ? new Color(50,50,50,150) : new Color(0,0,0,150) : new Color(0,0,0,150);
            RoundedUtils.drawRect(background.x + 5 + offset, background.y + 20 + yOffset, 100, 30, clientSettings.backgroundRadius.getValue(), selectedColor);
            font.drawString(config.getName(), background.x + 10 + offset, background.y + 30 + 2 + yOffset, Color.WHITE);
            offset += 105;

            if (offset > background.x + sizeBackground.x - 200) {
                yOffset += 35;
                offset = 0;
                totalHeight += 35;
            }
        }

        ScissorUtils.disableScissor();

        if (creatingConfig) {
            textField.drawTextBox();
            textField.setMaxStringLength(16);
        }

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            background.translatePos(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }
        GL11.glScaled(1f / scale, 1f / scale,1f);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (creatingConfig) textField.textboxKeyTyped(typedChar, keyCode);

        float scale = clientSettings.scale.getValue();

        if (keyCode == 1 && creatingConfig) {
            textField.setText("");
            textField.setFocused(false);
            creatingConfig = false;
            return;
        }

        if (keyCode == 1 && !closing) {
            closing = true;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            if (creatingConfig && !textField.getText().isEmpty()) {
                Config config = new Config(textField.getText());
                Configs.saveConfig(config);
                ClientUtils.chatLog("Successful created config: " + textField.getText() + ".");
                Configs.refreshConfigs();
                textField.setText("");
                textField.setFocused(false);
                creatingConfig = false;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float scale = clientSettings.scale.getValue();

        ScaledResolution sc = ScaleUtils.getScaledResolution(scale);

        mouseX = (int) (mouseX / scale);
        mouseY = (int) (mouseY / scale);

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
        boolean online = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 + 20 + 20 + 20 + 20 + 20 && mouseY < background.y + 20 + 20 + 20 + 20 + 20 + 20 + 20 + 15;

        float offset = 0;
        float yOffset = scrolls.y;
        for (Config config : Configs.getConfigs()) {
            boolean selectConfig = mouseX > background.x + 5 + offset && mouseX < background.x + 5 + offset + 100 && mouseY > background.y + 20 + yOffset && mouseY < background.y + 20 + yOffset + 30;
            if (load || save || delete || folder || refresh || create) break;
            if (selectConfig) {
                selectedConfig = config;
            }
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
                closing = true;
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
                String s = Configs.getCONFIG_DIRECTORY().getAbsolutePath();

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

            if (online) {
                new Thread(this::downloadOnlineConfigs).start();
            }

            if (refresh) {
                Configs.refreshConfigs();
                ClientUtils.chatLog("Successful refreshed.");
            }

            if (create) {
                creatingConfig = true;
                textField.setFocused(true);
            }

            if (selectedConfig != null) {
                if (load) {
                    Configs.loadConfig(selectedConfig);
                    ClientUtils.chatLog("Successful loaded config: " + selectedConfig.getName() + ".");
                }

                if (delete) {
                    Configs.deleteConfig(selectedConfig);
                    ClientUtils.chatLog("Successful deleted config: " + selectedConfig.getName() + ".");
                }

                if (save) {
                    Configs.saveConfig(selectedConfig);
                    ClientUtils.chatLog("Successful saved config: " + selectedConfig.getName() + ".");
                    Configs.saveConfig(Configs.getDefaultConfig());
                }
            }
        }
    }

    private void downloadOnlineConfigs() {
        try {
            ClientUtils.chatLog("Downloading online configs and models...");
            MessageChannel configsChannel = ClientIRC.getOnlineConfigsChannel();

            List<Message> messages = configsChannel.getIterableHistory().takeAsync(100).get();

            for (Message message : messages) {
                if (message.getAttachments().isEmpty()) continue;

                message.getAttachments().forEach(attachment -> {
                    try {
                        if (attachment.getFileName().endsWith(".json")) {
                            attachment.getProxy().downloadToFile(new File(Configs.getCONFIG_DIRECTORY() + "/" + attachment.getFileName()))
                                    .thenAccept(_ -> Configs.refreshConfigs());
                        }
                    } catch (Exception _) {
                        ClientUtils.chatLog("Failed download configs or models.");
                    }
                });
            }
        } catch (Exception e) {
            ClientUtils.chatLog("У ВАС ИНТЕРНЕТ ХУЕТА ПОЛНАЯ ИЛИ ЗАПРЕТ ПОЙДИ СКАЧАЙ ТУПОЙ УЕБАН!");
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