package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rot;
import me.hackclient.utils.rotation.RotUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "RotationHandler", category = Category.COMBAT)
public class RotationHandler extends Module {

    final IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 0, 180, 30);
    final IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 0, 180, 30);

    final FloatSetting minSmoothes = new FloatSetting("MinSmooth", this, 1, 10, 2f, 0.1f) {
        @Override
        public float getValue() {
            if (maxSmoothes.value < value) { value = maxSmoothes.value; }
            return super.getValue();
        }
    };

    final FloatSetting maxSmoothes = new FloatSetting("MaxSmooth", this, 1, 10, 2f, 0.1f) {
        @Override
        public float getValue() {
            if (minSmoothes.value > value) { value = minSmoothes.value; }
            return super.getValue();
        }
    };

    final FloatSetting stopThreshold = new FloatSetting("StopThreshold", this, 0f, 10f, 0.1f, 0.1f) {};

    @EventTarget
    public void onEvent(Event event) {
        if (!Client.INST.getModuleManager().getModule("Scaffold").isToggled() && (!Client.INST.getModuleManager().getModule("KillAura").isToggled() || Client.INST.getCombatManager().getTarget() == null)) {
            if (Rot.isChanged()) {
                if (event instanceof MotionEvent motionEvent) {
                    Rot rot = Rot.getServerRotation().copy();
                    motionEvent.setYaw(rot.getYaw());
                    motionEvent.setPitch(rot.getPitch());

                    Delta delta = RotUtils.getDelta(Rot.getServerRotation(), getPlayerRotation());

                    if (RotUtils.fixDelta(delta).hypot() <= stopThreshold.getValue()) {
                        Rot.setChanged(false);
                        return;
                    }

                    float randomizedSmooth = RandomUtils.nextFloat(minSmoothes.getValue(), maxSmoothes.getValue());

                    delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawSpeed.getValue(), yawSpeed.getValue()));
                    delta.setPitch(MathHelper.clamp(delta.getPitch(), -pitchSpeed.getValue(), pitchSpeed.getValue()));

                    delta.setYaw(delta.getYaw() / randomizedSmooth);
                    delta.setPitch(delta.getPitch() / randomizedSmooth);

                    delta = RotUtils.fixDelta(delta);

                    rot = rot.add(delta);
                    rot.setPitch(Math.clamp(rot.getPitch(), -90, 90));

                    Rot.setServerRotation(rot);
                }
                if (event instanceof LookEvent lookEvent) {
                    lookEvent.setYaw(Rot.getServerRotation().getYaw());
                    lookEvent.setPitch(Rot.getServerRotation().getPitch());
                }
                if (event instanceof MoveFlyingEvent moveFlyingEvent) {
                    moveFlyingEvent.setYaw(Rot.getServerRotation().getYaw());
                }
                if (event instanceof MoveEvent e) {
                    MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                }
                if (event instanceof SprintEvent) {
                    if (Math.abs(MoveUtils.getDirection() - MoveUtils.getDirection(Rot.getServerRotation().getYaw())) > 45) {
                        mc.thePlayer.setSprinting(false);
                    }
                }
                if (event instanceof JumpEvent jumpEvent) {
                    jumpEvent.setYaw(Rot.getServerRotation().getYaw());
                }
                if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
                    changeHeadRotationEvent.setYaw(Rot.getServerRotation().getYaw());
                    changeHeadRotationEvent.setPitch(Rot.getServerRotation().getPitch());
                }
                if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
                    UpdateBodyRotationEvent.setYaw(Rot.getServerRotation().getYaw());
                }
            } else {
                Rot.setServerRotation(new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));
                Rot.setChanged(false);
            }
        }
    }

    static Rot getPlayerRotation() {
        return new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    @Override
    public boolean isToggled() { return true; }
}
