package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.KeyBind;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Booster", category = Category.MOVE, description = "Позволяет вам телепортироватся по нажатию кнопке.")
public class Booster extends Module {

    KeyBind key = new KeyBind("Key", this, Keyboard.KEY_NONE);

    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 2);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);

    CheckBox checkBalance = new CheckBox("CheckBalance", this);

    boolean active, teleporting = false;

    int balance;

    @Override
    public void onEvent(Event event) {
        if (teleporting) return;
        if (event instanceof KeyEvent e && e.getKey() == key.getKey()) active = true;

        if (!active && checkBalance.isToggled()) {
            if (event instanceof TickEvent e) {
                if (balance > 0) {
                    e.cancel();
                    balance--;
                    return;
                }
            }
        }

        if (active) {
            if (event instanceof TickEvent) {
                teleporting = true;
                for (int i = 0; i < maxTicks.getValue(); i++) {
                    try {
                        mc.runTick();
                        if (checkBalance.isToggled()) balance++;
                    } catch (Exception _) {}
                }
                teleporting = false;
                active = false;
            }
        }
        if (event instanceof RunGameLoopEvent && balance > 0 && checkBalance.isToggled()) mc.timer.renderPartialTicks = partialTicks.getValue();
    }
}
