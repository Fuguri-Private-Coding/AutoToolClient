package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.module.Module;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.client.gui.ScaledResolution;

public class TargetHud extends Module {

    final IntegerSetting x = new IntegerSetting("PosX", this, 0, 1, 0);
    final IntegerSetting y = new IntegerSetting("PosY", this, 0, 1, 0);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        final ScaledResolution sc = new ScaledResolution(mc);
        x.setMax(sc.getScaledWidth());
        y.setMax(sc.getScaledHeight());

        if (!isToggled()) {
            return;
        }



    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
