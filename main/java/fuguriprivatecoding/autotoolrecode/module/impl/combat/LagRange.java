package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "LagRange", category = Category.COMBAT)
public class LagRange extends Module {

    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 1, 10, 4);
    final FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 0);
    final FloatSetting additionalDistance = new FloatSetting("AdditionalDistance", this, 0,1,0.3f, 0.05f);
    final IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);

    public static boolean click = false;
    static int teleportTicks;
    int posRotIncrement = 0;
    public static int balance = 0;

    Vec3 targetPos, pos;

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();

        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof LegitClickTimingEvent && click) {
            Clicks.click(target);
            click = false;
        }

        if (event instanceof TickEvent e) {
            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            if (target.hurtTime > maxTargetHurtTime.getValue()) return;

            if (teleportTicks == 0) {
                AxisAlignedBB box = RotUtils.getHitBox(target, 100, 100);

                float yaw = RotUtils.getBestRotation(box).getYaw();
                SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

                pos = target.getPositionVector();
                targetPos = target.getNewPosition();
                posRotIncrement = target.newPosRotationIncrements;

                for (int i = 0; i < maxTicks.getValue(); i++) {
                    updateCashedIncrementPos();

                    AxisAlignedBB targetBox = getPredictBB(target, target.getNPosition(), pos);

                    double distance = 3 + additionalDistance.getValue();
                    boolean skip = DistanceUtils.getDistance(simulatedPlayer, targetBox) > distance;

                    if (skip) {
                        simulatedPlayer.tick();
                        continue;
                    }

                    balance = teleportTicks = i;
                    balance += additionalTicks.getValue();
                    break;
                }
            }

            if (balance == 0 && teleportTicks > 0) {
                for (int i = 0; i < teleportTicks; i++) {
                    try {
                        mc.runTick();
                        balance++;
                        if (i == teleportTicks - 1) {
                            click = true;
                        }
                    } catch (Exception ignored) {}
                }
                teleportTicks = 0;
            }
        }
    }

    private void updateCashedIncrementPos() {
        if (posRotIncrement > 0) {
            pos = pos.add(targetPos.subtract(pos).divine(posRotIncrement));
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
        return LagRange.teleportTicks > 0 || LagRange.balance > 0;
    }
}
