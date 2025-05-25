package fuguriprivatecoding.autotoolrecode.guis.clickgui;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import net.minecraft.client.gui.GuiScreen;

public class NewClickGuiScreen extends GuiScreen {



    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {

        }
    }
}
