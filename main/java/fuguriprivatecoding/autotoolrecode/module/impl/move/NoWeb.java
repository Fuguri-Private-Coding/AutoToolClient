package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "NoWeb", category = Category.MOVE, description = "Позволяет свободно двигатся в паутине.")
public class NoWeb extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Vanilla").setMode("Vanilla");

    @EventTarget
    public void onEvent(Event event) {
        if (!mc.thePlayer.isInWeb) return;
        if (event instanceof UpdateEvent) {
            switch (mode.getMode()) {
                case "Vanilla" -> mc.thePlayer.isInWeb = false;
            }
        }
    }
}
