package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.connection.Ping;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;

import java.io.IOException;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT)
public class TimerRange extends Module {

    FloatSetting startDistance = new FloatSetting("Distance", this, 3, 6, 3.5f, 0.1f);
    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1,10,2);
    final IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this,0, 10, 10);
    BooleanSetting debug = new BooleanSetting("Debug", this, true);

    ModeSetting freezeMode = new ModeSetting(
            "FreezeAnimation",
            this,
            "TimeManipulation",
            new String[] {
                    "TimerRangeV2",
                    "TimeManipulation",
            }
    );

    KillAura killAura;
    double balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (killAura == null) killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
        if (event instanceof TickEvent tickEvent) {
            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }
            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();
            if (target != null && mc.thePlayer.getBps(false) > 0 && killAura.isToggled() && mc.thePlayer.moveForward > 0.6) {
                double distance = DistanceUtils.getDistanceToEntity(target);
                while (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) != target
                        && RayCastUtils.raycastEntity(startDistance.getValue(), Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target
                        && target.hurtTime <= maxTargetHurtTime.getValue()) {
                    try {
                        mc.runTick();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    balance++;
                    if (debug.isToggled()) ClientUtils.chatLog(String.format("%.1f, %.3f", balance, distance));
                    if (mc.thePlayer.isCollidedHorizontally) {
                        if (debug.isToggled()) ClientUtils.chatLog("Stopped due PlayerCollidedHorizontally");
                        break;
                    }
                    if (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target) {
                        Client.INSTANCE.getClickManager().addClick();
                        if (debug.isToggled()) ClientUtils.chatLog("Clicked due RayCast");
                        break;
                    }
                    if (balance >= limitTicks.getValue()) {
                        Client.INSTANCE.getClickManager().addClick();
                        if (debug.isToggled()) ClientUtils.chatLog("Clicked due LimitTick");
                        break;
                    }
                }
            }
        }

        if (balance > 0 && event instanceof RunGameLoopEvent) {
            switch (freezeMode.getMode()) {
                case "TimerRangeV2" -> mc.timer.renderPartialTicks = 0;
                case "TimeManipulation" -> mc.timer.renderPartialTicks = 1;
            }
        }
    }
}