package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;

@ModuleInfo(name = "Test", category = Category.MISC)
public class Test extends Module {

    int ticks;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            if (!mc.thePlayer.onGround) {
                ticks++;
                ClientUtils.chatLog("Ticks: " + ticks);
            } else {
                ticks = 0;
            }
        }
    }
}
