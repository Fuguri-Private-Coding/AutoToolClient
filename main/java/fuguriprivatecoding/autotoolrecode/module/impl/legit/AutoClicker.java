package fuguriprivatecoding.autotoolrecode.module.impl.legit;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoClicker", category = Category.LEGIT, description = "Автоматически кликает за вас.")
public class AutoClicker extends Module {

    final StopWatch stopWatch;
    int delay;

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1,80,20,1f);

    final CheckBox allowBreakBlock = new CheckBox("AllowBreakBlock", this, true);

    public AutoClicker() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (!Mouse.isButtonDown(0) || mc.thePlayer.isUsingItem() || Mouse.isButtonDown(1) || mc.currentScreen != null) return;
            if (allowBreakBlock.isToggled() && RayCastUtils.rayCast(3, 4.5, new Rot(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)).typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return;

            if (stopWatch.reachedMS(delay)) {
                stopWatch.reset();
                Client.INST.getClickManager().addClick();
                delay = Math.round(1000f / CPS.getRandomizedIntValue());
            }
        }
    }
}