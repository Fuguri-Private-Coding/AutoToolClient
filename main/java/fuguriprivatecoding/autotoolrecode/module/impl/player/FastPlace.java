package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.BestClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RayTrace;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "FastPlace", category = Category.PLAYER, description = "Позволяет быстрее ставить блоки.")
public class FastPlace extends Module {

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1,80,20,1f);
    private final DoubleSlider CPSUpdateDelay = new DoubleSlider("CPSUpdateDelay", this, 0, 20, 5, 1);
    CheckBox oneClick = new CheckBox("OneClick", this, true);

    private final StopWatch clickTimer = new StopWatch();
    private long delay;

    private double currentCps;
    private int currentCpsUpdateDelay;
    private int clicks = 1;

    @Override
    public void onEvent(Event event) {
        if (Modules.getInstance().getModule(Scaffold.class).isToggled() || mc.currentScreen != null) return;

        if (event instanceof RunGameLoopEvent) {
            if (clickTimer.reachedMS(delay)) {
                updateDelay();
                clickTimer.reset();

                clicks++;
            }
        }

        if (event instanceof TickEvent) {
            if (currentCpsUpdateDelay > 0) currentCpsUpdateDelay--;
        }

        if (event instanceof BestClickTimingEvent) {
            int iters = clicks;
            clicks = 0;

            if (needClick()) for (int i = 0; i < iters; i++) mc.rightClickMouse();
        }

        if (event instanceof ClickEvent e && needClick()) {
            if (e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }

    private boolean needClick() {
        RayTrace hit = mc.rayTrace;

        boolean oneClick = true;

        ItemStack heldStack = mc.thePlayer.getHeldItem();

        if (heldStack != null && heldStack.getItem() instanceof ItemBlock itemblock) {
            if (!itemblock.canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit, mc.thePlayer, heldStack)) {
                oneClick = false;
            }
        }

        boolean item = heldStack != null && heldStack.getItem() instanceof ItemBlock;
        boolean hitBlock = hit.typeOfHit == RayTrace.RayType.BLOCK;

//        boolean oneClickCheck = !this.oneClick.isToggled() || oneClick;

        return Mouse.isButtonDown(1) && item && hitBlock;
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
