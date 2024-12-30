package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.event.events.SprintEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "FarmGovno", category = Category.MISC)
public class FarmGovnoModule extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        mc.displayGuiScreen(null);
        if (event instanceof MoveButtonEvent moveButtonEvent) {
            moveButtonEvent.setForward(true);
            moveButtonEvent.setRight(true);
        }
        if (event instanceof SprintEvent) {
            mc.thePlayer.setSprinting(false);
        }
    }
}
