package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "KillEvents", category = Category.PLAYER, toggled = true)
public class KillEvents extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
    }
}
