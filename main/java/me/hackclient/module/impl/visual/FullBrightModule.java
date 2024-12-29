package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "FullBright", category = Category.VISUAL, toggled = true)
public class FullBrightModule extends Module {

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = 1f;
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            mc.gameSettings.gammaSetting = 10000F;
        }
    }
}
