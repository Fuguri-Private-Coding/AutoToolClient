package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.UpdateRenderingItem;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(
        name = "AutoTool",
        category = Category.PLAYER
)
public class AutoTool extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
        switchBack();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (mc.objectMouseOver == null)
            return;

        final MovingObjectPosition mouse = mc.objectMouseOver;

        if (event instanceof LegitClickTimingEvent) {
            if (mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mouse.getBlockPos() == null || !mc.gameSettings.keyBindAttack.isKeyDown()) {
                switchBack();
                return;
            }

            final BlockPos block = mouse.getBlockPos();

            mc.thePlayer.inventory.currentItem = getBestSlot(mc.theWorld.getBlockState(block).getBlock());
        }
        if (event instanceof UpdateRenderingItem updateRenderingItem) {
            updateRenderingItem.setStack(mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.fakeCurrentItem]);
        }

//        if (event instanceof LegitClickTimingEvent) {
//            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.gameSettings.keyBindAttack.isKeyDown()) {
//                flag = true;
//                BlockPos blockPos = mc.objectMouseOver.getBlockPos();
//                int bestSlot = getBestSlot(blockPos);
//                if (mc.thePlayer.inventory.currentItem != bestSlot && bestSlot != -1) {
//                    lastSlot = mc.thePlayer.inventory.currentItem;
//                    mc.thePlayer.inventory.currentItem = bestSlot;
//                }
//            } else if (flag) {
//                flag = false;
//                if (lastSlot != -1) {
//                    mc.thePlayer.inventory.currentItem = lastSlot;
//                }
//                lastSlot = -1;
//            }
//        }
//        if (event instanceof UpdateRenderingItem updateRenderingItem) {
//            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.gameSettings.keyBindAttack.isKeyDown() && lastSlot != -1) {
//                updateRenderingItem.setStack(mc.thePlayer.inventory.mainInventory[lastSlot]);
//            }
//        }
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
