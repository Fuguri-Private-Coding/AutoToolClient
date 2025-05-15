package fuguriprivatecoding.autotool.module.impl.move;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.UpdateEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.Mode;

@ModuleInfo(name = "Fly", category = Category.MOVE)
public class Fly extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Vanilla")
            .setMode("Vanilla");

    final FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 0.6f, 0.6f, 0.1f) {};

    int jumps;

    @Override
    public void onDisable() {
        mc.thePlayer.stopMotion();
        mc.thePlayer.capabilities.flySpeed = 0.05f;
        mc.thePlayer.capabilities.isFlying = false;
        jumps = 0;
    }

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Vanilla" -> {
                if (event instanceof UpdateEvent) {
                    mc.thePlayer.capabilities.isFlying = true;
                    mc.thePlayer.capabilities.flySpeed = speed.getValue();
                }
            }
            case "Matrix test" -> {

            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}