package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

@ModuleInfo(
        name = "Velocity",
        category = Category.COMBAT
)
public class Velocity extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "Legit",
            new String[] {
                    "Legit",
                    "Intave",
                    "Vanilla",
                    "Push"
            }
    );

    final IntegerSetting minPlayerHurtTimeLegit = new IntegerSetting("MinPlayerHurtTime", this, 0, 9, 7);

    // Legit
    final BooleanSetting forceHoldForwardWhenDamaged = new BooleanSetting("ForceHoldForwardWhenDamaged", this, () -> mode.getMode().equals("Legit"), true);

    // Intave
    final IntegerSetting minPlayerHurtTimeIntave = new IntegerSetting("MinPlayerHurtTime", this, () -> mode.getMode().equals("Intave"), 0, 9, 7);
    final BooleanSetting jump = new BooleanSetting("Jump", this, () -> mode.getMode().equals("Intave"), true);
    final BooleanSetting cancelSprint = new BooleanSetting("CancelSprintAtSprintHit", this, () -> mode.getMode().equals("Intave"), true);
    final FloatSetting sprintReduce = new FloatSetting("SprintReduce", this, () -> mode.getMode().equals("Intave"), 0.0f, 1.0f, 0.6f, 0.1f) {};
    final FloatSetting normalReduce = new FloatSetting("NormalReduce", this, () -> mode.getMode().equals("Intave"), 0.0f, 1.0f, 1.0f, 0.1f) {};

    // Vanilla
    final FloatSetting xz = new FloatSetting("XZ", this, () -> mode.getMode().equals("Vanilla"), -1.0f, 1.0f, 0.0f, 0.1f) {};
    final FloatSetting y = new FloatSetting("Y", this, () -> mode.getMode().equals("Vanilla"), 0, 1.0f, 0.0f, 0.1f) {};
    final BooleanSetting saveMotion = new BooleanSetting("SaveMotion", this, () -> mode.getMode().equals("Vanilla"), true);

    // Push
    final FloatSetting pushMotion = new FloatSetting("Motion (Divide by 100)", this, () -> mode.getMode().equals("Push"), 1, 10, 2, 0.1f) {};
    final IntegerSetting startHurtTimeToPush = new IntegerSetting("Start", this, () -> mode.getMode().equals("Push"), 0, 9, 0);
    final IntegerSetting endHurtTimeToPush = new IntegerSetting("End", this, () -> mode.getMode().equals("Push"), 0, 9, 3);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        switch (mode.getMode()) {
            case "Legit" -> {
                boolean damaged = mc.thePlayer.hurtTime >= minPlayerHurtTimeLegit.getValue();

                if (event instanceof MoveButtonEvent moveButtonEvent && mc.currentScreen == null && damaged) {
                    if (forceHoldForwardWhenDamaged.isToggled()) {
                        moveButtonEvent.setBack(false);
                        moveButtonEvent.setForward(true);
                    }
                    if (mc.thePlayer.onGround && Math.random() < 0.5) {
                        moveButtonEvent.setJump(true);
                    }
                }
            }
            case "Intave" -> {
                boolean attacking = event instanceof AttackEvent attackEvent && attackEvent.getHittingEntity() instanceof EntityLivingBase;
                boolean damaged = mc.thePlayer.hurtTime >= minPlayerHurtTimeIntave.getValue();
                if (event instanceof MoveButtonEvent moveButtonEvent && damaged && mc.thePlayer.onGround && jump.isToggled()) {
                    moveButtonEvent.setJump(true);
                    moveButtonEvent.setForward(true);
                    moveButtonEvent.setBack(false);
                }
                if (attacking && damaged) {
                    if (mc.thePlayer.isSprinting()) {
                        mc.thePlayer.motionX *= sprintReduce.getValue();
                        mc.thePlayer.motionZ *= sprintReduce.getValue();
                        if (cancelSprint.isToggled()) {
                            mc.thePlayer.setSprinting(false);
                        }
                    } else {
                        mc.thePlayer.motionX *= normalReduce.getValue();
                        mc.thePlayer.motionZ *= normalReduce.getValue();
                    }
                }
            }
            case "Vanilla" -> {
                if (event instanceof PacketEvent packetEvent) {
                    Packet packet = packetEvent.getPacket();

                    if (!(packet instanceof S12PacketEntityVelocity s12) || s12.getEntityID() != mc.thePlayer.getEntityId())
                        break;

                    packetEvent.setCanceled(true);

                    double serverMotionX = s12.getMotionX() / 8000f;
                    double serverMotionY = s12.getMotionY() / 8000f;
                    double serverMotionZ = s12.getMotionZ() / 8000f;

                    if (saveMotion.isToggled()) {
                        mc.thePlayer.motionX += (serverMotionX - mc.thePlayer.motionX) * xz.getValue();
                        mc.thePlayer.motionY += (serverMotionY - mc.thePlayer.motionY) * y.getValue();
                        mc.thePlayer.motionZ += (serverMotionZ - mc.thePlayer.motionZ) * xz.getValue();
                    } else {
                        mc.thePlayer.motionX = serverMotionX * xz.getValue();
                        mc.thePlayer.motionY = serverMotionY * y.getValue();
                        mc.thePlayer.motionZ = serverMotionZ * xz.getValue();
                    }
                }
            }
            case "Push" -> {
                if (event instanceof UpdateEvent
                && mc.thePlayer.hurtTime >= startHurtTimeToPush.getValue()
                && mc.thePlayer.hurtTime <= endHurtTimeToPush.getValue()) {
                    float yaw = (float) Math.toRadians(mc.thePlayer.rotationYaw);
                    mc.thePlayer.motionX -= Math.sin(yaw) * pushMotion.getValue() / 100;
                    mc.thePlayer.motionZ += Math.cos(yaw) * pushMotion.getValue() / 100;
                }
            }
        }
    }
}
