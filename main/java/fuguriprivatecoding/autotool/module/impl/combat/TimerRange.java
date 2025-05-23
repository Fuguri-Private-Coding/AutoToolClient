package fuguriprivatecoding.autotool.module.impl.combat;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.LegitClickTimingEvent;
import fuguriprivatecoding.autotool.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotool.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotool.utils.raytrace.RayTraceUtils;
import fuguriprivatecoding.autotool.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotool.utils.rotation.Rot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT)
public class TimerRange extends Module {

    IntegerSetting minTicks = new IntegerSetting("MinTicks", this, 0,20,2) {
        @Override
        public int getValue() {
            if (maxTicks.value < value) { value = maxTicks.value; }
            return value;
        }
    };
    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0,20,2){
        @Override
        public int getValue() {
            if (minTicks.value > value) { value = minTicks.value; }
            return value;
        }
    };

    IntegerSetting maxTargetHurtTime = new IntegerSetting("TargetHurtTime", this, 0,10,0);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, 0,1,1,0.1f);

    boolean teleporting, click = false;
    int teleportTicks = 0;
    public int balance;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof LegitClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }
        if (event instanceof TickEvent tickEvent) {
            if (teleporting) return;
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (balance > 0) {
                tickEvent.setCanceled(true);
                balance--;
                return;
            }

            if (target == null || target.hurtTime > maxTargetHurtTime.getValue()) return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

            teleportTicks = 0;
            for (int i = 0; i < maxTicks.getValue(); i++) {
                MovingObjectPosition mouse = RayTraceUtils.rayTrace(
                        simulatedPlayer.getPosEyes(),
                        3,
                        0,
                        new Rot(Rot.getServerRotation().getYaw(), Rot.getServerRotation().getPitch()));

                if (mouse == null || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    simulatedPlayer.tick();
                } else {
                    teleportTicks = i;
                    break;
                }
            }

            if (teleportTicks < minTicks.getValue()) return;

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (RayCastUtils.rayCast(3.0, 0, Rot.getServerRotation()).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
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
}