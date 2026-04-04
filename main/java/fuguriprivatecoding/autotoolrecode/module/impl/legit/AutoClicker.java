package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT, description = "Автоматически кликает за вас.")
public class AutoClicker extends Module {

    DoubleSlider leftCPS = new DoubleSlider("LeftCPS", this, 0, 40, 20, 1);
    CheckBox breakBlocks = new CheckBox("BreakBlocks", this, true);

    long leftDelay;

    final StopWatch leftStopWatch = new StopWatch();

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (leftStopWatch.reachedMS(leftDelay)) {
                leftDelay = getLeftDelay();
                leftStopWatch.reset();

                if (shouldStopClicking() || shouldBreak()) return;

                Clicks.addClick();
            }
        }

        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (shouldStopClicking() || shouldBreak()) return;
            e.cancel();
        }
    }

    private boolean shouldStopClicking() {
        return !Mouse.isButtonDown(0) || mc.thePlayer.isUsingItem() || Mouse.isButtonDown(1) || mc.currentScreen != null;
    }

    private boolean shouldBreak() {
        return mc.objectMouseOver.typeOfHit == RayTrace.RayType.BLOCK && breakBlocks.isToggled();
    }

    private long getLeftDelay() {
        return (long) (1000D / leftCPS.getRandomizedDoubleValue());
    }
}