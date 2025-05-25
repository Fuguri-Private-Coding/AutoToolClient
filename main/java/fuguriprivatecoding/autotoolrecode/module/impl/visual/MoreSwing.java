package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;

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
