package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.ModeSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Fly", category = Category.MOVE, key = Keyboard.KEY_Z)
public class Fly extends Module {

    ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "Vanilla",
            new String[] {
                    "Vanilla",
                    "Matrix test"
            }
    );

    final FloatSetting speed = new FloatSetting("Speed", this, () -> mode.getMode().equalsIgnoreCase("Vanilla"), 0.1f, 0.6f, 0.6f, 0.1f) {};

    int jumps;

    @Override
    public void onDisable() {
        mc.thePlayer.stopMotion();
        mc.thePlayer.capabilities.flySpeed = 0.05f;
        mc.thePlayer.capabilities.isFlying = false;
        jumps = 0;
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);

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