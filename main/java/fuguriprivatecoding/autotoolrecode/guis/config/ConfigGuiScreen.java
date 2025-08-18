package fuguriprivatecoding.autotoolrecode.guis.config;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.config.Config;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiText;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.KillAura;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Blur;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Util;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.Math.min;

public class ConfigGuiScreen extends GuiScreen {

    Vector2f pos, size, lastMouse, lastSize, lastPos;
    boolean moving, closing, creatingConfig;
    final Animation2D background, sizeBackground, scrolls;
    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);;
    ClientSettings clientSettings = Client.INST.getModuleManager().getModule(ClientSettings.class);
    int delay = 10;
    int scroll, totalHeight;
    Config selectedConfig;

    private final AltManagerGuiText textField;

    Color mainColor;

    Glow shadows;
    Blur blur;

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
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);

        mainColor = clickGui.fadeColor.isToggled() ?
                ColorUtils.fadeColor(clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue())
                : clickGui.color1.getColor();

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

        ClientFontRenderer font = Client.INST.getFonts().fonts.get("MuseoSans");

        double widthName = font.getStringWidth(name);
        double widthCreate = font.getStringWidth("Create") / 2f;
        double widthLoad = font.getStringWidth("Load") / 2f;
        double widthDelete = font.getStringWidth("Delete") / 2f;
        double widthSave = font.getStringWidth("Save") / 2f;
        double widthFolder = font.getStringWidth("Folder") / 2f;
        double widthRefresh = font.getStringWidth("Refresh") / 2f;
        double widthOnlineDownload = font.getStringWidth("Online") / 2f;

        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;
        scrolls.endY = scroll;

        background.update(15f);
        sizeBackground.update(15f);
        scrolls.update(15f);

        if (shadows.isToggled() && shadows.module.get("ConfigGui")) {
            BloomRealUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue(), clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue());
            });
        }

        if (blur.isToggled() && blur.module.get("ConfigGui")) {
            GaussianBlurUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue(), clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue());
            });
        }

        RenderUtils.drawRoundedOutLineRectangle(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue() * 1.7f, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 0,clientSettings.backgroundRadius.getValue() / 1.25f,clientSettings.backgroundRadius.getValue() / 1.25f,0, Color.BLACK);

        font.drawString(name, background.x + sizeBackground.x / 2f - widthName / 2 - 5, background.y + 3.5f + 2, Color.white);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, quit ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, fullscreen ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, collapse ? Color.WHITE : Color.BLACK);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15f, sizeBackground.x, sizeBackground.y - 15);
        int rectColor = new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB();

        float offset = 0;
        float yOffset = scrolls.y;
        totalHeight = 0;
        for (Config config : Client.INST.getConfigManager().getConfigs()) {
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

        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Create", background.x + sizeBackground.x - 55 + 25 - widthCreate, background.y + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Load", background.x + sizeBackground.x - 55 + 25 - widthLoad, background.y + 20 + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Save", background.x + sizeBackground.x - 55 + 25 - widthSave, background.y + 20 + 20 + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Delete", background.x + sizeBackground.x - 55 + 25 - widthDelete, background.y + 20 + 20 + 20 + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Folder", background.x + sizeBackground.x - 55 + 25 - widthFolder, background.y + 20 + 20 + 20 + 20 + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20 + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Refresh", background.x + sizeBackground.x - 55 + 25 - widthRefresh, background.y + 20 + 20 + 20 + 20 + 20 + 20 + 3 + 2, Color.WHITE, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20 + 20 + 20 + 20 + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        font.drawString("Online", background.x + sizeBackground.x - 55 + 25 - widthOnlineDownload, background.y + 20 + 20 + 20 + 20 + 20 + 20 + 20 + 3 + 2, Color.WHITE, true);

        ScissorUtils.disableScissor();

        if (creatingConfig) {
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

        if (keyCode == 1 && creatingConfig) {
            textField.setText("");
            textField.setFocused(false);
            creatingConfig = false;
            return;
        }

        if (keyCode == 1 && !closing) {
            ScaledResolution sc = new ScaledResolution(mc);
            lastPos.set(pos);
            lastSize.set(size);
            closing = true;
            size.set(0, 0);
            pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            if (creatingConfig && !textField.getText().isEmpty()) {
                Config config = new Config(textField.getText());
                Client.INST.getConfigManager().saveConfig(config);
                ClientUtils.chatLog("Successful created config: " + textField.getText() + ".");
                Client.INST.getConfigManager().refreshConfigs();
                textField.setText("");
                textField.setFocused(false);
                creatingConfig = false;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

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
        for (Config config : Client.INST.getConfigManager().getConfigs()) {
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

            if (online) {
                new Thread(this::downloadOnlineConfigs).start();
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
                    Client.INST.getConfigManager().saveConfig(Client.INST.getConfigManager().getDefaultConfig());
                }
            }
        }
    }

    private void downloadOnlineConfigs() {
        try {
            ClientUtils.chatLog("Downloading online configs and models...");
            MessageChannel configsChannel = Client.INST.getIrc().getOnlineConfigsChannel();

            List<Message> messages = configsChannel.getIterableHistory().takeAsync(100).get();

            for (Message message : messages) {
                if (message.getAttachments().isEmpty()) continue;

                message.getAttachments().forEach(attachment -> {
                    try {
                        if (attachment.getFileName().endsWith(".json")) {
                            attachment.getProxy().downloadToFile(new File(Client.INST.getConfigManager().getConfigsDirectory() + "/" + attachment.getFileName()))
                                    .thenAccept(_ -> Client.INST.getConfigManager().refreshConfigs());
                        }
                        if (attachment.getFileName().endsWith(".params")) {
                            attachment.getProxy().downloadToFile(new File(Client.INST.getModelsDirectory() + "/" + attachment.getFileName()))
                                    .thenAccept(_ -> Client.INST.getModuleManager().getModule(KillAura.class).updateModels());
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