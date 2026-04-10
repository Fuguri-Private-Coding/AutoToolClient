package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

@ModuleInfo(name = "KeyStrokes", category = Category.VISUAL)
public class KeyStrokes extends Module {

    IntegerSetting posX = new IntegerSetting("PosX", this, 0, 100, 5);
    IntegerSetting posY = new IntegerSetting("PosY", this, 0, 100, 5);

    FloatSetting radius = new FloatSetting("Radius", this, 0, 10, 5, 0.1f);
    FloatSetting size = new FloatSetting("Size", this, 10, 50, 25, 0.1f);

    FloatSetting gandon = new FloatSetting("Gap", this, 0, 10, 2, 0.1f);

    ColorSetting unToggleColor = new ColorSetting("UnToggleColor", this);
    ColorSetting toggleColor = new ColorSetting("ToggleColor", this);

    CheckBox glow = new CheckBox("Glow", this, false);
    ColorSetting glowUnToggleColor = new ColorSetting("GlowUnToggleColor", this, glow::isToggled);
    ColorSetting glowToggleColor = new ColorSetting("GlowToggleColor", this, glow::isToggled);

    CheckBox blur = new CheckBox("Blur", this, false);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ScaledResolution sc = e.getScaledResolution();
            Vector2f pos = GuiUtils.getAbsolutePos(posX.getValue(), posY.getValue(), sc);

            float size = this.size.getValue();

            boolean wHold = mc.gameSettings.keyBindForward.isKeyDown();
            boolean aHold = mc.gameSettings.keyBindLeft.isKeyDown();
            boolean dHold = mc.gameSettings.keyBindRight.isKeyDown();
            boolean sHold = mc.gameSettings.keyBindBack.isKeyDown();

            float radius = this.radius.getValue();

            Color unToggleColor = this.unToggleColor.getFadedColor();
            Color toggleColor = this.toggleColor.getFadedColor();

            Color wColor = wHold ? toggleColor : unToggleColor;
            Color aColor = aHold ? toggleColor : unToggleColor;
            Color dColor = dHold ? toggleColor : unToggleColor;
            Color sColor = sHold ? toggleColor : unToggleColor;

            float sizeWithGap = size + gandon.getValue() + 1;

            float posX_W = pos.x + sizeWithGap;
            float posY_W = pos.y;

            float posX_A = pos.x;
            float posY_A = pos.y + sizeWithGap;

            float posX_D = pos.x + sizeWithGap + sizeWithGap;
            float posY_D = pos.y + sizeWithGap;

            float posX_S = pos.x + sizeWithGap;
            float posY_S = pos.y + sizeWithGap;

            RoundedUtils.drawRect(posX_W, posY_W, size, size, radius, wColor);
            RoundedUtils.drawRect(posX_A, posY_A, size, size, radius, aColor);
            RoundedUtils.drawRect(posX_D, posY_D, size, size, radius, dColor);
            RoundedUtils.drawRect(posX_S, posY_S, size, size, radius, sColor);

            if (glow.isToggled()) {
                Color glowUnToggleColor = this.glowUnToggleColor.getFadedColor();
                Color glowToggleColor = this.glowToggleColor.getFadedColor();

                Color glowWColor = wHold ? glowToggleColor : glowUnToggleColor;
                Color glowAColor = aHold ? glowToggleColor : glowUnToggleColor;
                Color glowDColor = dHold ? glowToggleColor : glowUnToggleColor;
                Color glowSColor = sHold ? glowToggleColor : glowUnToggleColor;

                BloomUtils.startWrite();
                RenderUtils.drawMixedRoundedRect(posX_W, posY_W, size, size, radius, glowWColor, glowWColor, 0);
                RenderUtils.drawMixedRoundedRect(posX_A, posY_A, size, size, radius, glowAColor, glowAColor, 0);
                RenderUtils.drawMixedRoundedRect(posX_D, posY_D, size, size, radius, glowDColor, glowDColor, 0);
                RenderUtils.drawMixedRoundedRect(posX_S, posY_S, size, size, radius, glowSColor, glowSColor, 0);
                BloomUtils.stopWrite();
            }

            if (blur.isToggled()) {
                BlurUtils.startWrite();
                RoundedUtils.drawRect(posX_W, posY_W, size, size, radius, Color.WHITE);
                RoundedUtils.drawRect(posX_A, posY_A, size, size, radius, Color.WHITE);
                RoundedUtils.drawRect(posX_D, posY_D, size, size, radius, Color.WHITE);
                RoundedUtils.drawRect(posX_S, posY_S, size, size, radius, Color.WHITE);
                BlurUtils.stopWrite();
            }
        }
    }
}
