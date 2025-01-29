package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
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
import me.hackclient.utils.predict.PlayerInfo;
import me.hackclient.utils.predict.SimulatedPlayer;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

import java.io.IOException;

@ModuleInfo(name = "TimerRangeV2", category = Category.COMBAT)
public class TimerRangeV2 extends Module {

    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1, 10, 2);
    BooleanSetting debug = new BooleanSetting("Debug", this, true);

    ModeSetting freezeMode = new ModeSetting(
            "FreezeAnimation",
            this,
            "TimeManipulation",
            new String[]{
                    "TimerRangeV2",
                    "TimeManipulation",
            }
    );

    KillAura killAura;
    double balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent tickEvent) {
            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }
            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();
            if (target != null && RayCastUtils.raycastEntity(3, entity -> true) != target && RayCastUtils.raycastEntity(6, entity -> true) == target) {
                int finalTeleportTicks = 0;

                PlayerInfo pos = new PlayerInfo(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        mc.thePlayer.motionX,
                        mc.thePlayer.motionY,
                        mc.thePlayer.motionZ
                );
                for (int predictingTick = 1; predictingTick <= limitTicks.getValue(); predictingTick++) {
                    pos = SimulatedPlayer.getPredictedPos(
                            mc.thePlayer.moveForward,
                            mc.thePlayer.moveStrafing,
                            pos.getMotionX(),
                            pos.getMotionY(),
                            pos.getMotionZ(),
                            pos.getPosX(),
                            pos.getPosY(),
                            pos.getPosZ(),
                            mc.thePlayer.movementInput.jump
                    );
//                    ClientUtils.chatLog(String.format("%.2f %.2f %.2f %.2f %.2f %.2f",
//                            pos.getPosX(),
//                            pos.getPosY(),
//                            pos.getPosZ(),
//                            pos.getMotionX(),
//                            pos.getMotionY(),
//                            pos.getMotionZ()) + " " + predictingTick
//                    );
                    Vec3 eyesPos = new Vec3(pos.getPosX(), pos.getPosY() + mc.thePlayer.getEyeHeight(), pos.getPosZ());
                    if (RayCastUtils.raycastEntityFromPos(eyesPos, 3, entity -> true) == target) {
                        finalTeleportTicks = predictingTick;
                        ClientUtils.chatLog("Final predicted ticks " + finalTeleportTicks);
                        break;
                    }
                }

                if (finalTeleportTicks <= 0) {
                    return;
                }


                for (int i = 0; i < finalTeleportTicks; i++) {
                    try {
                        mc.runTick();
                        balance++;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    if (i + 1 == finalTeleportTicks) {
                        Client.INSTANCE.getClickManager().addClick();
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