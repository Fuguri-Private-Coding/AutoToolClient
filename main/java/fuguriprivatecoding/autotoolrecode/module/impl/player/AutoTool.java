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
import net.minecraft.util.RayTrace;

@ModuleInfo(name = "AutoTool", category = Category.PLAYER, description = "Автоматически берет инструмент в руку в зависимости от блока.")
public class AutoTool extends Module {

    DoubleSlider switchDelayTick = new DoubleSlider("SwitchDelayTick", this, 0,20,0,1);
    DoubleSlider backSwitchDelayTick = new DoubleSlider("BackSwitchDelayTick", this, 0,20,0,1);

    boolean flag, switched;

    StopWatch switchTimer = new StopWatch();
    StopWatch backSwitchTimer = new StopWatch();

    @Override
    public void onDisable() {
        super.onDisable();
        switchBack();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof LegitClickTimingEvent) {
            flag = shouldSwitch(mc.objectMouseOver);

            long switchDelay = switchDelayTick.getRandomizedIntValue() * 50L;

            if (switchTimer.reachedMS(switchDelay)) {
                if (flag && !switched) {
                    switched = true;
                    switchTool();
                }
            }

            if (switched) {
                long backSwitchDelay = backSwitchDelayTick.getRandomizedIntValue() * 50L;

                if (!flag) {
                    if (backSwitchTimer.reachedMS(backSwitchDelay)) switchBack();
                } else {
                    backSwitchTimer.reset();
                    switchTool();
                }
            }

            if (!flag) {
                switchTimer.reset();
            }

        }
    }

    boolean shouldSwitch(RayTrace mouse) {
        return mouse.typeOfHit == RayTrace.RayType.BLOCK && mouse.getBlockPos() != null && mc.gameSettings.keyBindAttack.isKeyDown();
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

    void switchTool() {
        final BlockPos block = mc.objectMouseOver.getBlockPos();
        mc.thePlayer.inventory.currentItem = getBestSlot(mc.theWorld.getBlockState(block).getBlock());
    }

    void switchBack() {
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
        switched = false;
    }
}
