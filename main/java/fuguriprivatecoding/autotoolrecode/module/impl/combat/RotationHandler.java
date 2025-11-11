package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.module.impl.player.AntiFireball;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Fucker;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;

@ModuleInfo(name = "RotationHandler", category = Category.COMBAT, description = "Плавно поворачиватся обратно после изменения ротации.")
public class RotationHandler extends Module {

    final IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 0, 180, 30);
    final IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 0, 180, 30);

    final FloatSetting stopThreshold = new FloatSetting("StopThreshold", this, 1f, 10f, 0.1f, 0.1f) {};

    @Override
    public void onEvent(Event event) {
        boolean handle = (TargetStorage.getTarget() == null
            || DistanceUtils.getDistance(TargetStorage.getTarget()) > Modules.getModule(KillAura.class).rotateDistance.getValue())
            && !Modules.getModule(Scaffold.class).isToggled()
            && Modules.getModule(AntiFireball.class).target == null &&
            Modules.getModule(Fucker.class).bedPos == null
            && (!Modules.getModule(Speed.class).isToggled() && Modules.getModule(Speed.class).mode.is("45Degree"));
        if (handle) {
            if (Rot.isChanged()) {
                Rot rot = Rot.getServerRotation().copy();
                if (event instanceof TickEvent) {
                    Rot delta = RotUtils.getDelta(Rot.getServerRotation(), mc.thePlayer.getRotations());

                    if (RotUtils.fixDelta(delta).hypot() <= stopThreshold.getValue()) {
                        Rot.setChanged(false);
                        return;
                    }

                    Rot rotateSpeed = new Rot(
                        yawSpeed.getValue(),
                        pitchSpeed.getValue()
                    );

                    RotUtils.limitDelta(delta, rotateSpeed);
                    delta = RotUtils.fixDelta(delta);

                    rot = rot.add(delta);
                    Rot.setServerRotation(rot);
                }

                if (event instanceof MotionEvent e) {
                    e.setYaw(rot.getYaw());
                    e.setPitch(rot.getPitch());
                }

                if (event instanceof LookEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                    e.setPitch(Rot.getServerRotation().getPitch());
                }

                if (event instanceof MoveEvent e) MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());
                if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
                if (event instanceof UpdateBodyRotationEvent e) e.setYaw(Rot.getServerRotation().getYaw());

                if (event instanceof ChangeHeadRotationEvent e) {
                    e.setYaw(Rot.getServerRotation().getYaw());
                    e.setPitch(Rot.getServerRotation().getPitch());
                }
            } else {
                Rot.setServerRotation(new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));
                Rot.setChanged(false);
            }
        }
    }

    @Override
    public boolean isToggled() { return super.isToggled(); }
}
