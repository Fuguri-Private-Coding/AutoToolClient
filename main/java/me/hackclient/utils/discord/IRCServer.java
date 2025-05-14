package me.hackclient.utils.discord;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.utils.interfaces.Imports;

public class IRCServer implements Imports {

    public IRCServer() {
        Client.INSTANCE.getEventManager().register(this);
    }

    String ip = "";

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof WorldChangeEvent) {
            if (mc.getCurrentServerData() != null && !ip.equalsIgnoreCase(mc.getCurrentServerData().serverIP)) {
                ip = mc.getCurrentServerData().serverIP;

            }
        }
    }

}
