package me.hackclient.module.impl.legit;

import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Mouse;

@ModuleInfo(
    name = "AutoClicker",
    category = Category.LEGIT
)
public class AutoClicker extends Module {

    final StopWatch stopWatch;
    int clicks, delay;

    final FloatSetting minCps = new FloatSetting("MinCps", this, 0.0f, 25.0f, 13.0f, 0.1f);
    final FloatSetting maxCps = new FloatSetting("MaxCps", this, 0.0f, 25.0f, 17.0f, 0.1f);
    final BooleanSetting allowBreakBlock = new BooleanSetting("AllowBreakBlock", this, true);

    public AutoClicker() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof LegitClickTimingEvent) {
            if (mc.currentScreen == null && !mc.thePlayer.isUsingItem()) {
                for (int i = 0; i < clicks; i++) {
                    if (!allowBreakBlock.isToggled() || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                        mc.clickMouse();
                    }
                }
            }
            clicks = 0;
        }
        if (event instanceof RunGameLoopEvent) {
            if (stopWatch.reachedMS(delay) && Mouse.isButtonDown(0)) {
                stopWatch.reset();
                clicks++;
                delay = Math.round(1000f / RandomUtils.nextFloat(minCps.getValue(), maxCps.getValue()));
            }
        }
    }
}
