package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.event.events.UpdateRenderingItem;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(
        name = "AutoTool",
        category = Category.PLAYER
)
public class AutoTool extends Module {

    int lastSlot;
    boolean flag;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (mc.objectMouseOver == null)
            return;

        if (event instanceof LegitClickTimingEvent) {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.gameSettings.keyBindAttack.isKeyDown()) {
                flag = true;
                BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                int bestSlot = getBestSlot(blockPos);
                if (mc.thePlayer.inventory.currentItem != bestSlot && bestSlot != -1) {
                    lastSlot = mc.thePlayer.inventory.currentItem;
                    mc.thePlayer.inventory.currentItem = bestSlot;
                }
            } else if (flag) {
                flag = false;
                if (lastSlot != -1) {
                    mc.thePlayer.inventory.currentItem = lastSlot;
                }
                lastSlot = -1;
            }
        }
        if (event instanceof UpdateRenderingItem updateRenderingItem) {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.gameSettings.keyBindAttack.isKeyDown() && lastSlot != -1) {
                updateRenderingItem.setStack(mc.thePlayer.inventory.mainInventory[lastSlot]);
            }
        }
    }

    int getBestSlot(BlockPos blockPos) {
        float bestEff = mc.thePlayer.inventory.getCurrentItem() != null ? mc.thePlayer.inventory.getCurrentItem().getStrVsBlock(mc.theWorld.getBlockState(blockPos).getBlock()) : 0.0f;
        int bestSlot = mc.thePlayer.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null) { continue; }

            float eff = item.getStrVsBlock(mc.theWorld.getBlockState(blockPos).getBlock());

            if (eff <= bestEff) {
                continue;
            }

            bestEff = eff;
            bestSlot = i;
        }
        return bestSlot;
    }
}
