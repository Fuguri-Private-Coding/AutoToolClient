package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT, description = "Автоматически кликает за вас.")
public class AutoClicker extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 0, 40, 20, 1);

    Mode randomizeMode = new Mode("RandomizeMode", this)
        .addModes("None", "Gaussian")
        .setMode("Gaussian");

    BooleanSupplier gaussianMode = () -> randomizeMode.is("Gaussian");

    DoubleSlider cpsLimiter = new DoubleSlider("CPSLimiter", this, gaussianMode, 0, 40, 20, 1);

    FloatSetting consistency = new FloatSetting("Consistency", this, gaussianMode, 0, 2, 0.2f, 0.01f);
    FloatSetting instability = new FloatSetting("Instability", this, gaussianMode, 0, 2, 0.2f, 0.01f);

    MultiMode stopClickingWhen = new MultiMode("StopClickingWhen", this)
        .addModes("UsingItem", "BreakBlock")
        ;

    long leftDelay;

    final StopWatch leftStopWatch = new StopWatch();

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (leftStopWatch.reachedMS(leftDelay)) {
                leftDelay = getLeftDelay();
                leftStopWatch.reset();

                if (shouldStopClicking()) return;

                Clicks.addClick();
            }
        }

        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (shouldStopClicking()) return;
            e.cancel();
        }
    }

    private boolean shouldStopClicking() {
        boolean usingItem = stopClickingWhen.get("UsingItem") && (mc.thePlayer.isUsingItem() || Mouse.isButtonDown(1));
        boolean breakBlock = stopClickingWhen.get("BreakBlock") && mc.rayTrace.typeOfHit == RayTrace.RayType.BLOCK;

        return !Mouse.isButtonDown(0) || usingItem || breakBlock || mc.currentScreen != null;
    }

    private long getLeftDelay() {
        double baseDelay = 1000D / CPS.getRandomizedDoubleValue();

        if (randomizeMode.is("Gaussian")) {
            double minDelay = 1000D / cpsLimiter.getMaxValue();
            double maxDelay = 1000D / cpsLimiter.getMinValue();

            double consistency = this.consistency.getValue();
            double instability = this.instability.getValue();

            double gaussian = RandomUtils.random.nextGaussian() * consistency;
            double noise = (RandomUtils.random.nextDouble() - 0.5) * instability;

            double delay = baseDelay * (1 + gaussian + noise);

            return (long) Math.clamp(delay, minDelay, maxDelay);
        }

        return (long) baseDelay;
    }
}