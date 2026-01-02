package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", category = Category.PLAYER, description = "Позволяет быстрее ставить блоки.")
public class FastPlace extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1,80,20,1f);

    StopWatch timer = new StopWatch();

    long delay;
    boolean click;

    @Override
    public void onEvent(Event event) {
        if (Modules.getModule(Scaffold.class).isToggled() || mc.currentScreen != null) return;

        boolean needClick = needClick();
        if (event instanceof RunGameLoopEvent && needClick) {
            if (timer.reachedMS(delay)) {
                delay = (long) (1000D / CPS.getRandomizedIntValue());
                click = true;
                timer.reset();
            }
        }

        if (event instanceof LegitClickTimingEvent && click) {
            mc.rightClickMouse();
            click = false;
        }

        if (event instanceof ClickEvent e) {
            if (needClick && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }

    private boolean needClick() {
        return Mouse.isButtonDown(1) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTrace.RayType.BLOCK && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
    }
}
