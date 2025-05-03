package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

    final ModeSetting mode = new ModeSetting("Mode", this)
            .addModes("Vanilla")
            .setMode("Vanilla");

    final FloatSetting XZ = new FloatSetting("XZ", this, -1, 1, 0, 0.1f);
    final FloatSetting Y = new FloatSetting("Y", this, 0, 1, 1, 0.1f);

    @EventTarget
    public void onEvent(Event event) {
        if (mode.getMode().equalsIgnoreCase("Vanilla")) {
            if (event instanceof PacketEvent e
                    && e.getPacket() instanceof S12PacketEntityVelocity s12
                    && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                double needMotionX = s12.getMotionX() / 8000d;
                double needMotionY = s12.getMotionY() / 8000d;
                double needMotionZ = s12.getMotionZ() / 8000d;

                double deltaMotionX = needMotionX - mc.thePlayer.motionX;
                double deltaMotionY = needMotionY - mc.thePlayer.motionY;
                double deltaMotionZ = needMotionZ - mc.thePlayer.motionZ;

                deltaMotionX *= XZ.getValue();
                deltaMotionY *= Y.getValue();
                deltaMotionZ *= XZ.getValue();

                mc.thePlayer.motionX += deltaMotionX;
                mc.thePlayer.motionY += deltaMotionY;
                mc.thePlayer.motionZ += deltaMotionZ;
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}
