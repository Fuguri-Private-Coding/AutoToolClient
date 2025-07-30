package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", category = Category.PLAYER, description = "Позволяет быстрее ставить блоки.")
public class FastPlace extends Module {

    IntegerSetting minCps = new IntegerSetting("MinCps", this, 1, 40, 7) {
        @Override
        public int getValue() {
            if (maxCps.value < value) { value = maxCps.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxCps = new IntegerSetting("MaxCps", this, 0, 40, 11) {
        @Override
        public int getValue() {
            if (minCps.value > value) { value = minCps.value; }
            return super.getValue();
        }
    };

    StopWatch stopWatch;
    long delay;

    public FastPlace() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.currentScreen != null) return;
        boolean needClick = Mouse.isButtonDown(1) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
        if (event instanceof TickEvent) {
            if (stopWatch.reachedMS(delay)) {
                if (needClick && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                    mc.thePlayer.swingItem();
                    delay = (long) (1000D / RandomUtils.nextDouble(minCps.getValue(), maxCps.getValue()));
                    stopWatch.reset();
                }
            }
        }
        if (event instanceof ClickEvent e) {
            if (needClick && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }
}
