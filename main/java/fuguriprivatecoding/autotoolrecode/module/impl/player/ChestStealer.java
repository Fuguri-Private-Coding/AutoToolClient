package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.lwjgl.util.vector.Vector2f;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 1000, 250, 1);
    DoubleSlider delay = new DoubleSlider("Delay", this, 0,500,200,1);

    final CheckBox autoClose = new CheckBox("AutoClose", this, true);
    DoubleSlider closeDelay = new DoubleSlider("CloseDelay", this, autoClose::isToggled, 0,500,200,1);

    final CheckBox fail = new CheckBox("Fail", this);
    final IntegerSetting failChance = new IntegerSetting("FailChance", this, fail::isToggled, 0, 100, 30);

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;
    final StopWatch closeDelayStopWatch;

    boolean checked = false;

    Vector2f lastPos;

    public ChestStealer() {
        delayStopWatch = new StopWatch();
        startDelayStopWatch = new StopWatch();
        closeDelayStopWatch = new StopWatch();
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

                if (lastPos == null) lastPos = new Vector2f(0,0);

                if (!startDelayStopWatch.reachedMS(startDelay.getRandomizedIntValue())) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
                int availableSlot = getClosestSlotIndex(container, lastPos);

                if (delayStopWatch.reachedMS(delay.getRandomizedIntValue()) && availableSlot != -1) {
                    Slot slot = mc.thePlayer.openContainer.getSlot(availableSlot);

                    guiChest.activeSlot = slot;

                    mc.playerController.windowClick(container.windowId, availableSlot, 0, 1, mc.thePlayer);
                    delayStopWatch.reset();
                }

                if (autoClose.isToggled()) {
                    if (InventoryUtils.isInventoryFull() || InventoryUtils.isInventoryEmpty(container.getLowerChestInventory()) || availableSlot == -1) {
                        if (closeDelayStopWatch.reachedMS(closeDelay.getRandomizedIntValue())) {
                            mc.thePlayer.closeScreen();
                        }
                    } else {
                        closeDelayStopWatch.reset();
                    }
                }
            } else {
                checked = false;
                startDelayStopWatch.reset();
            }
        }
    }

//    int updateAvailableSlots(ContainerChest container, Vector2f lastPos) {
//        List<SlotPosition> slotPositions = new ArrayList<>();
//
//        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
//            if (InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
//                final Slot slot = container.getSlot(i);
//                if (slot.getHasStack()) {
//                    slotPositions.add(new SlotPosition(i, slot.xDisplayPosition + 8, slot.yDisplayPosition + 8));
//                }
//            }
//        }
//
//        if (slotPositions.isEmpty()) {
//            return -1;
//        }
//
//        SlotPosition closestSlot = slotPositions.getFirst();
//        float minDistanceSq = Float.MAX_VALUE;
//
//        for (SlotPosition sp : slotPositions) {
//            float dx = sp.x - lastPos.x;
//            float dy = sp.y - lastPos.y;
//            float distanceSq = dx * dx + dy * dy;
//
//            if (distanceSq < minDistanceSq) {
//                minDistanceSq = distanceSq;
//                closestSlot = sp;
//            }
//        }
//
//        return closestSlot.slotIndex;
//            }

    int getClosestSlotIndex(ContainerChest container, Vector2f lastPos) {
        int closestSlotIndex = -1;
        float minDistanceSq = Float.MAX_VALUE;

        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            final Slot slot = container.getSlot(i);
            if (InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
                if (slot.getHasStack()) {
                    float slotCenterX = slot.xDisplayPosition + 8;
                    float slotCenterY = slot.yDisplayPosition + 8;

                    float dx = slotCenterX - lastPos.x;
                    float dy = slotCenterY - lastPos.y;
                    float distanceSq = dx * dx + dy * dy;

                    if (distanceSq < minDistanceSq) {
                        minDistanceSq = distanceSq;
                        closestSlotIndex = i;
                    }
                }
            }
        }

        return closestSlotIndex;
    }
}
