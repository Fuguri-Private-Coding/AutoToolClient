package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    final FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);
    final IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);
    final CheckBox useBestRotationForPredict = new CheckBox("UseBestRotationForPredict", this, true);
    final CheckBox checkHittableDistance = new CheckBox("CheckHittableDistance", this, true);

    final IntegerSetting tickDelay = new IntegerSetting("TickDelay", this, 0, 20, 0);

    final Mode snapConditions = new Mode("SnapConditions", this)
        .addModes("ToClick", "ToTeleport")
        .setMode("ToClick")
        ;

    public static boolean teleporting = false, click = false;
    public static int balance = 0;
    int teleportTicks;

    StopWatch timer = new StopWatch();

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();

        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof BestClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                if (target != null && target.hurtTime > 0) target.hurtTime--;
                e.cancel();
                balance--;
                return;
            }

            teleportTicks = 0;

            Vec3 pos = target.getServerPosition().divine(32.0D)
                .subtract(target.getPositionVector());

            Vec3 newPos = target.getNPosition().subtract(target.getPositionVector());

            AxisAlignedBB box = target.getExpandedBoundingBox()
                .offset(pos);

            AxisAlignedBB newBox = target.getExpandedBoundingBox()
                .offset(newPos);

            if (target.hurtTime > maxTargetHurtTime.getValue() || DistanceUtils.getDistance(box) < 3.0 || !timer.reachedMS(tickDelay.getValue() * 50L))
                return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(
                mc.thePlayer.movementInput,
                useBestRotationForPredict.isToggled() ? RotUtils.getBestRotation(box).getYaw() : mc.thePlayer.rotationYaw
            );

            BackTrack backTrack = Modules.getModule(BackTrack.class);

            for (int i = 0; i < maxTicks.getValue(); i++) {
                double distance = DistanceUtils.getDistance(simulatedPlayer.getPosEyes(), box);
                double newDistance = DistanceUtils.getDistance(simulatedPlayer.getPosEyes(), newBox);

                boolean skip = distance > 3.0D;
                boolean distanceSkip = newDistance > 6.0D && checkHittableDistance.isToggled();
                boolean backTrackSkip = newDistance > backTrack.distanceToCancelHits.getValue() && backTrack.isToggled();

                if (backTrackSkip || distanceSkip) {
                    teleportTicks = 0;
                    break;
                }

                if (skip) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i + 1;
                break;
            }

            if (teleportTicks == 0)
                return;

            teleporting = true;
            balance = PlayerUtils.teleport(teleportTicks, additionalTicks.getValue());

            if (balance > 0) {
                timer.reset();
                click = true;
            }

            teleporting = false;
        }
    }

    public static boolean isWorking() {
        return click || balance > 0 || teleporting;
    }

    public static boolean needSnap() {
        return switch (Modules.getModule(TimerRange.class).snapConditions.getMode()) {
            case "ToTeleport" -> teleporting || balance > 0;
            case "ToClick" -> click;
            default -> false;
        };
    }
}