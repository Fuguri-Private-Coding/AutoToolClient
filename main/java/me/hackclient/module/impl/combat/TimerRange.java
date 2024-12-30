package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, toggled = true)
public class TimerRange extends Module {

    //ModeSetting predictMode = new ModeSetting("Predict", this, "RayCast", new String[] {"RayCast", "Ticks"});

    FloatSettings startDistance = new FloatSettings("StartDistance", this/*, () -> predictMode.getMode().equalsIgnoreCase("RayCast")*/, 3f, 6, 3.6f, 0.1f);
    IntegerSetting limitTicks = new IntegerSetting("LimitTick", this/*, () -> predictMode.getMode().equalsIgnoreCase("RayCast")*/, 1,10,2);
    IntegerSetting disabledTicks = new IntegerSetting("FlagDelayTicks", this, 0, 100, 10);

    //FloatSettings partialTicks = new FloatSettings("PartialTicks", this, 0f, 2f, 1f, 0.1f);
    ModeSetting freezeMode = new ModeSetting(
            "FreezeAnimation",
            this,
            "TimerRangeV2",
            new String[] {
                    "TimerRangeV2",
                    "TimeManipulation",
                    "PizdecPolniy"
            });

    private KillAura killAura;

    private int flagDelayTicks;
    private float balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (killAura == null) killAura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
        if (killAura.getTarget() == null) {
            balance = 0;
        }

        if (event instanceof TickEvent) {
            if (flagDelayTicks > 0) {
                flagDelayTicks--;
            }
        }

        if (flagDelayTicks > 0) {
            return;
        }

        if (event instanceof PacketEvent packetEvent) {
            if (packetEvent.getPacket() instanceof S08PacketPlayerPosLook) {
                flagDelayTicks = disabledTicks.getValue();
                return;
            }
        }

        if (event instanceof TickEvent tickEvent) {
            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }

            EntityLivingBase target = killAura.getTarget();
            if (target != null && mc.thePlayer.motionX != 0 && mc.thePlayer.motionZ != 0) {
                double distance = DistanceUtils.getDistanceToEntity(target);
                while (distance < startDistance.getValue()
                        && RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) != target
                        && RayCastUtils.raycastEntity(6, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target
                        && mc.thePlayer.hurtTime == 0) {
                    try {
                        mc.runTick();
                        balance++;
                        if (balance >= limitTicks.getValue() || mc.thePlayer.moveForward == 0f)
                            break;
                    } catch (Exception ignored) {
                    }
                }
                if (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target) {
                    killAura.clickManager.clicks++;
                }
            }
        }
        if (event instanceof RunGameLoopEvent) {
            if (balance > 0) {
                switch (freezeMode.getMode()) {
                    case "TimerRangeV2": {
                        mc.timer.renderPartialTicks = 0;
                        break;
                    }
                    case "TimeManipulation": {
                        mc.timer.renderPartialTicks = 1;
                        break;
                    }
                    case "PizdecPolniy": {
                        mc.timer.renderPartialTicks = 2;
                        break;
                    }
                }
            }
        }
    }
}