package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final IntegerSetting startDelay = new IntegerSetting("StartDelay", this, 0, 1000, 250);

    DoubleSlider delay = new DoubleSlider("Delay", this, 0,500,200,1);

    final CheckBox checkName = new CheckBox("CheckName", this, true);
    final CheckBox randomSlots = new CheckBox("RandomSlots", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;

    int delays;

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

                delays = delay.getRandomizedIntValue();

                if (!delayStopWatch.reachedMS(delays) || !startDelayStopWatch.reachedMS(startDelay.getValue())) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                final int nextSlot = updatePos(container, randomSlots.isToggled());

                if (nextSlot == -1) return;

                mc.playerController.windowClick(container.windowId, nextSlot, 0, 1, mc.thePlayer);
                delayStopWatch.reset();
            } else {
                startDelayStopWatch.reset();
            }
        }
    }

    int updatePos(ContainerChest container, boolean random) {
        if (random) {
            int slot = RandomUtils.nextInt(0, container.getLowerChestInventory().getSizeInventory());
            final Slot getSlot = container.getSlot(slot);
            if (getSlot.getHasStack()) return slot;
        } else {
            for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
                final Slot slot = container.getSlot(i);
                if (slot.getHasStack()) return i;
            }
        }

        return -1;
    }
}
