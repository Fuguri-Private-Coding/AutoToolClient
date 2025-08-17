package fuguriprivatecoding.autotoolrecode.hottext;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.guis.altmanager.AltManagerGuiText;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Blur;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class HotTextGui extends GuiScreen {
    public static final HotTextGui INST = new HotTextGui();

    Vector2f pos, size, lastMouse, lastSize, lastPos;
    boolean moving, closing, creatingHotKey, binding, changingText;
    int scroll, totalHeight = 0;
    final Animation2D background, sizeBackground, scrolls;

    public List<HotText> hotKeys = new ArrayList<>();

    private final AltManagerGuiText textField;
    private final AltManagerGuiText changeTextField;

    HotText selectedHotText;

    public HotTextGui() {
        Client.INST.getEventManager().register(this);
        mc = Minecraft.getMinecraft();

        ScaledResolution sc = new ScaledResolution(mc);

        lastSize = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        lastPos = new Vector2f(50f, 50f);
        lastMouse = new Vector2f(0, 0);

        size = new Vector2f(sc.getScaledWidth() - 100, sc.getScaledHeight() - 100);
        pos = new Vector2f(50f, 50f);
        textField = new AltManagerGuiText(1, mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);
        changeTextField = new AltManagerGuiText(1, mc.fontRendererObj, sc.getScaledWidth() / 2 - 50, sc.getScaledHeight() / 2, 100, 20);

        scrolls = new Animation2D();
        background = new Animation2D();
        sizeBackground = new Animation2D();
    }

    Glow shadows;
    Blur blur;
    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);
    ClientSettings clientSettings = Client.INST.getModuleManager().getModule(ClientSettings.class);
    Color mainColor;
    int delay = 30;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);

        mainColor = clickGui.fadeColor.isToggled() ?
                ColorUtils.fadeColor(clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue())
                : clickGui.color1.getColor();

        boolean hotScroll = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y + 15 && mouseY < background.y + sizeBackground.y;
        if (hotScroll) scroll -= Mouse.getDWheel() / 120 * 10;

        float hotKeysVisibleHeight = sizeBackground.y - 55;
        float maxScroll = Math.max(0, totalHeight - hotKeysVisibleHeight);

        if (scroll > 0) scroll = 0;
        if (scroll < -maxScroll) scroll = (int) -maxScroll;

        String name = switch (delay) {
            case 0, 1, 2 -> "HotKeys_";
            case 3, 4 -> "HotKeys";
            case 5, 6 -> "HotKey";
            case 7, 8 -> "HotKe";
            case 9, 10 -> "HotK";
            case 11, 12, 13 -> "Hot";
            case 14, 15, 16 -> "Ho";
            case 17, 18, 19 -> "H";
            case 20 -> "_";
            default -> "§k" + "HotKeys_".substring(0, min(delay - 20, 8));
        };

        if (closing) {
            if (Math.hypot(sizeBackground.x, sizeBackground.y) < 2) {
                closing = false;
                mc.displayGuiScreen(null);
                mc.currentScreen = null;
            }
        }

        float widthName = mc.fontRendererObj.getStringWidth(name);
        float widthCreate = fontRendererObj.getStringWidth("Create") / 2f;
        float widthDelete = fontRendererObj.getStringWidth("Delete") / 2f;

        scrolls.endY = scroll;
        background.endX = pos.x;
        background.endY = pos.y;
        sizeBackground.endX = size.x;
        sizeBackground.endY = size.y;

        scrolls.update(15f);
        background.update(15f);
        sizeBackground.update(15f);

        if (shadows.isToggled() && shadows.module.get("HotKeyGui")) {
            BloomRealUtils.addToDraw(() -> {
                RenderUtils.drawMixedRoundedRect(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue(), clickGui.color1.getColor(), clickGui.color2.getColor(), clickGui.fadeSpeed.getValue());
            });
        }

        if (blur.isToggled() && blur.module.get("HotKeyGui")) {
            GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, sizeBackground.y, clientSettings.backgroundRadius.getValue(), shadows.color.getColor()));
        }
        RenderUtils.drawRoundedOutLineRectangle(background.x - 0.5f, background.y - 0.5f, sizeBackground.x + 1, sizeBackground.y + 1, clientSettings.backgroundRadius.getValue() * 1.7f, new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB(),Color.BLACK.getRGB(),Color.BLACK.getRGB());

        RoundedUtils.drawRect(background.x, background.y, sizeBackground.x, 15, 0,clientSettings.backgroundRadius.getValue() / 1.25f,clientSettings.backgroundRadius.getValue() / 1.25f,0, Color.BLACK);

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y, sizeBackground.x, sizeBackground.y);

        mc.fontRendererObj.drawString(name, background.x + sizeBackground.x / 2f - widthName / 2 - 5, background.y + 3.5f,  -1);

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;

        RoundedUtils.drawRect(background.x + 4.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, quit ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 14.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, fullscreen ? Color.WHITE : Color.BLACK);
        RoundedUtils.drawRect(background.x + 24.5f, background.y + 3.5f, 7.5f, 7.5f, 4f, collapse ? Color.WHITE : Color.BLACK);

        RoundedUtils.drawRect(background.x + 5, background.y + 4, 6.5f, 6.5f, 3f, Color.red);
        RoundedUtils.drawRect(background.x + 15, background.y + 4, 6.5f, 6.5f, 3f, Color.yellow);
        RoundedUtils.drawRect(background.x + 25, background.y + 4, 6.5f, 6.5f, 3f, Color.green);
        int rectColor = new Color(0,0,0, clickGui.backgroundAlpha.getValue()).getRGB();

        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        fontRendererObj.drawString("Create", background.x + sizeBackground.x - 55 + 25 - widthCreate, background.y + 20 + 3, -1, true);
        RenderUtils.drawRoundedOutLineRectangle(background.x + sizeBackground.x - 55, background.y + 20 + 20, 50, 15, clientSettings.backgroundRadius.getValue() * 1.7f, rectColor, Color.BLACK.getRGB(),Color.BLACK.getRGB());
        fontRendererObj.drawString("Delete", background.x + sizeBackground.x - 55 + 25 - widthDelete, background.y + 20 + 20 + 3, -1, true);

        ScissorUtils.disableScissor();

        ScissorUtils.enableScissor();
        ScissorUtils.scissor(new ScaledResolution(mc), background.x, background.y + 15, sizeBackground.x, sizeBackground.y - 15);

        float offset = 0;
        float yOffset = scrolls.y;
        totalHeight = 0;
        for (HotText hotText : hotKeys) {
            String bindText = (hotText == selectedHotText ? (binding ? "▬" : (hotText.getKey() == 0 ? "-" : Keyboard.getKeyName(hotText.getKey()))) : (hotText.getKey() == 0 ? "-" : Keyboard.getKeyName(hotText.getKey())));
            Color selectedColor = selectedHotText != null ? selectedHotText == hotText ? new Color(50,50,50,150) : new Color(0,0,0,150) : new Color(0,0,0,150);
            RoundedUtils.drawRect(background.x + 5 + offset, background.y + 20 + yOffset, 150, 30, clientSettings.backgroundRadius.getValue(), selectedColor);
            fontRendererObj.drawString(bindText,background.x + 10 + 80 + 50 + offset, background.y + 30 + yOffset, -1);
            if (hotText != null) fontRendererObj.drawString(hotText.getText(), background.x + 10 + offset, background.y + 30 + yOffset, -1);
            offset += 155;

            if (offset > background.x + sizeBackground.x - 250) {
                yOffset += 35;
                offset = 0;
                totalHeight += 35;
            }
        }

        ScissorUtils.disableScissor();

        if (creatingHotKey) {
            RoundedUtils.drawRect(5,5,15,15,3f,Color.RED);
            textField.drawTextBox();
            textField.setMaxStringLength(20);
        }

        if (changingText) {
            RoundedUtils.drawRect(5,5,15,15,3f,Color.RED);
            changeTextField.drawTextBox();
            changeTextField.setMaxStringLength(20);
        }

        if (moving) {
            pos.translate(mouseX - lastMouse.x, mouseY - lastMouse.y);
            lastMouse.set(mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (creatingHotKey) textField.textboxKeyTyped(typedChar, keyCode);
        if (changingText) changeTextField.textboxKeyTyped(typedChar, keyCode);
        if (selectedHotText == null) binding = false;

        if (binding) {
            binding = false;
            if (keyCode == Keyboard.KEY_ESCAPE) {
                selectedHotText.setKey(Keyboard.KEY_NONE);
                return;
            }
            selectedHotText.setKey(keyCode);
        }

        if (keyCode == 1 && !closing && !creatingHotKey && !changingText) {
            ScaledResolution sc = new ScaledResolution(mc);
            Client.INST.getConfigManager().saveHotKeys();
            Client.INST.getHotTextManager().updateHotKeys();
            lastPos.set(pos);
            lastSize.set(size);
            closing = true;
            size.set(0, 0);
            pos.set(sc.getScaledWidth() / 2f, sc.getScaledHeight() / 2f);
        }

        if (keyCode == Keyboard.KEY_RETURN && creatingHotKey && !textField.getText().isEmpty()) {
            HotText hotText = new HotText(0, textField.getText(),0);
            hotKeys.add(hotText);
            textField.setText("");
            textField.setFocused(false);
            creatingHotKey = false;
        }

        if (keyCode == Keyboard.KEY_RETURN && changingText && !changeTextField.getText().isEmpty()) {
            selectedHotText.setText(changeTextField.getText());
            changeTextField.setText("");
            changeTextField.setFocused(false);
            changingText = false;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);

        if (mouseX > 5 && mouseX < 20 && mouseY > 5 && mouseY < 20) {
            if (creatingHotKey) {
                textField.setText("");
                textField.setFocused(false);
                creatingHotKey = false;
            } else if (changingText) {
                changeTextField.setText("");
                changeTextField.setFocused(false);
                changingText = false;
            }
        }

        if (creatingHotKey || changingText) return;

        boolean quit = mouseX > background.x + 5 && mouseX < background.x + 5 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean fullscreen = mouseX > background.x + 15 && mouseX < background.x + 15 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean collapse = mouseX > background.x + 25 && mouseX < background.x + 25 + 6.5 && mouseY > background.y + 4 && mouseY < background.y + 4 + 6;
        boolean move = mouseX > background.x && mouseX < background.x + sizeBackground.x && mouseY > background.y && mouseY < background.y + 15;
        boolean create = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 && mouseY < background.y + 20 + 15;
        boolean delete = mouseX > background.x + sizeBackground.x - 55 && mouseX < background.x + sizeBackground.x -5 && mouseY > background.y + 20 + 20 && mouseY < background.y + 20 + 20 + 15;

        float offset = 0;
        float yOffset = scrolls.y;
        totalHeight = 0;
        for (HotText hotText : hotKeys) {
            boolean tapHotText = mouseX > background.x + 5 + offset && mouseX < background.x + 5 + offset + 80 + 50 && mouseY > background.y + 20 + yOffset && mouseY < background.y + 20 + yOffset + 30;
            boolean tapBindHotText = mouseX > background.x + 5 + 50 + offset && mouseX < background.x + 5 + offset + 150 && mouseY > background.y + 20 + yOffset && mouseY < background.y + 20 + yOffset + 30;
            offset += 155;

            if (mouseButton == 0) {
                if (tapBindHotText && selectedHotText == hotText) {
                    binding = true;
                }
                if (tapHotText) selectedHotText = hotText;
            } else if (mouseButton == 1) {
                if (tapHotText) {
                    changingText = true;
                    changeTextField.setFocused(true);
                }
            }

            if (offset > background.x + sizeBackground.x - 250) {
                yOffset += 35;
                offset = 0;
                totalHeight += 35;
            }
        }

        if (mouseButton == 0) {
            if (mouseX > background.x + sizeBackground.x || mouseY > background.y + sizeBackground.y) return;

            if (quit) {
                Client.INST.getConfigManager().saveHotKeys();
                Client.INST.getHotTextManager().updateHotKeys();
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

            if (create) {
                creatingHotKey = true;
                textField.setFocused(true);
            }

            if (selectedHotText != null && hotKeys.contains(selectedHotText)) {
                if (delete) {
                    hotKeys.remove(selectedHotText);
                    selectedHotText = null;
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
        Client.INST.getConfigManager().loadHotKeys();
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
