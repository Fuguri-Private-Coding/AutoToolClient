package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.EntityKilledEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;

@ModuleInfo(name = "KillEvents", category = Category.PLAYER, toggled = true)
public class KillEvents extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof EntityKilledEvent) {
            //ClientUtils.chatLog("Убит нахуй тварь сука");
            //ClientUtils.chatLog("Убит нахуй тварь сука");
            //ClientUtils.chatLog("Убит нахуй тварь сука");
            //ClientUtils.chatLog("Убит нахуй тварь сука");
            //ClientUtils.chatLog("Убит нахуй тварь сука");
        }
    }
}
