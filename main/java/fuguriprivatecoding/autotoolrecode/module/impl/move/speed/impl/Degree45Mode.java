package fuguriprivatecoding.autotoolrecode.module.impl.move.speed.impl;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.KillAura;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.AbstractSpeedMode;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Fucker;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Degree45Mode extends AbstractSpeedMode {

    public Degree45Mode() {
        super("45Degree");
    }

    @Override
    public void onDisable(Speed speed) {
        if (!Modules.getModule(Scaffold.class).isToggled() && !Modules.getModule(KillAura.class).isToggled() && !Modules.getModule(Fucker.class).isToggled()) CameraRot.INST.setWillChange(false);
    }

    @Override
    public void handleEvent(Event event, Speed speed) {
        if (Modules.getModule(Scaffold.class).isToggled() || Modules.getModule(KillAura.class).isToggled() || Modules.getModule(Fucker.class).isToggled()) return;

        if (event instanceof TickEvent) {
            float yaw = speed.rotateWithMovement.isToggled() ? MoveUtils.getDir() : CameraRot.INST.getYaw();

            if (!mc.thePlayer.onGround) {
                yaw += 45;
            }

            Rot rotation = new Rot(yaw, CameraRot.INST.getPitch());

            Rot delta = mc.thePlayer.getRotation().deltaTo(rotation);
            CameraRot.INST.setUnlocked(true);
            mc.thePlayer.moveRotation(delta.fix());
        }
        
        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(
                CameraRot.INST.getYaw(),
                e.getForward(), 
                e.getStrafe()
            ));
        }

        if (event instanceof MoveButtonEvent e && MoveUtils.isMoving() && speed.jump.isToggled()) {
            e.setJump(true);
        }
    }
}