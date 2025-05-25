package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Vanilla", "Jump", "Intave")
            .setMode("Vanilla");

    CheckBox jump = new CheckBox("Jump", this, () -> mode.getMode().equalsIgnoreCase("Intave"));

    IntegerSetting hurtTime = new IntegerSetting("HurtTime", this,() -> mode.getMode().equalsIgnoreCase("Jump") || (mode.getMode().equalsIgnoreCase("Intave") && jump.isToggled()), 1, 10, 9);
    CheckBox forceForward = new CheckBox("ForceForward", this,() -> mode.getMode().equalsIgnoreCase("Jump") || (mode.getMode().equalsIgnoreCase("Intave") && jump.isToggled()));

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

            case "Intave" -> {
                if (event instanceof AttackEvent) {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= 0.6f;
                        mc.thePlayer.motionZ *= 0.6f;
                        mc.thePlayer.setSprinting(false);
                    }
                }
                if (event instanceof MoveButtonEvent moveButtonEvent && jump.isToggled()) {
                    boolean jump = mc.thePlayer.hurtTime == hurtTime.getValue() && mc.thePlayer.onGround && !moveButtonEvent.isJump() && MoveUtils.isMoving();
                    if (jump) {
                        if (forceForward.isToggled()) moveButtonEvent.setForward(true);
                        moveButtonEvent.setJump(true);
                    }
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
