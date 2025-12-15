package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
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

    StopWatch stopWatch;
    long delay;

    public FastPlace() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        if (Modules.getModule(Scaffold.class).isToggled()) return;
        if (mc.currentScreen != null) return;
        boolean needClick = Mouse.isButtonDown(1) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTrace.RayType.BLOCK && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
        if (event instanceof LegitClickTimingEvent) {
            if (stopWatch.reachedMS(delay) && needClick) {
                mc.rightClickMouse();
                delay = (long) (1000D / CPS.getRandomizedIntValue());
                stopWatch.reset();
            }
        }
        if (event instanceof ClickEvent e) {
            if (needClick && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }
}
