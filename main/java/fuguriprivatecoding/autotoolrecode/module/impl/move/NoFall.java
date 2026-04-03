package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.FallDistanceEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "NoFall", category = Category.MOVE)
public class NoFall extends Module {

    Mode modes = new Mode("NoFallModes", this)
        .addModes("Vanilla")
        .setMode("Vanilla")
        ;

    @Override
    public void onEvent(Event event) {

        switch (modes.getMode()) {
            case "Vanilla" -> {
                if (event instanceof FallDistanceEvent e) {
                    e.cancel();
                }
            }
        }
    }
}
