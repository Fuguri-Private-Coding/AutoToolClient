package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(
        name = "RotationHandler",
        category = Category.COMBAT
)
public class RotationHandler extends Module {

    final IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 0, 180, 30);
    final IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 0, 180, 30);
    final FloatSetting maxSmoothes = new FloatSetting("MaxSmooth", this, 1, 10, 2f, 0.1f) {};
    final FloatSetting minSmoothes = new FloatSetting("MinSmooth", this, 1, 10, 2f, 0.1f) {};
    final FloatSetting stopThreshold = new FloatSetting("StopThreshold", this, 0f, 10f, 0.1f, 0.1f) {};

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (!mm.getModule("Scaffold").isToggled() && (!mm.getModule("KillAura").isToggled() || Client.INSTANCE.getCombatManager().getTarget() == null)) {
            if (Rotation.isChanged()) {
                if (event instanceof TickEvent) {
                    Delta delta = RotationUtils.getDelta(Rotation.getServerRotation(), getPlayerRotation());

                    if (RotationUtils.fixDelta(delta).hypot() <= stopThreshold.getValue()) {
                        Rotation.setChanged(false);
                        return;
                    }

                    float randomizedSmooth = RandomUtils.nextFloat(minSmoothes.getValue(), maxSmoothes.getValue());

                    delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawSpeed.getValue(), yawSpeed.getValue()));
                    delta.setPitch(MathHelper.clamp(delta.getPitch(), -pitchSpeed.getValue(), pitchSpeed.getValue()));

                    delta.setYaw(delta.getYaw() / randomizedSmooth);
                    delta.setPitch(delta.getPitch() / randomizedSmooth);

                    delta = RotationUtils.fixDelta(delta);
                    Rotation.setServerRotation(
                            new Rotation(
                                    Rotation.getServerRotation().getYaw() + delta.getYaw(),
                                    Rotation.getServerRotation().getPitch() + delta.getPitch()
                            )
                    );
                }
                if (event instanceof MotionEvent motionEvent) {
                    motionEvent.setYaw(Rotation.getServerRotation().getYaw());
                    motionEvent.setPitch(Rotation.getServerRotation().getPitch());
                }
                if (event instanceof LookEvent lookEvent) {
                    lookEvent.setYaw(Rotation.getServerRotation().getYaw());
                    lookEvent.setPitch(Rotation.getServerRotation().getPitch());
                }
                if (event instanceof MoveFlyingEvent moveFlyingEvent) {
                    moveFlyingEvent.setCanceled(true);
                    MoveUtils.silentMoveFix(moveFlyingEvent);
                }
                if (event instanceof SprintEvent) {
                    if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
                        mc.thePlayer.setSprinting(false);
                    }
                }
                if (event instanceof JumpEvent jumpEvent) {
                    jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
                }
                if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
                    changeHeadRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
                    changeHeadRotationEvent.setPitch(Rotation.getServerRotation().getPitch());
                }
                if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
                    UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
                }
            } else {
                Rotation.setServerRotation(getPlayerRotation());
                Rotation.setChanged(false);
            }
        }
    }

    static Rotation getPlayerRotation() {
        return new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    @Override
    public boolean handleEvents() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    @Override
    public boolean isToggled() { return true; }
}
