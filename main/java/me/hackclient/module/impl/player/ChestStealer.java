package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.inventory.InventoryUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "ChestStealer",
        category = Category.PLAYER
)
public class ChestStealer extends Module {

    final IntegerSetting baseDelay = new IntegerSetting("BaseDelay", this, 0, 1000, 50);
    final IntegerSetting distanceFactor = new IntegerSetting    ("DistanceFactor", this, 0, 500, 150);
    final BooleanSetting checkName = new BooleanSetting("CheckName", this, true);

    final StopWatch delayStopWatch;

    int delay;

    Vector2f currentPos = new Vector2f();
    Vector2f nextPos = null;

    public ChestStealer() {
        delayStopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
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

                int nextSlotCalculateX = nextSlot;
                int nextSlotCalculateY = 0;

                while (nextSlotCalculateX > 8) {
                    nextSlotCalculateY++;
                    nextSlotCalculateX -= 8;
                }


                delayStopWatch.reset();
                mc.playerController.windowClick(container.windowId, nextSlot, 0, 1, mc.thePlayer);
                nextPos = new Vector2f(nextSlotCalculateX, nextSlotCalculateY);
                double distance = new Vector2f(
                        nextSlotCalculateX - currentPos.x,
                        nextSlotCalculateY - currentPos.y
                ).length();
                delay = (int) (baseDelay.getValue() + distance * distanceFactor.getValue());

                ClientUtils.chatLog(nextSlotCalculateX + " " + nextSlotCalculateY + " " + nextSlot + " " + delay);

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
