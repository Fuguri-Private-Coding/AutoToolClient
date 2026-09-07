package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.PacketDirection;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
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
    final CheckBox useBestRotationForPredict = new CheckBox("UseBestRotationForPredict", this, true);

    final CheckBox cancelPackets = new CheckBox("CancelPackets", this, false);
    final Mode cancelMode = new Mode("CancelMode", this, cancelPackets::isToggled)
            .addModes("Lag", "Both")
            .setMode("Lag")
            ;

    final Mode snapConditions = new Mode("SnapConditions", this)
        .addModes("ToClick", "ToTeleport")
        .setMode("ToClick")
        ;

    public static boolean teleporting = false, click = false;
    public static int balance = 0;
    int teleportTicks;

    List<Packet> packets = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();

        if (balance == 0) {
            packets.forEach(PacketUtils::sendPacket);
            packets.clear();
        }

        if (event instanceof PacketEvent e && needAddPackets()) {
            Packet packet = e.getPacket();

            if (cancelPackets.isToggled() && e.getDirection() == PacketDirection.OUTGOING) {
                e.cancel();
                packets.add(packet);
            }
        }

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

            teleportTicks = 0;

            Vec3 position = target.getServerPosition().divine(32.0D)
                .subtract(target.getPositionVector());

            AxisAlignedBB box = target.getExpandedBoundingBox()
                    .offset(position);

            if (target.hurtTime > maxTargetHurtTime.getValue() || DistanceUtils.getDistance(box) < 3.0) return;

            float yaw = useBestRotationForPredict.isToggled() ? RotUtils.getBestRotation(box).getYaw() : mc.thePlayer.rotationYaw;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

            BackTrack backTrack = Modules.getModule(BackTrack.class);

            for (int i = 0; i < maxTicks.getValue(); i++) {
                double distance = DistanceUtils.getDistance(simulatedPlayer.getPosEyes(), box);

                boolean skip = distance > 3.0D;
                boolean backTrackSkip = distance > backTrack.distanceToCancelHits.getValue() && backTrack.isToggled();

                if (skip || backTrackSkip) {
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
            if (balance > 0) click = true;
            teleporting = false;
        }
    }

    private boolean needAddPackets() {
        return switch (cancelMode.getMode()) {
            case "Lag" -> balance > 0 && !teleporting;
            case "Both" -> teleporting || balance > 0;
            default -> false;
        };
    }

    public static boolean needSnap() {
        return switch (Modules.getModule(TimerRange.class).snapConditions.getMode()) {
            case "ToTeleport" -> teleporting || balance > 0;
            case "ToClick" -> click;
            default -> false;
        };
    }
}