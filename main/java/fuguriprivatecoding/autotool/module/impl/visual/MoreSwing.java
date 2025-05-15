package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;

@ModuleInfo(name = "MoreSwing", category = Category.VISUAL)
public class MoreSwing extends Module {

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (Client.INST.getCombatManager().getTarget() != null) {
                if (mc.thePlayer.swingProgressInt >= 3 || mc.thePlayer.swingProgressInt < 0) {
                    mc.thePlayer.swingProgressInt = -1;
                    mc.thePlayer.isSwingInProgress = true;
                }
            }
        }
    }
}
