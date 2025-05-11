package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.predict.SimulatedPlayer;
import me.hackclient.utils.raytrace.RayTraceUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

import java.io.IOException;

@ModuleInfo(name = "TimerRangeV2", category = Category.COMBAT)
public class TimerRangeV2 extends Module {

    private final IntegerSetting minTicks = new IntegerSetting("MinTicks", this, 1, 10, 3);
    private final IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 1, 10, 3);
    private final IntegerSetting hurtTime = new IntegerSetting("HurtTime", this, 0, 10, 0);

    private int balance;
    private boolean teleporting;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = 0;
        }
        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

            if (target == null || target.hurtTime > hurtTime.getValue()) {
                return;
            }

            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit == target) {
                return;
            }

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);
            int predictedTicks = 0;

            for (int tick = 1; tick <= maxTicks.getValue(); tick++) {
                simulatedPlayer.tick();
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3d,
                        4.5d,
                        Rotation.getServerRotation()
                );

                if (mouse != null && mouse.entityHit == target) {
                    predictedTicks = tick;
                    break;
                }
            }

            if (predictedTicks == 0 || predictedTicks < minTicks.getValue()) {
                return;
            }

            teleporting = true;
            for (int i = 0; i < predictedTicks; i++) {
                try {
                    mc.runTick();
                } catch (IOException _) {
                }
            }
            teleporting = false;

            balance = predictedTicks;
            mc.clickMouse();
        }
    }
}
