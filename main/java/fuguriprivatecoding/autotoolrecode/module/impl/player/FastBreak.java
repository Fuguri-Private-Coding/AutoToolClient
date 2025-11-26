package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "FastBreak", category = Category.PLAYER)
public class FastBreak extends Module {

    Mode breakMode = new Mode("BreakMode", this)
        .addModes("Legit", "Grim")
        .setMode("Legit")
        ;

    IntegerSetting delay = new IntegerSetting("Delay", this, () -> breakMode.is("Legit"), 0, 5, 0);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            switch (breakMode.getMode()) {
                case "Legit" -> mc.playerController.blockHitDelay = Math.min(mc.playerController.blockHitDelay, delay.getValue());
                case "Grim" -> {
                    if (mc.playerController.blockHitDelay > 0) {
                        mc.clickMouse();
                        mc.playerController.blockHitDelay = 0;
                    }
                }
            }
        }
    }
}
