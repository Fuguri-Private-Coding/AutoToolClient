package fuguriprivatecoding.autotoolrecode.guis.clickgui.panel;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.AlphaUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class Panel {

    Module module;
    EasingAnimation openAnimation = new EasingAnimation(0);

    ClickGui clickGui = Client.INST.getModuleManager().getModule(ClickGui.class);

    float backgroundX;
    float backgroundY;
    float backgroundWidth;
    float backgroundHeight;

    EasingAnimation posX = new EasingAnimation(), posY = new EasingAnimation();

    int modulesScroll = 0;

    private final Vector2f moveOffset = new Vector2f();

    EasingAnimation moduleScrollAnim = new EasingAnimation();

    boolean closing, moving, closed;

    float settingsOffset = 0;

    public Panel(Module module, float x, float y, float width) {
        this.module = module;
        this.posY.setEnd(y);
        this.backgroundX = x;
        this.backgroundY = y;
        this.backgroundWidth = width;
    }

    ClientFontRenderer fontRenderer = Client.INST.getFonts().fonts.get("SFPro");

    public void render(int x, int y, int deltaScroll) {
        float panelRadius = 8;

        Color panelColor = new Color(0,0,0, clickGui.backgroundAlpha.getValue() / 255f);

        openAnimation.update(4f, Easing.OUT_CUBIC);
        openAnimation.setEnd(closing ? 0f : 1f);

        if (closing && openAnimation.getValue() < 1) {
            if (openAnimation.getValue() == 0) {
                closing = false;
                closed = true;
            }
        }

        backgroundX = posX.getValue() + openAnimation.getValue() * 150;
        backgroundY = posY.getValue();
        backgroundWidth = 150 * openAnimation.getValue();
        backgroundHeight = settingsOffset + 20 * openAnimation.getValue();

        if (GuiUtils.isHovered(x, y, backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
            modulesScroll += deltaScroll / 120 * 25;
        }

        if (moving) {
            posX.setEnd(x - moveOffset.x);
            posY.setEnd(y - moveOffset.y);
        }

        float altVisibleHeight = backgroundHeight;
        float maxScroll = Math.max(settingsOffset - altVisibleHeight, 0);
        modulesScroll = (int) Math.clamp(modulesScroll, -maxScroll, 0);

        moduleScrollAnim.update(4f, Easing.OUT_CUBIC);
        moduleScrollAnim.setEnd(modulesScroll);

        AlphaUtils.startWrite();

        RoundedUtils.drawRect(
            backgroundX, backgroundY,
            backgroundWidth, backgroundHeight,
            panelRadius,
            panelColor
        );

        RoundedUtils.drawRect(
            backgroundX, backgroundY,
            backgroundWidth, 15,
            panelRadius, 0,0,panelRadius,
            panelColor
        );

        fontRenderer.drawString(module.getName(), backgroundX + 5, backgroundY + 2.5f, Color.WHITE);

        ResourceLocation exitSettings = new ResourceLocation("minecraft", "autotool/mainmenu/exit.png");

        ColorUtils.glColor(GuiUtils.isHovered(x, y, backgroundX + backgroundWidth - 20, backgroundY, 15,15) ? Color.RED : Color.WHITE);
        RenderUtils.drawImage(exitSettings, backgroundX + backgroundWidth - 20, backgroundY, 15,15,true);

        settingsOffset = 0;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) return;
            float settingX = backgroundX + 5;
            float settingY = backgroundY + 5 + 15 + settingsOffset;

//            switch (setting) {
//                case FloatSetting floatSetting -> {
//
////                    RoundedUtils.drawRect();
//
////                    settingsOffset +=
//                }
//            }

        }

        AlphaUtils.endWrite();
        AlphaUtils.draw(openAnimation.getValue() * 1.7f);
    }

    public boolean clickMouse(int x, int y, int button) {
        if (GuiUtils.isHovered(x, y, backgroundX, backgroundY, backgroundWidth, 15) && button == 0) {
            if (GuiUtils.isHovered(x, y, backgroundX + backgroundWidth - 20, backgroundY, 15,15)) {
                closing = true;
                return true;
            }

            moveOffset.set(x - backgroundX, y - backgroundY);
            moving = true;
            return true;
        }

        return false;
    }

    public boolean mouseReleased(int x, int y, int button) {
        moving = false;
        return false;
    }
}
