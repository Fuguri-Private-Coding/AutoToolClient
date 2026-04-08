package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.Ping;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import java.awt.*;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    final FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0, 2.5f, 1, 0.1f);
    final IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, 0,5,1);

    final CheckBox onlyWhenPing = new CheckBox("OnlyWhenPing", this, false);

    public static boolean teleporting = false, click = false;
    public static int balance = 0;
    int teleportTicks;

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

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                if (target != null && target.hurtTime > 0) target.hurtTime--;
                e.cancel();
                balance--;
                return;
            }

            AxisAlignedBB box = RotUtils.getHitBox(target, 100, 100).expand(0.1D);

            float yaw = RotUtils.getBestRotation(box).getYaw();
            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

            teleportTicks = 0;

            if ((!Ping.isWorking() && onlyWhenPing.isToggled())
                || target.hurtTime > maxTargetHurtTime.getValue()
            ) return;

            for (int i = 0; i < maxTicks.getValue(); i++) {
                Vec3 pos = RenderUtils.getAbsoluteSmoothPos(target.getPositionVector(), target.getLastPositionVector(), 0f);

                AxisAlignedBB targetBox = getRealBB(target, target.getNPosition(), pos).expand(-0.1D);
                boolean skip = DistanceUtils.getDistance(simulatedPlayer, targetBox) > 3.0D;

                if (skip) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i;
                break;
            }

            if (teleportTicks > 0) {
                teleporting = true;
                balance = PlayerUtils.teleport(teleportTicks, additionalTicks.getValue());
                click = true;
                teleporting = false;
            }
        }
    }

    private AxisAlignedBB getRealBB(EntityLivingBase target, Vec3 newPos, Vec3 pos) {
        double offsetX = BackTrack.working ? pos.xCoord - target.posX : newPos.xCoord - target.posX;
        double offsetY = BackTrack.working ? pos.yCoord - target.posY : newPos.yCoord - target.posY;
        double offsetZ = BackTrack.working ? pos.zCoord - target.posZ : newPos.zCoord - target.posZ;

        return target.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ);
    }

    public static boolean isTeleporting() {
        return click;
    }
}