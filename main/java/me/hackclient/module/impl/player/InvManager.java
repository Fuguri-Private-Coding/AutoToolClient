package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.inventory.InventoryUtils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "InvManager", category = Category.PLAYER)
public class InvManager extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent && mc.currentScreen instanceof GuiInventory) {
            for (int i = 0; i < 45; i++) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                    if (is.getItem() == null) { continue; }

                    if (InventoryUtils.needDropGOVNO(is)) {
                        InventoryUtils.drop(i);
                    }
                }
            }
        }
    }
}
