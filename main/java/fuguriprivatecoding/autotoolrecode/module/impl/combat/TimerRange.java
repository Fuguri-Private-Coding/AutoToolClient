package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PostTickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    final FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);
    final IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);
    final IntegerSetting idealChance = new IntegerSetting("IdealChance", this, 0, 100, 50);
    final IntegerSetting tickDelay = new IntegerSetting("TickDelay", this, 0, 20, 0);

    final Mode snapConditions = new Mode("SnapConditions", this)
        .addModes("ToClick", "ToTeleport")
        .setMode("ToClick")
        ;

    public static boolean teleporting = false, click = false;
    public static int balance = 0;
    int teleportTicks;

    List<PredictProcess> predictList = new CopyOnWriteArrayList<>();

    StopWatch timer = new StopWatch();

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();

        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof BestClickTimingEvent && click) {
            if (mc.rayTrace.entityHit == target)  {
                mc.clickMouse();
            }
            click = false;
        }

        if (event instanceof MoveEvent && !teleporting) {
            if (balance > 0) {
                return;
            }

            predictList.clear();

            if (target == null)
                return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(
                mc.thePlayer.movementInput,
                1,
                mc.thePlayer.rotationYaw
            );

            for (int times = 0; times <= maxTicks.getValue(); times++) {
                predictList.add(new PredictProcess(times, simulatedPlayer));
                simulatedPlayer.tick();
            }
        }

        if (event instanceof PostTickEvent) {
            if (balance > 0) {
                if (target != null && target.hurtTime > 0) {
                    target.hurtTime--;
                }
            }
        }

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            teleportTicks = 0;

            if (target == null) {
                return;
            }

            Vec3 pos = target.getServerPosition().divine(32.0D).subtract(target.getPositionVector());

            AxisAlignedBB box = target.getExpandedBoundingBox()
                .offset(pos);

            if (target.hurtTime > maxTargetHurtTime.getValue() || DistanceUtils.getDistance(box) <= 3.0 || !timer.reachedMS(tickDelay.getValue() * 50L) || predictList.isEmpty())
                return;

            for (PredictProcess process : predictList.reversed()) {
                double distance = DistanceUtils.getDistance(process.player.getPosEyes(), box);

                double factor = idealChance.getValue() / 100D;

                if (distance > 3.0D || distance < 2.0D + factor)
                    continue;

                teleportTicks = process.tick;
                break;
            }

            if (teleportTicks == 0)
                return;

            teleporting = true;
            balance = PlayerUtils.tick(teleportTicks, additionalTicks.getValue());

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
        TimerRange timerRange = Modules.getInstance().getModule(TimerRange.class);
        return switch (timerRange.snapConditions.getMode()) {
            case "ToTeleport" -> teleporting || balance > 0;
            case "ToClick" -> click;
            default -> false;
        };
    }

    public record PredictProcess(int tick, SimulatedPlayer player) {}
}