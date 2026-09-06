package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT, description = "Автоматически кликает за вас.")
public class AutoClicker extends Module {

    private final DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 40, 16, 1);
    private final DoubleSlider CPSUpdateDelay = new DoubleSlider("CPSUpdateDelay", this, 0, 20, 5, 1);

    MultiMode stopClickingWhen = new MultiMode("StopClickingWhen", this)
        .addModes("UsingItem", "BreakBlock")
        ;

    private final StopWatch clickTimer = new StopWatch();
    private long delay;

    private double currentCps = 10;
    private int currentCpsUpdateDelay = 1;

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (clickTimer.reachedMS(delay)) {
                updateDelay();
                clickTimer.reset();

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

    private void updateDelay() {
        updateCps();
        delay = Math.round(1000 / currentCps);
    }

    private void updateCps() {
        if (currentCpsUpdateDelay == 0) {
            currentCpsUpdateDelay = CPSUpdateDelay.getRandomizedIntValue();
            currentCps = CPS.getRandomizedDoubleValue();
        }
    }
}