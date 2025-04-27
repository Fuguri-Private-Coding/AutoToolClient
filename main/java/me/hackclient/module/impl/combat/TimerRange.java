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
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT)
public class TimerRange extends Module {

    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1,20,2);
    IntegerSetting maxTargetHurtTime = new IntegerSetting("TargetHurtTime", this, 0,10,0);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0,1,1,0.1f);

    boolean teleporting = false;
    int teleportTicks = 0;
    boolean click;
    public int balance;

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null) return;
        if (event instanceof LegitClickTimingEvent && click) {
            click = false;
            mc.clickMouse();
        }
        if (event instanceof TickEvent tickEvent) {
            if (teleporting) return;

            EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }

            if (target == null || target.hurtTime > maxTargetHurtTime.getValue()) return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

            teleportTicks = 0;
            for (int i = 0; i < limitTicks.getValue(); i++) {
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3,
                        0,
                        new Rotation(Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch()));

                if (mouse == null || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    simulatedPlayer.tick();
                } else {
                    teleportTicks = i;
                    break;
                }
            }

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (RayCastUtils.raycastEntity(3, ignored -> true) == target) {
                        click = true;
                        break;
                    }
                } catch (Exception ignored) { }
            }
            teleporting = false;
        }
        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }
    }

    @Override
    public boolean handleEvents() {
        return isToggled();
    }
}