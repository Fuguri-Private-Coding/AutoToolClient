package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.AntiFireball;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Fucker;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "RotationHandler", category = Category.COMBAT, description = "Плавно поворачиватся обратно после изменения ротации.")
public class RotationHandler extends Module {

    final IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 0, 180, 30);
    final IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 0, 180, 30);

    final FloatSetting stopThreshold = new FloatSetting("StopThreshold", this, 1f, 10f, 0.1f, 0.1f) {};

    @EventTarget
    public void onEvent(Event event) {
        boolean handle = (Client.INST.getCombatManager().getTarget() == null
            || DistanceUtils.getDistance(Client.INST.getCombatManager().getTarget()) > Client.INST.getModuleManager().getModule(KillAura.class).rotateDistance.getValue())
            && !Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()
            && Client.INST.getModuleManager().getModule(AntiFireball.class).target == null &&
            !Client.INST.getModuleManager().getModule(Fucker.class).rotate &&
            Client.INST.getModuleManager().getModule(Fucker.class).bedPos == null;
        if (handle) {
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

                    delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawSpeed.getValue(), yawSpeed.getValue()));
                    delta.setPitch(MathHelper.clamp(delta.getPitch(), -pitchSpeed.getValue(), pitchSpeed.getValue()));

                    delta = RotUtils.fixDelta(delta);

                    rot = rot.add(delta);
                    rot.setPitch(Math.clamp(rot.getPitch(), -90, 90));

                    Rot.setServerRotation(rot);
                }
                if (event instanceof LookEvent lookEvent) {
                    lookEvent.setYaw(Rot.getServerRotation().getYaw());
                    lookEvent.setPitch(Rot.getServerRotation().getPitch());
                }
                if (event instanceof MoveEvent e) {
                    MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                }
                if (event instanceof MoveFlyingEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
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
    public boolean isToggled() { return super.isToggled(); }
}
