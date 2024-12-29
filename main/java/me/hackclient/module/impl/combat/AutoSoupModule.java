package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoupModule extends Module {

    final StopWatch timer;
    IntegerSetting health = new IntegerSetting("Health", this, 4, 12, 10);

    boolean used, dropped;

    int prevSlot;

    public AutoSoupModule() {
        timer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            int soup = getSoup();

            if (soup == -1)
                return;

            if (timer.reachedMS() < 300)
                return;


            if (mc.thePlayer.getHealth() < health.getValue()) {
                if (mc.thePlayer.inventory.currentItem != soup) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(soup));
                }
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                if (mc.thePlayer.inventory.currentItem != soup) mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }

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
