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
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT, description = "Автоматически кликает за вас.")
public class AutoClicker extends Module {

    MultiMode buttons = new MultiMode("Buttons", this)
        .addModes("Left")
        ;

    DoubleSlider leftCPS = new DoubleSlider("LeftCPS", this, () -> buttons.get("Left"), 0, 40, 20, 1);
    CheckBox breakBlocks = new CheckBox("BreakBlocks", this, () -> buttons.get("Left"), true);

    long leftDelay;

    final StopWatch leftStopWatch = new StopWatch();

    @Override
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (buttons.get("Left")) {
                if (!Mouse.isButtonDown(0) || mc.thePlayer.isUsingItem() || Mouse.isButtonDown(1) || mc.currentScreen != null) return;
                if (RayCastUtils.rayCast(3, 4.5, new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)).typeOfHit == RayTrace.RayType.BLOCK && breakBlocks.isToggled()) return;

                if (leftStopWatch.reachedMS(leftDelay)) {
                    leftDelay = getLeftDelay();
                    Clicks.addClick();
                    leftStopWatch.reset();
                }
            }
        }

        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
            if (!Mouse.isButtonDown(0) || mc.thePlayer.isUsingItem() || Mouse.isButtonDown(1) || mc.currentScreen != null) return;
            if (RayCastUtils.rayCast(3, 4.5, new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)).typeOfHit == RayTrace.RayType.BLOCK && breakBlocks.isToggled()) return;

            e.cancel();
        }
    }

    private long getLeftDelay() {
        return (long) (1000D / leftCPS.getRandomizedDoubleValue());
    }
}