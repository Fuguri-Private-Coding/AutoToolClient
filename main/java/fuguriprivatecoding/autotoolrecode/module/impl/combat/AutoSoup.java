package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.Range;

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

    final IntegerSetting minUseDelay = new IntegerSetting("MinUseDelay", this, 0, 10, 0) {
        @Override
        public int getValue() {
            if (maxUseDelay.value < value) { value = maxUseDelay.value; }
            return value;
        }
    };
    final IntegerSetting maxUseDelay = new IntegerSetting("MaxUseDelay", this, 0, 10, 0) {
        @Override
        public int getValue() {
            if (minUseDelay.value > value) { value = minUseDelay.value; }
            return value;
        }
    };

    final CheckBox refill = new CheckBox("Refill", this, true);

    final IntegerSetting minRefillDelay = new IntegerSetting("MinRefillDelay", this, refill::isToggled, 0, 10, 0) {
        @Override
        public int getValue() {
            if (maxRefillDelay.value < value) { value = maxRefillDelay.value; }
            return value;
        }
    };
    final IntegerSetting maxRefillDelay = new IntegerSetting("MaxRefillDelay", this, refill::isToggled, 0, 10, 0) {
        @Override
        public int getValue() {
            if (minRefillDelay.value > value) { value = minRefillDelay.value; }
            return value;
        }
    };

    private final StopWatch soupTimer = new StopWatch();
    private final StopWatch refillTimer = new StopWatch();
    private int soupWaitTime = 0;
    private boolean switchBack;
    private int lastSoupSlot;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (mc.currentScreen == null) {
                if (soupTimer.reachedMS(soupWaitTime * 50L)) {
                    if (switchBack) {
                        mc.thePlayer.inventory.currentItem = lastSoupSlot;

                        switchBack = false;
                        soupWaitTime = RandomUtils.nextInt(minUseDelay.getValue(), maxUseDelay.getValue());
                        soupTimer.reset();
                    }

                    if (Range.between(1, 9).contains(getSoupSlot()) && mc.thePlayer.getHealth() < RandomUtils.nextInt(minHealth.getValue(), maxHealth.getValue())) {
                        lastSoupSlot = mc.thePlayer.inventory.currentItem;

                        mc.thePlayer.inventory.currentItem = getSoupSlot();

                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        mc.thePlayer.dropOneItem(false);

                        switchBack = true;
                        soupWaitTime = RandomUtils.nextInt(minUseDelay.getValue(), maxUseDelay.getValue());
                        soupTimer.reset();
                    }
                } else if (mc.thePlayer.getCurrentEquippedItem().getItem() == Items.bowl) {
                    mc.thePlayer.dropOneItem(false);
                }

                refillTimer.reset();
            } else if (mc.currentScreen instanceof GuiInventory) {
                if (refill.isToggled() && refillTimer.reachedMS(RandomUtils.nextInt(minRefillDelay.getValue(), maxRefillDelay.getValue()) * 50L)) {
                    for (int slot = InventoryUtils.EXCLUDE_ARMOR_BEGIN; slot < InventoryUtils.ONLY_HOT_BAR_BEGIN; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                        if (stack != null) {
                            if (stack.getItem() instanceof ItemSoup) {
                                for (int hotbarSlot = InventoryUtils.ONLY_HOT_BAR_BEGIN; hotbarSlot < 45; hotbarSlot++) {
                                    final ItemStack hotbarStack = mc.thePlayer.inventoryContainer.getSlot(hotbarSlot).getStack();

                                    if (hotbarStack == null) {
                                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);

                                        if (RandomUtils.nextInt(minRefillDelay.getValue(), maxRefillDelay.getValue()) > 0) {
                                            refillTimer.reset();
                                            return;
                                        }
                                    }
                                }
                            } else if (stack.getItem() == Items.bowl) {
                                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getSoupSlot() {
        int slot = -1;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
                if (heldItem != null && heldItem.getItem() instanceof ItemSoup) {
                    continue;
                }

                slot = i;
            }
        }
        return slot;
    }
}
