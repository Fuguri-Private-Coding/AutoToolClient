package fuguriprivatecoding.autotoolrecode.utils.inventory;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import net.minecraft.item.ItemStack;

public class SpoofSlotUtils implements Imports {
    private int spoofedSlot;

    public SpoofSlotUtils() {
        Client.INST.getEventManager().register(this);
    }

    @Getter
    private boolean spoofing;

    public void startSpoofing(int slot) {
        spoofing = true;
        spoofedSlot = slot;
    }

    public void stopSpoofing() {
        spoofing = false;
    }

    public int getSpoofedSlot() {
        return spoofing ? spoofedSlot : mc.thePlayer.inventory.currentItem;
    }

    public ItemStack getSpoofedStack() {
        return spoofing ? mc.thePlayer.inventory.getStackInSlot(spoofedSlot) : mc.thePlayer.inventory.getCurrentItem();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent) {
            stopSpoofing();
        }
    }
}