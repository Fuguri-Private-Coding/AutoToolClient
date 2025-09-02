package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", category = Category.PLAYER, description = "Позволяет быстрее ставить блоки.")
public class FastPlace extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1,80,20,1f);

    StopWatch stopWatch;
    long delay;

    public FastPlace() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
        if (mc.currentScreen != null) return;
        boolean needClick = Mouse.isButtonDown(1) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
        if (event instanceof TickEvent) {
            if (stopWatch.reachedMS(delay)) {
                if (needClick && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                    mc.thePlayer.swingItem();
                    mc.getItemRenderer().resetEquippedProgress();
                    delay = (long) (1000D / CPS.getRandomizedIntValue());
                    stopWatch.reset();
                }
            }
        }
        if (event instanceof ClickEvent e) {
            if (needClick && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }
}
