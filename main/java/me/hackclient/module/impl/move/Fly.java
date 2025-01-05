package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Fly", category = Category.MOVE, key = Keyboard.KEY_Z)
public class Fly extends Module {

    FloatSetting speed = new FloatSetting("Speed", this, 0.1f, 0.6f, 0.6f, 0.1f);

    @Override
    public void onDisable() {
        mc.thePlayer.stopMotion();
        mc.thePlayer.capabilities.flySpeed = 0.05f;
        mc.thePlayer.capabilities.isFlying = false;
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.flySpeed = speed.getValue();
        }
    }
}