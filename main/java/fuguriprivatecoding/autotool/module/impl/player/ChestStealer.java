package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotool.utils.math.RandomUtils;
import fuguriprivatecoding.autotool.utils.timer.StopWatch;
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

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;

    int delay;

    public ChestStealer() {
        delayStopWatch = new StopWatch();
        startDelayStopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                if (checkName.isToggled() && !guiChest.getLowerChestInventory().getName().contains("Chest")) {
                    return;
                }

                delay = RandomUtils.nextInt(minDelay.getValue(), maxDelay.getValue());

                if (!delayStopWatch.reachedMS(delay) || !startDelayStopWatch.reachedMS(startDelay.getValue())) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                final int nextSlot = updatePos(container);

                if (nextSlot == -1) return;

                mc.playerController.windowClick(container.windowId, nextSlot, 0, 1, mc.thePlayer);
                delayStopWatch.reset();
            } else {
                startDelayStopWatch.reset();
            }
        }
    }

    int updatePos(ContainerChest container) {
        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            final Slot slot = container.getSlot(i);

            if (!slot.getHasStack()) {
                continue;
            }

            return i;
        }

        return -1;
    }
}
