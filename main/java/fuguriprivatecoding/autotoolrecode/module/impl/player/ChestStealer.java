package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ChestStealer", category = Category.PLAYER, description = "Автоматически берет вещи из сундука.")
public class ChestStealer extends Module {

    final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 20, 5, 1);
    DoubleSlider delay = new DoubleSlider("Delay", this, 0,10,1,1);

    final CheckBox autoClose = new CheckBox("AutoClose", this, true);
    DoubleSlider closeDelay = new DoubleSlider("CloseDelay", this, autoClose::isToggled, 0,10,1,1);

    final CheckBox checkName = new CheckBox("CheckName", this, true);

    final StopWatch delayStopWatch = new StopWatch();
    final StopWatch startDelayStopWatch = new StopWatch();
    final StopWatch closeDelayStopWatch = new StopWatch();

    Vector2f lastPos = new Vector2f(0,0);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            if (mc.currentScreen instanceof GuiChest guiChest) {
                String chestName = guiChest.getLowerChestInventory().getDisplayName().getUnformattedText();

                if (checkName.isToggled()) {
                    if (!chestName.contains("Chest")) return;
                }

                if (!startDelayStopWatch.reachedMS(startDelay.getRandomizedIntValue() * 50L)) return;

                final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

                List<Integer> slotList = new CopyOnWriteArrayList<>();

                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
                    int slot = getClosestSlotIndex(container, lastPos, slotList);
                    if (slot != -1) slotList.add(slot);
                }

                for (Integer i : slotList) {
                    if (delayStopWatch.reachedMS(delay.getRandomizedIntValue() * 50L) && !slotList.isEmpty()) {
                        guiChest.activeSlot = mc.thePlayer.openContainer.getSlot(i);

                        mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                        delayStopWatch.reset();
                        slotList.remove(i);
                    }
                }

                if (autoClose.isToggled()) {
                    if (slotList.isEmpty()) {
                        if (closeDelayStopWatch.reachedMS(closeDelay.getRandomizedIntValue() * 50L)) {
                            mc.thePlayer.closeScreen();
                        }
                    } else {
                        closeDelayStopWatch.reset();
                    }
                }
            } else {
                startDelayStopWatch.reset();
            }
        }
    }

    int getClosestSlotIndex(ContainerChest container, Vector2f lastPos, List<Integer> list) {
        float minDistanceSq = Float.MAX_VALUE;
        int closestSlotIndex = -1;

        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            final Slot slot = container.getSlot(i);
            if (InventoryUtils.isValid(container.getLowerChestInventory().getStackInSlot(i))) {
                if (slot.getHasStack() && !list.contains(i)) {
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
