package me.hackclient.module.impl.legit;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(
    name = "AutoClicker",
    category = Category.LEGIT
)
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

    final BooleanSetting allowBreakBlock = new BooleanSetting("AllowBreakBlock", this, true);

    public AutoClicker() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof RunGameLoopEvent) {
            if (!Mouse.isButtonDown(0)) return;

            if (mc.thePlayer.isUsingItem()) return;

            if (mc.currentScreen != null) return;

            if (allowBreakBlock.isToggled() && RayCastUtils.rayCast(Client.INSTANCE.getCombatManager().getEntityReach(), Client.INSTANCE.getCombatManager().getBlockReach(), Rotation.getServerRotation()).typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return;

            if (stopWatch.reachedMS(delay)) {
                stopWatch.reset();
                Client.INSTANCE.getClickManager().addClick();
                delay = Math.round(1000f / RandomUtils.nextFloat(minCps.getValue(), maxCps.getValue()));
            }
        }
    }
}
