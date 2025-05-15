package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.move.MoveUtils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "Jump")
            .setMode("Vanilla");

    IntegerSetting hurtTime = new IntegerSetting("HurtTime", this,() -> mode.getMode().equalsIgnoreCase("Jump"), 1, 10, 9);

    CheckBox forceForward = new CheckBox("ForceForward", this,() -> mode.getMode().equalsIgnoreCase("Jump"));

    final FloatSetting XZ = new FloatSetting("XZ", this,() -> mode.getMode().equalsIgnoreCase("Vanilla"), -1, 1, 0, 0.1f);
    final FloatSetting Y = new FloatSetting("Y", this,() -> mode.getMode().equalsIgnoreCase("Vanilla"), 0, 1, 1, 0.1f);

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Vanilla" -> {
                if (event instanceof PacketEvent packetEvent
                        && packetEvent.getPacket() instanceof S12PacketEntityVelocity s12
                        && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                    packetEvent.cancel();
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

            case "Jump" -> {
                if (event instanceof MoveButtonEvent moveButtonEvent) {
                    boolean jump = mc.thePlayer.hurtTime == hurtTime.getValue() && mc.thePlayer.onGround && !moveButtonEvent.isJump() && MoveUtils.isMoving();
                    if (jump) {
                        if (forceForward.isToggled()) moveButtonEvent.setForward(true);
                        moveButtonEvent.setJump(true);
                    }
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.getMode();
    }
}
