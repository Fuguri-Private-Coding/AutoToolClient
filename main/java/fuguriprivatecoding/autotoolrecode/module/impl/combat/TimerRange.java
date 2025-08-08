package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connection.BackTrack;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 2);

    IntegerSetting maxTargetHurtTime = new IntegerSetting("TargetHurtTime", this, 0, 10, 0);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);

    FloatSetting minDistanceToSkipTick = new FloatSetting("MinDistanceToSkipTick", this, 2.5f, 6, 3, 0.1f);

    CheckBox clickTeleport = new CheckBox("Click",this);

    boolean teleporting, click = false;
    int teleportTicks, balance = 0;

    @EventTarget
    public void onEvent(Event event) {
        if (teleporting) return;
        if (event instanceof LegitClickTimingEvent && click && clickTeleport.isToggled()) {
            mc.clickMouse();
            click = false;
        }

        if (event instanceof TickEvent e) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            if (target == null || target.hurtTime > maxTargetHurtTime.getValue()) return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, Rot.getServerRotation().getYaw());

            double posX = target.posX;
            double posY = target.posY;
            double posZ = target.posZ;
            double targetX = target.newPosX;
            double targetY = target.newPosY;
            double targetZ = target.newPosZ;
            int posRotIncrement = target.newPosRotationIncrements;

            teleportTicks = 0;
            for (int i = 0; i < maxTicks.getValue(); i++) {
                if (posRotIncrement > 0) {
                    posX += (targetX - posX) / posRotIncrement;
                    posY += (targetY - posY) / posRotIncrement;
                    posZ += (targetZ - posZ) / posRotIncrement;

                    posRotIncrement--;
                }

                AxisAlignedBB targetBox = getBBtoTp(target, new Vec3(target.nx, target.ny, target.nz), new Vec3(posX, posY, posZ));

                boolean skipTickDistance = DistanceUtils.getDistance(simulatedPlayer, targetBox) > minDistanceToSkipTick.getValue();

                if (skipTickDistance) {
                    simulatedPlayer.tick();
                } else {
                    teleportTicks = i;
                    break;
                }
            }

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (RayCastUtils.rayCast(3.0, 0, Rot.getServerRotation()).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        click = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            teleporting = false;
        }
        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }
    }

    private AxisAlignedBB getBBtoTp(EntityLivingBase target, Vec3 pos, Vec3 pos2) {
        BackTrack backTrack = Client.INST.getModuleManager().getModule(BackTrack.class);
        if (backTrack.isToggled() && !backTrack.packetBuffer.isEmpty()) {
            return target.getEntityBoundingBox().offset(
                    pos2.xCoord - target.posX, pos2.yCoord - target.posY, pos2.zCoord - target.posZ
            ).expand(
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize()
            );
        }
        return target.getEntityBoundingBox().offset(
                pos.xCoord - target.posX, pos.yCoord - target.posY, pos.zCoord - target.posZ
        ).expand(
                target.getCollisionBorderSize(),
                target.getCollisionBorderSize(),
                target.getCollisionBorderSize()
        );
    }
}