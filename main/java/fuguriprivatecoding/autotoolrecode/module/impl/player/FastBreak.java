package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "FastBreak", category = Category.PLAYER)
public class FastBreak extends Module {

    Mode breakMode = new Mode("BreakMode", this)
        .addMode("Legit")
        .setMode("Legit")
        ;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            switch (breakMode.getMode()) {
                case "Legit" -> mc.playerController.blockHitDelay = 0;
            }
        }
    }
}
