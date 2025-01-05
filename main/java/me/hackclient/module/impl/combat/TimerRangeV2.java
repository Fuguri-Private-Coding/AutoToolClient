package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;

import java.util.Comparator;

@ModuleInfo(name = "TimerRangeV2", category = Category.COMBAT)
public class TimerRangeV2 extends Module {

    BooleanSetting limitTicks = new BooleanSetting("LimitTeleportTicks", this, true);
    IntegerSetting ticks = new IntegerSetting("Ticks", this, () -> limitTicks.isToggled(), 1,20,2);
    FloatSettings startDistance = new FloatSettings("StartDistance", this, 3f, 6, 3.6f, 0.1f);

    FloatSettings renderPartialTicks = new FloatSettings("RenderPartialTicksAtFreeze", this, 0, 2, 1, 0.1f);

    BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);

    TimerState state = TimerState.NONE;

    EntityLivingBase target;
    KillAura killAura;

    boolean click;
    int balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (killAura == null) {
            killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
        }
        if (event instanceof LegitClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }

        switch (state) {
            case NONE -> {
                target = killAura.getTarget();
                if (!onlyKillAura.isToggled() && target == null) {
                    target = (EntityLivingBase) mc.theWorld.loadedEntityList.parallelStream()
                            .filter(entity -> entity instanceof EntityLivingBase)
                            .filter(entity -> !(entity instanceof EntityPlayerSP))
                            .min(Comparator.comparing(RotationUtils::getFovToEntity))
                            .orElse(null);
                }
                if (target != null) {
                    if (DistanceUtils.getDistanceToEntity(target) < startDistance.getValue()) {
                        state = TimerState.TIMER;
                    }
                }
            }
            case TIMER -> {
                if (event instanceof RunGameLoopEvent) {
                    while (true) {
                        if (target != null
                                && DistanceUtils.getDistanceToEntity(target) < startDistance.getValue()
                                && RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) != target
                                && RayCastUtils.raycastEntity(6, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target
                            && (!limitTicks.isToggled() || balance < ticks.getValue())) {
                            try {
                                mc.runTick();
                                balance++;
                            } catch (Exception ignored) {}
                        } else {
                            if (balance > 0) {
                                state = TimerState.FREEZE;
                                if (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target) {
                                    click = true;
                                }
                            } else {
                                state = TimerState.NONE;
                            }
                            break;
                        }
                    }
                }
            }
            case FREEZE -> {
                if (balance <= 0) {
                    state = TimerState.NONE;
                    return;
                }
                if (event instanceof TickEvent tickEvent) {
                    tickEvent.setCanceled(true);
                    balance--;
                }
                if (event instanceof RunGameLoopEvent) {
                    mc.timer.renderPartialTicks = renderPartialTicks.getValue();
                }
            }
        }
    }

    enum TimerState {
        NONE,
        FREEZE,
        TIMER
    }
}
