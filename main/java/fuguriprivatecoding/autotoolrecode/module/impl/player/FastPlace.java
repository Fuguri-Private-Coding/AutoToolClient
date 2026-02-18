package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", category = Category.PLAYER, description = "Позволяет быстрее ставить блоки.")
public class FastPlace extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1,80,20,1f);
    CheckBox oneClick = new CheckBox("OneClick", this, true);

    StopWatch timer = new StopWatch();

    long delay;
    int clicks = 0;

    @Override
    public void onEvent(Event event) {
        if (Modules.getModule(Scaffold.class).isToggled() || mc.currentScreen != null) return;

        if (event instanceof RunGameLoopEvent) {
            if (timer.reachedMS(delay)) {
                delay = (long) (1000D / CPS.getRandomizedIntValue());
                clicks++;
                timer.reset();
            }
        }

        if (event instanceof LegitClickTimingEvent) {
            int iters = clicks;
            clicks = 0;

            if (needClick()) for (int i = 0; i < iters; i++) mc.rightClickMouse();
        }

        if (event instanceof ClickEvent e && needClick()) {
            if (e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }

    private boolean needClick() {
        RayTrace hit = mc.objectMouseOver;

        boolean oneClick = true;
        boolean flag = false;

        ItemStack heldStack = mc.thePlayer.getHeldItem();

        if (!flag && heldStack != null && heldStack.getItem() instanceof ItemBlock itemblock) {
            if (!itemblock.canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit, mc.thePlayer, heldStack)) {
                oneClick = false;
            }
        }

        boolean item = mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
        boolean hitBlock = hit.typeOfHit == RayTrace.RayType.BLOCK;

        boolean finas = !this.oneClick.isToggled() || oneClick;

        return Mouse.isButtonDown(1) && finas && item && hitBlock;
    }
}
