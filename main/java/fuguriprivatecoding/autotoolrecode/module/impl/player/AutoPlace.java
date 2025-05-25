package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoPlace", category = Category.PLAYER)
public class AutoPlace extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Legit", "AutoPlace")
            .setMode("Legit")
            ;

    CheckBox needHoldRight = new CheckBox("HoldRight", this, () -> mode.getMode().equalsIgnoreCase("AutoPlace"));

    IntegerSetting minCps = new IntegerSetting("MinCps", this, () -> mode.getMode().equals("Legit"), 1, 40, 7) {
        @Override
        public int getValue() {
            if (maxCps.value < value) { value = maxCps.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxCps = new IntegerSetting("MaxCps", this, () -> mode.getMode().equals("Legit"), 0, 40, 11) {
        @Override
        public int getValue() {
            if (minCps.value > value) { value = minCps.value; }
            return super.getValue();
        }
    };

    StopWatch stopWatch;
    long delay;
    int clicks;
    long lastTime = 0L;

    public AutoPlace() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        super.onEvent(event);
        if (mc.currentScreen != null) return;
        switch (mode.getMode()) {
            case "Legit" -> {
                if (event instanceof RunGameLoopEvent && stopWatch.reachedMS(delay)) {
                    stopWatch.reset();
                    delay = (long) (1000D / RandomUtils.nextDouble(minCps.getValue(), maxCps.getValue()));
                    clicks++;
                }
                boolean needClick = Mouse.isButtonDown(1) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
                if (event instanceof TickEvent) {
                    for (int i = 0; i < clicks; i++) {
                        if (needClick && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec)) {
                            mc.thePlayer.swingItem();
                        }
                    }
                    clicks = 0;
                }
                if (event instanceof ClickEvent e) {
                    if (needClick && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
                }
            }

            case "AutoPlace" -> {
                if (event instanceof DrawBlockHighlightEvent) {
                    if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)) return;

                    if (mc.objectMouseOver == null
                            || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                            || mc.objectMouseOver.sideHit == EnumFacing.UP
                            || mc.objectMouseOver.sideHit == EnumFacing.DOWN) return;

                    if (!needHoldRight.isToggled() || Mouse.isButtonDown(1)) {
                        if (mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock().getMaterial() != Material.air) {
                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec) && System.currentTimeMillis() - lastTime >= 25) {
                                mc.thePlayer.swingItem();
                            }
                        }
                    }
                }
            }
        }
    }
}
