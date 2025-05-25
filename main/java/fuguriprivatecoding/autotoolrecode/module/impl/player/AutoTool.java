package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "AutoTool", category = Category.PLAYER)
public class AutoTool extends Module {

    boolean flag;

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

        if (event instanceof LegitClickTimingEvent) {
            if (mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mouse.getBlockPos() == null || !mc.gameSettings.keyBindAttack.isKeyDown()) {
                if (flag) {
                    flag = false;
                    switchBack();
                }
                return;
            }

            flag = true;
            final BlockPos block = mouse.getBlockPos();
            mc.thePlayer.inventory.currentItem = getBestSlot(mc.theWorld.getBlockState(block).getBlock());
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
