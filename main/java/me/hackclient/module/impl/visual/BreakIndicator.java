package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ModuleInfo(
        name = "BreakIndicator",
        category = Category.VISUAL
)
public class BreakIndicator extends Module {

    FloatSetting posHorizontal = new FloatSetting("PosX", this, 0,10000,0,0.1f);
    FloatSetting posVertical = new FloatSetting("PosY", this, 0,10000,0,0.1f);
    FloatSetting radius = new FloatSetting("Radius", this, 0.5f,5,1f,0.1f);

    ColorSetting color = new ColorSetting("Color", this, 0,0,0,0.4f);
    BooleanSetting textShadow = new BooleanSetting("TextShadow",this, true);
    ColorSetting textColor = new ColorSetting("TextColor", this, 1,1,1,1);

    Shadows shadows;

    @Override
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);

        ScaledResolution sc = new ScaledResolution(mc);

        posHorizontal.setMax(sc.getScaledWidth());
        posVertical.setMax(sc.getScaledHeight());

        FontRenderer font = mc.fontRendererObj;
        String text = "Progress: " + String.format("%.0f", mc.playerController.curBlockDamageMP * 100) + "%";
        float width = font.getStringWidth(text);

        if (event instanceof Render2DEvent && mc.playerController.curBlockDamageMP > 0) {
            if (shadows.isToggled() && shadows.breakIndicator.isToggled()) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(posHorizontal.getValue(), posVertical.getValue(), width + 4, font.FONT_HEIGHT + 4, radius.getValue(), Color.WHITE));

            RoundedUtils.drawRect(posHorizontal.getValue(), posVertical.getValue(), width + 4, font.FONT_HEIGHT + 4, radius.getValue(), color.getColor());
            font.drawString(text, posHorizontal.getValue() + 2.5f, posVertical.getValue() + 2.5f, textColor.getColor().getRGB(), textShadow.isToggled());
        }
    }
}
