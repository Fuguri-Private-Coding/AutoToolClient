package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.predict.SimulatedPlayer;
import me.hackclient.utils.raytrace.RayTraceUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "TickBase", category = Category.COMBAT)
public class TickBase extends Module {

    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1,20,2);

    boolean teleporting = false;
    boolean click;
    int balance;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (teleporting) return;
        if (event instanceof LegitClickTimingEvent && click) {
            click = false;
            mc.clickMouse();
        }
        if (event instanceof TickEvent tickEvent) {
            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

            if (target == null || target.hurtTime != 0) {
                return;
            }

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);
            int teleportTicks = 0;

            for (int i = 0; i < limitTicks.getValue(); i++) {
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3,
                        8,
                        new Rotation(Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch()));

                if (mouse == null || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i;
                break;
            }

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (RayCastUtils.raycastEntity(3, entity -> true) == target) {
                        click = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            teleporting = false;
        }

        if (event instanceof RunGameLoopEvent) {
            if (balance > 0) {
                mc.timer.renderPartialTicks = 1.0f;
            }
        }
    }
}