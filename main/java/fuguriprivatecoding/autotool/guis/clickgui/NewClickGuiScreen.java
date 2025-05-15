package fuguriprivatecoding.autotool.guis.clickgui;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import net.minecraft.client.gui.GuiScreen;

public class NewClickGuiScreen extends GuiScreen {



    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {

        }
    }
}
