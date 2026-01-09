package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, -2.5f, 2.5f, 1, 0.1f);
    IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);

    public static boolean teleporting = false, click = false;
    int teleportTicks, posRotIncrement = 0;
    public static int balance = 0;

    Vec3 targetPos, pos;

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();
        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof LegitClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            AxisAlignedBB box = RotUtils.getHitBox(target, 100, 100);

            float yaw = RotUtils.getBestRotation(box).getYaw();
            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

            pos = target.getPositionVector();
            targetPos = target.getNewPosition();
            posRotIncrement = target.newPosRotationIncrements;

            teleportTicks = 0;
            for (int i = 0; i < maxTicks.getValue(); i++) {
                updateCashedIncrementPos();

                AxisAlignedBB targetBox = getPredictBB(target, target.getNPosition(), pos);
                boolean skip = DistanceUtils.getDistance(simulatedPlayer, targetBox) > 3.0D;

                if (skip) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i;
                break;
            }

            if (target.hurtTime > maxTargetHurtTime.getValue()) return;

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (i == teleportTicks - 1) {
                        click = true;
                        balance += additionalTicks.getValue();
                    }
                } catch (Exception ignored) {}
            }
            teleporting = false;
        }
    }

    private void updateCashedIncrementPos() {
        if (posRotIncrement > 0) {
            Vec3 cashingPos = new Vec3(
                    (targetPos.xCoord - pos.xCoord) / posRotIncrement,
                    (targetPos.yCoord - pos.yCoord) / posRotIncrement,
                    (targetPos.zCoord - pos.zCoord) / posRotIncrement
            );
            pos = pos.add(cashingPos);
            posRotIncrement--;
        }
    }

    private AxisAlignedBB getPredictBB(EntityLivingBase target, Vec3 newPos, Vec3 pos) {
        double offsetX = BackTrack.working ? pos.xCoord - target.posX : newPos.xCoord - target.posX;
        double offsetY = BackTrack.working ? pos.yCoord - target.posY : newPos.yCoord - target.posY;
        double offsetZ = BackTrack.working ? pos.zCoord - target.posZ : newPos.zCoord - target.posZ;

        return target.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
    }

    public static boolean isTeleporting() {
        return TimerRange.teleporting || TimerRange.balance > 0;
    }
}