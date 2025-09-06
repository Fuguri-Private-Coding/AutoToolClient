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
import fuguriprivatecoding.autotoolrecode.utils.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final IntegerSetting startDelay = new IntegerSetting("StartDelay", this, 0, 1000, 250);
    DoubleSlider delay = new DoubleSlider("Delay", this, 0,500,200,1);
    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;

    int delays;

    public ChestStealer() {
        delayStopWatch = new StopWatch();
        startDelayStopWatch = new StopWatch();
    }

    private final String[] list = new String[]{"mode", "delivery", "menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter",
            "shop", "melee", "armor", "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept",
            "soul", "book", "recipe", "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings",
            "user", "preference", "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                if (checkName.isToggled()) {
                    String name = guiChest.getLowerChestInventory().getDisplayName().getUnformattedText().toLowerCase();
                    for (String str : list) {
                        if (name.contains(str)) return;
                    }
                }

                delays = delay.getRandomizedIntValue();

                if (!startDelayStopWatch.reachedMS(startDelay.getValue())) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                List<Integer> nextSlot = updatePos(container);

                if (nextSlot == null) return;

                for (Integer i : nextSlot) {
                    if (delayStopWatch.reachedMS(delays)) {
                        mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                        delayStopWatch.reset();
                    }
                }
            } else {
                startDelayStopWatch.reset();
            }
        }
    }

    List<Integer> updatePos(ContainerChest container) {
        List<Integer> available = new ArrayList<>();

        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            if (InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
                final Slot slot = container.getSlot(i);
                if (slot.getHasStack()) available.add(i);
            }
        }

        return available;
    }
}
