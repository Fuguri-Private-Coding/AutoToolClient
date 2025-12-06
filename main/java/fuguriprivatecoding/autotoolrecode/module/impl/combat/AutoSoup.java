package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "AutoSoup", category = Category.COMBAT, description = "Автоматический хил супами")
public class AutoSoup extends Module {

    DoubleSlider health = new DoubleSlider("Health", this, 1, 20, 9, 1);

    DoubleSlider useDelay = new DoubleSlider("UseDelay", this, 0, 20, 9, 1);
    DoubleSlider dropDelay = new DoubleSlider("DropDelay", this, 0, 20, 9, 1);
    DoubleSlider switchDelay = new DoubleSlider("SwitchDelay", this, 0, 20, 9, 1);

    final CheckBox refill = new CheckBox("Refill", this, true);
    DoubleSlider refillDelay = new DoubleSlider("RefillDelay", this, refill::isToggled, 0, 20, 9, 1);

    private final StopWatch soupTimer = new StopWatch();
    private final StopWatch refillTimer = new StopWatch();
    private final StopWatch useTimer = new StopWatch();
    private final StopWatch dropTimer = new StopWatch();

    private int soupSwitchTime = 0;
    private int soupUseTime = 0;
    private int soupDropTime = 0;
    private boolean switchBack;
    private int lastSoupSlot;

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen == null) {
                if (soupTimer.reachedMS(soupSwitchTime * 50L)) {
                    if (switchBack) {
                        mc.thePlayer.inventory.currentItem = lastSoupSlot;

                        switchBack = false;
                        soupSwitchTime = switchDelay.getRandomizedIntValue();
                        soupUseTime = useDelay.getRandomizedIntValue();
                        soupDropTime = dropDelay.getRandomizedIntValue();
                        soupTimer.reset();
                        useTimer.reset();
                        dropTimer.reset();
                    }

                    int soupSlot = getSoupSlot();

                    if (soupSlot != -1 && mc.thePlayer.getHealth() < health.getRandomizedIntValue()) {
                        lastSoupSlot = mc.thePlayer.inventory.currentItem;

                        mc.thePlayer.inventory.currentItem = soupSlot;

                        if (useTimer.reachedMS(soupUseTime * 50L)) {
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                            if (dropTimer.reachedMS(soupDropTime * 50L)) {
                                mc.thePlayer.dropOneItem(false);
                            }
                        }

                        switchBack = true;
                        soupSwitchTime = switchDelay.getRandomizedIntValue();
                        soupTimer.reset();
                    }
                } else if (mc.thePlayer.getCurrentEquippedItem().getItem() == Items.bowl) {
                    mc.thePlayer.dropOneItem(false);
                }

                refillTimer.reset();
            } else if (mc.currentScreen instanceof GuiInventory) {
                if (refill.isToggled() && refillTimer.reachedMS(refillDelay.getRandomizedIntValue() * 50L)) {
                    for (int slot = InventoryUtils.EXCLUDE_ARMOR_BEGIN; slot < InventoryUtils.ONLY_HOT_BAR_BEGIN; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                        if (stack != null) {
                            if (stack.getItem() instanceof ItemSoup) {
                                for (int hotbarSlot = InventoryUtils.ONLY_HOT_BAR_BEGIN; hotbarSlot < 45; hotbarSlot++) {
                                    final ItemStack hotbarStack = mc.thePlayer.inventoryContainer.getSlot(hotbarSlot).getStack();

                                    if (hotbarStack == null) {
                                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);

                                        if (refillDelay.getRandomizedIntValue() > 0) {
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
