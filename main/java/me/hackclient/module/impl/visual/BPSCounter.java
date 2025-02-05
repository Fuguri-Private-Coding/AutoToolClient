package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.client.gui.ScaledResolution;

@ModuleInfo(name = "BPSCounter", category = Category.VISUAL)
public class BPSCounter extends Module {

    IntegerSetting x = new IntegerSetting("x", this, 0, 10, 0);
    IntegerSetting y = new IntegerSetting("y", this, 0, 10, 0);
    BooleanSetting includeY = new BooleanSetting("includeY", this, false);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            x.setMax(sc.getScaledWidth());
            y.setMax(sc.getScaledHeight());
            mc.fontRendererObj.drawString(
                String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled())),
                x.getValue(),
                y.getValue(),
                -1
            );
        }
    }
}
