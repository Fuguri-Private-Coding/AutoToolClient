package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.LegitClickTimingEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.math.RandomUtils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoup extends Module {

    int health;

    final IntegerSetting minHealth = new IntegerSetting("MinHealth", this, 4, 20, 9) {
        @Override
        public int getValue() {
            if (maxHealth.value < value) { value = maxHealth.value; }
            return super.getValue();
        }
    };

    final IntegerSetting maxHealth = new IntegerSetting("MaxHealth", this, 4, 20, 12) {
        @Override
        public int getValue() {
            if (minHealth.value > value) { value = minHealth.value; }
            return super.getValue();
        }
    };

    int delayBetweenUse;
    final IntegerSetting minDelayBetweenUse = new IntegerSetting("MinDelayBetweenUse", this, 0, 10, 9) {
        @Override
        public int getValue() {
            if (maxDelayBetweenUse.value < value) { value = maxDelayBetweenUse.value; }
            return super.getValue();
        }
    };

    final IntegerSetting maxDelayBetweenUse = new IntegerSetting("MaxDelayBetweenUse", this, 0, 10, 9) {
        @Override
        public int getValue() {
            if (minDelayBetweenUse.value > value) { value = minDelayBetweenUse.value; }
            return super.getValue();
        }
    };

    final BooleanSetting randomSlot = new BooleanSetting("RandomSlot", this, false);

    public AutoSoup() {
        resetValues();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof LegitClickTimingEvent) {
            if (mc.currentScreen instanceof GuiInventory) {

            } else {
                if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() == Items.bowl) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                    mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
                    resetValues();
                    return;
                }

                if (delayBetweenUse > 0) {
                    delayBetweenUse--;
                    return;
                }

                final int soupSlot = getSoupInHotBar();
                if (mc.thePlayer.getHealth() <= health && soupSlot != -1) {
                    mc.thePlayer.inventory.currentItem = soupSlot;

                    mc.playerController.syncCurrentPlayItemNoEvent();
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));

                    delayBetweenUse = RandomUtils.nextInt(minDelayBetweenUse.getValue(), maxDelayBetweenUse.getValue());
                }
            }
        }
    }

    int getSoupInHotBar() {
        List<Integer> possibleSlots = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null || item.getItem() != Items.mushroom_stew) { continue; }

            possibleSlots.add(i);
        }

        if (possibleSlots.isEmpty()) {
            return -1;
        }

        if (randomSlot.isToggled()) {
            return possibleSlots.get(RandomUtils.nextInt(0, possibleSlots.size() - 1));
        } else {
            return possibleSlots.get(0);
        }
    }

    void resetValues() {
        health = RandomUtils.nextInt(minHealth.getValue(), maxHealth.getValue());
        delayBetweenUse = RandomUtils.nextInt(minDelayBetweenUse.getValue(), maxDelayBetweenUse.getValue());
    }

//    public int getEmptySoup() {
//        if (mc.currentScreen instanceof GuiInventory) {
//            GuiInventory inventory = (GuiInventory)mc.currentScreen;
//
//            for(int i = 36; i < 45; ++i) {
//                ItemStack item = inventory.inventorySlots.getInventory().get(i);
//                if (item != null && item.getItem() == Items.bowl) {
//                    return i;
//                }
//            }
//        }
//
//        return -1;
//    }
//
//    public int getSoupExceptHotbar() {
//        for(int i = 9; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
//            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
//            if (item != null && item.getItem() instanceof ItemSoup) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    public int getSoupInWholeInventory() {
//        for(int i = 0; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
//            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
//            if (item != null && item.getItem() instanceof ItemSoup) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    int getSoup() {
//        for (int i = 0; i < 9; ++i) {
//            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
//            if (item == null || !(item.getItem() instanceof ItemSoup)) continue;
//            return i;
//        }
//        return -1;
//    }
}
