package fuguriprivatecoding.autotoolrecode.module.impl.move.speed.impl;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.move.Speed;
import fuguriprivatecoding.autotoolrecode.module.impl.move.speed.AbstractSpeedMode;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.util.MathHelper;

public class Degree45Mode extends AbstractSpeedMode {
    
    public Degree45Mode() {
        super("45Degree");
    }
    
    @Override
    public void handleEvent(Event event, Speed speed) {
        if (Modules.getModule(Scaffold.class).isToggled() || TargetStorage.getTarget() != null) return;

        if (event instanceof TickEvent) {
            float yaw = MoveUtils.getDir();

            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.onGround) {
                yaw += 45;
            }

            Rot rotation = new Rot(MathHelper.wrapDegree(yaw), mc.thePlayer.rotationPitch);
            Rot.setServerRotation(rotation.fix());
        }
        
        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(
                mc.thePlayer.rotationYaw,
                e.getForward(), 
                e.getStrafe()
            ));
        }

        if (event instanceof MotionEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof LookEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof ChangeHeadRotationEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof UpdateBodyRotationEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
    }
}