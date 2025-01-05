package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;

@ModuleInfo(name = "TimeChanger", category = Category.VISUAL, toggled = true)
public class TImeChanger extends Module {

    IntegerSetting time = new IntegerSetting("Time", this, 1, 20, 20);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof RunGameLoopEvent) {
            mc.theWorld.setWorldTime(time.getValue() * 1000L);
        }
    }
}
