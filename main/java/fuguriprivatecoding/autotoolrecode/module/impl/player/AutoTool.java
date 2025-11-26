package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "AutoTool", category = Category.PLAYER, description = "Автоматически берет инструмент в руку в зависимости от блока.")
public class AutoTool extends Module {

    DoubleSlider switchDelayTick = new DoubleSlider("SwitchDelayTick", this, 0,20,0,1);
    DoubleSlider backSwitchDelayTick = new DoubleSlider("BackSwitchDelayTick", this, 0,20,0,1);

    boolean flag;

    boolean switchSlot;
    boolean switchBack;

    StopWatch switchTimer = new StopWatch();
    StopWatch backSwitchTimer = new StopWatch();

    @Override
    public void onDisable() {
        super.onDisable();
        switchBack();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.objectMouseOver == null)
            return;

        final MovingObjectPosition mouse = mc.objectMouseOver;

        if (event instanceof LegitClickTimingEvent) {
            if (mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mouse.getBlockPos() == null || !mc.gameSettings.keyBindAttack.isKeyDown()) {
                if (flag && backSwitchTimer.reachedMS(backSwitchDelayTick.getRandomizedIntValue() * 50L)) {
                    flag = false;
                    switchBack();
                    backSwitchTimer.reset();
                }
                return;
            }

            if (switchTimer.reachedMS(switchDelayTick.getRandomizedIntValue() * 50L)) {
                flag = true;
                backSwitchTimer.reset();
                final BlockPos block = mouse.getBlockPos();
                mc.thePlayer.inventory.currentItem = getBestSlot(mc.theWorld.getBlockState(block).getBlock());
                switchTimer.reset();
            }
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
