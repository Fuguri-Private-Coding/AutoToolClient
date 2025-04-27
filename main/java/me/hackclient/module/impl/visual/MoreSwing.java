package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;

@ModuleInfo(name = "MoreSwing", category = Category.VISUAL)
public class MoreSwing extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            if (Client.INSTANCE.getCombatManager().getTarget() != null) {
                if (mc.thePlayer.swingProgressInt >= 3 || mc.thePlayer.swingProgressInt < 0) {
                    mc.thePlayer.swingProgressInt = -1;
                    mc.thePlayer.isSwingInProgress = true;
                }
            }
        }
    }

    @Override
    public boolean handleEvents() {
        return isToggled();
    }
}
