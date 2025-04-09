package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.Render3DEvent;
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

@ModuleInfo(name = "TimerRange", category = Category.COMBAT)
public class TimerRange extends Module {

    IntegerSetting limitTicks = new IntegerSetting("Ticks", this, 1,20,2);
    IntegerSetting maxTargetHurtTime = new IntegerSetting("TargetHurtTime", this, 0,10,0);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0,1,1,0.1f);

    boolean teleporting = false;
    boolean click;
    int balance;

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null) {
            return;
        }
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

            if (target == null || target.hurtTime > maxTargetHurtTime.getValue()) {
                return;
            }

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);
            int teleportTicks = 0;

            for (int i = 0; i < limitTicks.getValue(); i++) {
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3,
                        15,
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
                    if (RayCastUtils.raycastEntity(3, ignored -> true) == target) {
                        click = true;
                        break;
                    }
                } catch (Exception ignored) { }
            }
            teleporting = false;
        }
        if (event instanceof Render3DEvent) {
            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

            for (int i = 0; i < limitTicks.getValue(); i++) {
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3,
                        12,
                        new Rotation(Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch()));

                if (mouse == null || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    simulatedPlayer.tick();
                    continue;
                }

                break;
            }
            double x = mc.thePlayer.lastTickPosX + (simulatedPlayer.getPos().xCoord - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = mc.thePlayer.lastTickPosY + (simulatedPlayer.getPos().yCoord - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = mc.thePlayer.lastTickPosZ + (simulatedPlayer.getPos().zCoord - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

            //mc.renderManager.doRenderEntity(mc.thePlayer, x,y,z,mc.thePlayer.rotationYawHead, mc.timer.renderPartialTicks, true);

        }
        if (event instanceof RunGameLoopEvent && balance > 0) mc.timer.renderPartialTicks = partialTicks.getValue();
    }

    @Override
    public boolean handleEvents() {
        return !teleporting && isToggled();
    }
}