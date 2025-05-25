package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoup extends Module {

    int health;

    final IntegerSetting minHealth = new IntegerSetting("MinHealth", this, 4, 20, 9) {
        @Override
        public int getValue() {
            if (maxHealth.value < value) { value = maxHealth.value; }
            return value;
        }
    };

    final IntegerSetting maxHealth = new IntegerSetting("MaxHealth", this, 4, 20, 12) {
        @Override
        public int getValue() {
            if (minHealth.value > value) { value = minHealth.value; }
            return value;
        }
    };

    int delayBetweenUse;
    final IntegerSetting minDelayBetweenUse = new IntegerSetting("MinDelayBetweenUse", this, 0, 10, 9) {
        @Override
        public int getValue() {
            if (maxDelayBetweenUse.value < value) { value = maxDelayBetweenUse.value; }
            return value;
        }
    };

    final IntegerSetting maxDelayBetweenUse = new IntegerSetting("MaxDelayBetweenUse", this, 0, 10, 9) {
        @Override
        public int getValue() {
            if (minDelayBetweenUse.value > value) { value = minDelayBetweenUse.value; }
            return value;
        }
    };

    int soupSlot;

    final CheckBox randomSlot = new CheckBox("RandomSlot", this, false);

    public AutoSoup() {
        resetValues();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (mc.currentScreen instanceof GuiInventory) {
                int emptySoup = getEmptySoup();
                if (emptySoup != -1) {
                    if (Math.sin(ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2)) <= 0.5) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, emptySoup, 1, 4, mc.thePlayer);
                    }
                } else {
                    int slot = getSoupExceptHotbar();
                    int i = 0;

                    while (true) {
                        if (i < 9) {
                            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
                            if (item != null) {
                                ++i;
                                continue;
                            }
                        }

                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
                        break;
                    }
                }
            }
        }
        if (event instanceof LegitClickTimingEvent) {
             if (mc.currentScreen == null) {
                if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() == Items.bowl) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    resetValues();
                    mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;

                    return;
                }

                if (delayBetweenUse > 0) {
                    delayBetweenUse--;
                    return;
                }

                if (soupSlot == -1) {
                    soupSlot = getSoupInHotBar();
                }
                if (mc.thePlayer.getHealth() < health && soupSlot != -1) {
                    if (mc.thePlayer.inventory.currentItem != soupSlot) {
                        mc.thePlayer.inventory.currentItem = soupSlot;
                        mc.playerController.syncCurrentPlayItemNoEvent();
                        return;
                    }

                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    delayBetweenUse = RandomUtils.nextInt(minDelayBetweenUse.getValue(), maxDelayBetweenUse.getValue());
                }
            }
        }
    }

    public int getSoupExceptHotbar() {
        for (int i = 9; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item != null && item.getItem() instanceof ItemSoup) {
                return i;
            }
        }

        return -1;
    }

    public int getEmptySoup() {
        if (mc.currentScreen instanceof GuiInventory inventory) {
            for (int i = 36; i < 45; ++i) {
                ItemStack item = inventory.inventorySlots.getInventory().get(i);
                if (item != null && item.getItem() == Items.bowl) {
                    return i;
                }
            }
        }

        return -1;
    }

    int getSoupInHotBar() {
        List<Integer> possibleSlots = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

            if (item == null || !(item.getItem() instanceof ItemSoup)) { continue; }

            possibleSlots.add(i);
        }

        if (possibleSlots.isEmpty()) {
            return -1;
        }

        if (randomSlot.isToggled()) {
            return possibleSlots.get(RandomUtils.nextInt(0, possibleSlots.size() - 1));
        } else {
            return possibleSlots.getFirst();
        }
    }

    void resetValues() {
        soupSlot = -1;
        health = RandomUtils.nextInt(minHealth.getValue(), maxHealth.getValue());
        delayBetweenUse = RandomUtils.nextInt(minDelayBetweenUse.getValue(), maxDelayBetweenUse.getValue());
    }
}
