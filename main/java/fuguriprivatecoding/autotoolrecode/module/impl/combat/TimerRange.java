package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.RayTrace;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    final FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);
    final IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);

    final Mode snapConditions = new Mode("SnapConditions", this)
        .addModes("ToClick", "ToTeleport")
        .setMode("ToClick")
        ;

    public static boolean teleporting = false, click = false;
    public static int balance = 0;
    int teleportTicks;

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();

        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof BestClickTimingEvent && click) {
            Clicks.click(target);
            click = false;
        }

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                if (target != null && target.hurtTime > 0) target.hurtTime--;
                e.cancel();
                balance--;
                return;
            }

            boolean canSnap = KillAura.canSnapTeleport();

            AxisAlignedBB box = target.getExpandedBoundingBox();

            float yaw = mc.thePlayer.rotationYaw;
            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

            teleportTicks = 0;

            if (target.hurtTime > maxTargetHurtTime.getValue() || (canSnap && DistanceUtils.getDistance(box) < 3.0)) return;

            Vec3 targetPosition = target.getServerPosition().divine(32.0D).subtract(target.getPositionVector());
            AxisAlignedBB targetBox = box.offset(targetPosition);

            for (int i = 0; i < maxTicks.getValue(); i++) {
                boolean skip = DistanceUtils.getDistance(simulatedPlayer.getPosEyes(), targetBox) > 3.0D;

                if (!canSnap) {
                    RayTrace hit = RayCastUtils.rayCast(mc.thePlayer.getPositionEyes(1f), 12, 6, mc.thePlayer.getRotation(), 1f);

                    if (hit == null || hit.entityHit != target)
                        break;

                    skip = DistanceUtils.getDistance(simulatedPlayer.getPosEyes(), hit.hitVec) > 3.0D;
                }

                if (skip) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i;
                break;
            }

            if (teleportTicks == 0)
                return;

            teleporting = true;
            balance = PlayerUtils.teleport(teleportTicks, additionalTicks.getValue());
            if (balance > 0) click = true;
            teleporting = false;
        }
    }

    public static boolean needSnap() {
        return switch (Modules.getModule(TimerRange.class).snapConditions.getMode()) {
            case "ToTeleport" -> teleporting || balance > 0;
            case "ToClick" -> click;
            default -> false;
        };
    }
}