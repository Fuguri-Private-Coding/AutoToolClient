package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 1000, 250, 1);
    DoubleSlider delay = new DoubleSlider("Delay", this, 0,500,200,1);

    final CheckBox autoClose = new CheckBox("AutoClose", this, true);
    DoubleSlider closeDelay = new DoubleSlider("CloseDelay", this, autoClose::isToggled, 0,500,200,1);

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch;
    final StopWatch startDelayStopWatch;
    final StopWatch closeDelayStopWatch;

    boolean checked = false;

    Vector2f lastPos = new Vector2f(0,0);

    public ChestStealer() {
        delayStopWatch = new StopWatch();
        startDelayStopWatch = new StopWatch();
        closeDelayStopWatch = new StopWatch();
    }

    private final String[] list = new String[]{"mode", "delivery", "menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter",
            "shop", "melee", "armor", "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept",
            "soul", "book", "recipe", "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings",
            "user", "preference", "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                if (checkName.isToggled()) {
                    String name = guiChest.getLowerChestInventory().getDisplayName().getUnformattedText().toLowerCase();
                    for (String str : list) {
                        if (name.contains(str)) return;
                    }
                }

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
