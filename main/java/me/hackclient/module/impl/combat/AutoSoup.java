package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoup extends Module {

    final StopWatch timer;
    IntegerSetting health = new IntegerSetting("Health", this, 4, 12, 10);

//    boolean used, dropped, test1, test2;
//
//    int prevSlot;

    int slot;
    boolean used;

    public AutoSoup() {
        timer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            if (slot == -1) {
                slot = getSoup();
            }

            if (slot == -1)
                return;

            if (timer.reachedMS() < 300)
                return;

            if (mc.thePlayer.getHealth() <= health.getValue() && !used) {
                if (mc.thePlayer.serverSlot != slot) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                    mc.thePlayer.serverSlot = slot;
                }
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)));
                used = true;
                return;
            }

            if (used) {
                if (mc.thePlayer.serverSlot != slot) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                    mc.thePlayer.serverSlot = slot;
                }
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                used = false;
                timer.reset();
                slot = -1;
                if (mc.thePlayer.serverSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    mc.thePlayer.serverSlot = mc.thePlayer.inventory.currentItem;
                }
            }


//            if (mc.thePlayer.getHealth() <= health.getValue() && !test1) {
//                test2 = mc.thePlayer.serverSlot != soup;
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
//                    mc.thePlayer.serverSlot = soup;
//                    return;
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
//                test1 = true;
//                return;
//            }
//
//            if (test1) {
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
//                    mc.thePlayer.serverSlot = mc.thePlayer.inventory.currentItem;
//                }
//                test1 = false;
//                test2 = false;
//                timer.reset();
//            }

//            if (mc.thePlayer.getHealth() < health.getValue() && !test1) {
//                test2 = mc.thePlayer.inventory.currentItem != soup;
//                if (test2) {
//                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
//                }
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
//                test1 = true;
//                return;
//            }
//
//            if (test1) {
//                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                if (test2) mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
//                test1 = false;
//                test2 = false;
//            }

            /*if (mc.thePlayer.inventory.getCurrentItem().getItem() == Items.bowl) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                mc.thePlayer.addChatMessage(new ChatComponentText("try drop"));
                return;
            }

            if (mc.thePlayer.inventory.getCurrentItem() == null || mc.thePlayer.inventory.getCurrentItem().getItem() == null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("swith back"));
                mc.thePlayer.inventory.currentItem = prevSlot;
                timer.reset();
            }*/

            //if (mc.thePlayer.inventory.currentItem != soup) {
            //    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
            //}
            //
            //mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
            //if (mc.thePlayer.inventory.currentItem != soup) {
            //    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            //}
            //timer.reset();
        }
    }

    int getSoup() {
        for (int i = 0; i < 9; ++i) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemSoup)) continue;
            return i;
        }
        return -1;
    }
}
