package me.hackclient.guis.clickgui;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import net.minecraft.client.gui.GuiScreen;

public class NewClickGuiScreen extends GuiScreen {



    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {

        }
    }
}
