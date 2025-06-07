package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.FakeTickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

import java.io.IOException;

@ModuleInfo(name = "TestTimerRange", category = Category.COMBAT)
public class TestTimerRange extends Module {

    IntegerSetting minTicks = new IntegerSetting("MinTicks", this, 0, 10, 2) {
        @Override
        public int getValue() {
            if (maxTicks.value < value) { value = maxTicks.value; }
            return value;
        }
    };

    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 1, 10, 3) {
        @Override
        public int getValue() {
            if (minTicks.value > value) { value = minTicks.value; }
            return value;
        }
    };

    IntegerSetting maxTargetHurtTime = new IntegerSetting("TargetHurtTime", this, 0,10,0);

    boolean teleporting;
    int balance = 0;

    @EventTarget
    public void onEvent(Event event) {
        if (teleporting) {
            return;
        }
        if (event instanceof TickEvent e) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (target == null || target.hurtTime > maxTargetHurtTime.getValue()) {
                return;
            }

            MovingObjectPosition movingObjectPosition = RayCastUtils.rayCast(3, 4.5, Rot.getServerRotation());
            if (movingObjectPosition != null && movingObjectPosition.entityHit instanceof EntityLivingBase) {
                return;
            }

            SimulatedPlayer predictPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

            int ticks = 0;
            for (int i = 1; i <= maxTicks.getValue(); i++) {
                predictPlayer.tick();
                MovingObjectPosition mouse = RayCastUtils.rayCast(predictPlayer.getPosEyes(), 3, 4.5, Rot.getServerRotation());
                if (mouse != null && mouse.entityHit == target) {
                    ticks = i;
                    break;
                }
            }

            if (ticks < minTicks.getValue()) {
                return;
            }

            teleporting = true;
            for (int i = 0; i < ticks; i++) {
                try {
                    mc.runTick();
                } catch (IOException ex) {
                }
            }
            teleporting = false;

            mc.clickMouse();
            balance = ticks;
            mc.timer.timerSpeed = 0f;
        }
        if (event instanceof FakeTickEvent) {
            if (balance <= 0) {
                balance = 0;
                mc.timer.timerSpeed = 1;
            } else {
                balance--;
            }
        }
    }
}