package fuguriprivatecoding.autotool.module.impl.legit;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.utils.math.RandomUtils;
import fuguriprivatecoding.autotool.utils.rotation.RayCastUtils;
import fuguriprivatecoding.autotool.utils.rotation.Rot;
import fuguriprivatecoding.autotool.utils.timer.StopWatch;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT)
public class AutoClicker extends Module {

    final StopWatch stopWatch;
    int delay;

    final FloatSetting minCps = new FloatSetting("MinCps", this, 0.0f, 25.0f, 13.0f, 0.1f) {
        @Override
        public float getValue() {
            if (maxCps.value < value) { value = maxCps.value; }
            return super.getValue();
        }
    };

    final FloatSetting maxCps = new FloatSetting("MaxCps", this, 0.0f, 25.0f, 17.0f, 0.1f) {
        @Override
        public float getValue() {
            if (minCps.value > value) { value = minCps.value; }
            return super.getValue();
        }
    };

    final CheckBox allowBreakBlock = new CheckBox("AllowBreakBlock", this, true);

    public AutoClicker() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (!Mouse.isButtonDown(0)) return;

            if (mc.thePlayer.isUsingItem()) return;

            if (mc.currentScreen != null) return;

            if (allowBreakBlock.isToggled() && RayCastUtils.rayCast(Client.INST.getCombatManager().getEntityReach(), Client.INST.getCombatManager().getBlockReach(), Rot.getServerRotation()).typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return;

            if (stopWatch.reachedMS(delay)) {
                stopWatch.reset();
                Client.INST.getClickManager().addClick();
                delay = Math.round(1000f / RandomUtils.nextFloat(minCps.getValue(), maxCps.getValue()));
            }
        }
    }
}
