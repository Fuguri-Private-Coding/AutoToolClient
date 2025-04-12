package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ModuleInfo(name = "BPSCounter", category = Category.VISUAL)
public class BPSCounter extends Module {

    FloatSetting posHorizontal = new FloatSetting("PosX", this, 0,0,0,0.1f);
    FloatSetting posVertical = new FloatSetting("PosY", this, 0,0,0,0.1f);
    FloatSetting radius = new FloatSetting("Radius", this, 0.5f,5,1f,0.1f);

    ColorSetting color = new ColorSetting("Color", this, 1,1,1,1);
    BooleanSetting textShadow = new BooleanSetting("TextShadow",this, true);
    ColorSetting textColor = new ColorSetting("TextColor", this, 1,1,1,1);

    BooleanSetting includeY = new BooleanSetting("includeY", this, false);

    Shadows shadows;
    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);

        ScaledResolution sc = new ScaledResolution(mc);

        posHorizontal.setMax(sc.getScaledWidth());
        posVertical.setMax(sc.getScaledHeight());

        if (event instanceof Render2DEvent) {
            String text = String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled()));
            float width = mc.fontRendererObj.getStringWidth(text);

            if (shadows.isToggled() && shadows.bpsCounter.isToggled()) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(posHorizontal.getValue(), posVertical.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, radius.getValue(), Color.WHITE));

            RoundedUtils.drawRect(posHorizontal.getValue(), posVertical.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, radius.getValue(), color.getColor());
            mc.fontRendererObj.drawString(String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled())), posHorizontal.getValue() + 2, posVertical.getValue() + 2, textColor.getColor().getRGB(), textShadow.isToggled());
        }
    }
}
