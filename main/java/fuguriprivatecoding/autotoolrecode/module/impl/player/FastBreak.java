package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.BlockDamageEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.BlockHitDelayEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;

@ModuleInfo(name = "FastBreak", category = Category.PLAYER, description = "Позволяет быстрее ломать блоки.")
public class FastBreak extends Module {

    Mode breakMode = new Mode("BreakMode", this)
        .addModes("Delay", "DropDelay")
        .setMode("DropDelay")
        ;

    IntegerSetting delay = new IntegerSetting("Delay", this, () -> breakMode.is("Delay"), 0, 5, 0);
    IntegerSetting clicks = new IntegerSetting("Clicks", this, () -> breakMode.is("DropDelay"), 1,3,1);

    FloatSetting multiple = new FloatSetting("Multiple", this, 0, 2f, 1f, 0.01f);

    @Override
    public void onEvent(Event event) {
        switch (breakMode.getMode()) {
            case "Delay" -> {
                if (event instanceof BlockHitDelayEvent e) {
                    e.setDelay(delay.getValue());
                }
            }

            case "DropDelay" -> {
                if (event instanceof TickEvent) {
                    if (mc.playerController.blockHitDelay > 0) {
                        for (int i = 0; i < clicks.getValue(); i++) mc.clickMouse();
                        mc.playerController.blockHitDelay = 0;
                    }
                }
            }
        }

        if (event instanceof BlockDamageEvent e) {
           float current = e.getAddingDamage();
           e.setAddingDamage(current * multiple.getValue());
        }
    }
}
