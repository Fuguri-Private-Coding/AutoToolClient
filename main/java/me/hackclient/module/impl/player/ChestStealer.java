package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER)
public class ChestStealer extends Module {

    final IntegerSetting startDelay = new IntegerSetting("StartDelay", this, 0, 1000, 250);
    final IntegerSetting minDelay = new IntegerSetting("MinDelay", this, 0, 500, 50) {
        @Override
        public int getValue() {
            if (maxDelay.value < value) { value = maxDelay.value; }
            return value;
        }
    };
    final IntegerSetting maxDelay = new IntegerSetting("MaxDelay", this, 0, 500, 50) {
        @Override
        public int getValue() {
            if (minDelay.value > value) { value = minDelay.value; }
            return value;
        }
    };

    final BooleanSetting checkName = new BooleanSetting("CheckName", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;

    int delay;

    public ChestStealer() {
        delayStopWatch = new StopWatch();
        startDelayStopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                if (checkName.isToggled() && !guiChest.getLowerChestInventory().getName().contains("Chest")) {
                    return;
                }

                if (!delayStopWatch.reachedMS(delay)) {
                    return;
                }

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                final int nextSlot = updatePos(container);

                if (nextSlot == -1) {
                    return;
                }

                mc.playerController.windowClick(container.windowId, nextSlot, 0, 1, mc.thePlayer);
                delayStopWatch.reset();
            }
        }
    }

    int updatePos(ContainerChest container) {
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            final Slot slot = container.getSlot(i);

            if (!slot.getHasStack()) {
                continue;
            }

            return i;
        }

        return -1;
    }
}
