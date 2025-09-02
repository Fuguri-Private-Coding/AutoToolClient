package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "AutoTool", category = Category.PLAYER, description = "Автоматически берет инструмент в руку в зависимости от блока.")
public class AutoTool extends Module {

    DoubleSlider switchDelayTick = new DoubleSlider("SwitchDelayTick", this, 0,20,0,1);
    boolean flag;

    int delay;

    StopWatch timer = new StopWatch();

    @Override
    public void onDisable() {
        super.onDisable();
        switchBack();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.objectMouseOver == null)
            return;

        final MovingObjectPosition mouse = mc.objectMouseOver;

        if (event instanceof LegitClickTimingEvent && timer.reachedMS(delay * 50L)) {
            if (mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mouse.getBlockPos() == null || !mc.gameSettings.keyBindAttack.isKeyDown()) {
                if (flag) {
                    flag = false;
                    switchBack();
                    timer.reset();
                }
                return;
            }

            flag = true;
            final BlockPos block = mouse.getBlockPos();
            mc.thePlayer.inventory.currentItem = getBestSlot(mc.theWorld.getBlockState(block).getBlock());
            delay = switchDelayTick.getRandomizedIntValue();
            timer.reset();
        }
    }

    int getBestSlot(Block block) {
        float bestEff = 1f;
        int bestSlot = mc.thePlayer.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null) {
                continue;
            }

            final float eff = item.getStrVsBlock(block);

            if (eff <= bestEff) {
                continue;
            }

            bestEff = eff;
            bestSlot = i;
        }

        return bestSlot;
    }

    void switchBack() {
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
    }
}
