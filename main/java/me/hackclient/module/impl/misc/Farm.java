package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.settings.impl.ModeSetting;

@ModuleInfo(
        name = "Farm",
        category = Category.MISC
)
public class Farm extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "ForwardRight",
            new String[] {
                    "ForwardRight",
                    "BackLeft",
            }
    );


    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MoveButtonEvent moveButtonEvent) {
            switch (mode.getMode()) {
                case "ForwardRight" -> {
                    moveButtonEvent.setForward(true);
                    moveButtonEvent.setRight(true);
                }
                case "BackLeft" -> {
                    moveButtonEvent.setBack(true);
                    moveButtonEvent.setLeft(true);
                }
            }
        }
    }
}
