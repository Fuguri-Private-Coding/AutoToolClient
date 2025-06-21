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
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT)
public class AutoSoup extends Module {

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

    final IntegerSetting minUseDelay = new IntegerSetting("MinUseDelay", this, 0, 500, 0);
    final IntegerSetting maxUseDelay = new IntegerSetting("MaxUseDelay", this, 0, 500, 0);

    final CheckBox refill = new CheckBox("Refill", this, true);

    final IntegerSetting minRefillDelay = new IntegerSetting("MinRefillDelay", this, refill::isToggled, 0, 500, 0);
    final IntegerSetting maxRefillDelay = new IntegerSetting("MaxRefillDelay", this, refill::isToggled, 0, 500, 0);

    final CheckBox autoOpen = new CheckBox("AutoOpen", this, refill::isToggled);
    final CheckBox autoClose = new CheckBox("AutoClose", this, refill::isToggled);

    int health, soupSlot;

    StopWatch useTimer, refillTimer;

    public AutoSoup() {
        useTimer = new StopWatch();
        refillTimer = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (mc.currentScreen == null && autoOpen.isToggled() && hotbarIsEmpty()) openInventory();
            if (mc.currentScreen instanceof GuiInventory && refill.isToggled()) {
                int emptySoup = getEmptySoup();
                if (emptySoup != -1) {
                    if (Math.sin(ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2)) <= 0.5) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, emptySoup, 1, 4, mc.thePlayer);
                    }
                } else {
                    int slot = getSoupExceptHotbar();
                    int i = 0;

                    int randomizeRefillDelay = RandomUtils.nextInt(minRefillDelay.getValue(), maxRefillDelay.getValue());

                    while (refillTimer.reachedMS(randomizeRefillDelay)) {
                        if (i < 9) {
                            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
                            if (item != null) {
                                ++i;
                                continue;
                            }
                        }

                        if (hasEmptySlotsInHotbar()) {
                            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
                            refillTimer.reset();
                        } else if (autoClose.isToggled()) {
                            mc.thePlayer.closeScreen();
                        }
                        break;
                    }
                    resetValues();
                }
            }
        }
        if (event instanceof LegitClickTimingEvent) {
            if (mc.currentScreen != null) resetValues();
            int randomizeUseDelay = RandomUtils.nextInt(minUseDelay.getValue(), maxUseDelay.getValue());
            if (mc.currentScreen == null && useTimer.reachedMS(randomizeUseDelay)) {
                if (mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() == Items.bowl) {
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    resetValues();
                    mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
                    return;
                }

                if (soupSlot == -1) soupSlot = getSoupInHotBar();

                if (mc.thePlayer.getHealth() <= health && soupSlot != -1) {
                    if (mc.thePlayer.inventory.currentItem != soupSlot) {
                        mc.thePlayer.inventory.currentItem = soupSlot;
                        mc.playerController.syncCurrentPlayItemNoEvent();
                        return;
                    }

                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    useTimer.reset();
                }
            }
        }
    }

    boolean hotbarIsEmpty() {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item == null || !(item.getItem() instanceof ItemSoup)) { continue; }
            return false;
        }
        return true;
    }

    public void openInventory() {
        mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
        mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
    }

    public boolean hasEmptySlotsInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || stack.getItem() == null) {
                return true;
            }
        }
        return false;
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

        if (possibleSlots.isEmpty()) return -1;

        return possibleSlots.getFirst();
    }

    void resetValues() {
        soupSlot = -1;
        health = RandomUtils.nextInt(minHealth.getValue(), maxHealth.getValue());
        mc.thePlayer.inventory.fakeCurrentItem = 0;
        mc.thePlayer.inventory.currentItem = 0;
    }
}
