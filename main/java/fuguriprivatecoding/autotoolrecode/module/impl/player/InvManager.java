package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.inventory.InventoryUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import java.util.*;

@ModuleInfo(name = "InvManager", category = Category.PLAYER, description = "Автоматически сортирует ваш инвентарь и выкидывает мусор.")
public class InvManager extends Module {

    private final Mode mode = new Mode("InvMode", this)
        .addModes("OpenInv", "Spoof")
        .setMode("OpenInv")
        ;

    private final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 1000, 250, 1f);

    private final CheckBox autoArmor = new CheckBox("AutoArmor", this);
    private final DoubleSlider armorDelay = new DoubleSlider("ArmorDelay", this, autoArmor::isToggled, 0, 1000, 250, 1f);

    private final CheckBox sortItems = new CheckBox("SortItems", this);
    private final DoubleSlider sortDelay = new DoubleSlider("SortDelay", this, sortItems::isToggled, 0, 1000, 250, 1f);

    private final CheckBox dropItems = new CheckBox("DropItems", this);
    private final DoubleSlider dropDelay = new DoubleSlider("DropDelay", this, dropItems::isToggled, 0, 1000, 250, 1f);

    private final int[] bestArmorPieces = new int[4];
    private final int[] bestToolSlots = new int[3];

    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private final List<Integer> blockSlot = new ArrayList<>();
    private final List<Integer> trash = new ArrayList<>();

    private int bestSwordSlot;
    private int bestBowSlot;

    private boolean serverOpen;
    private boolean clientOpen;
    private boolean nextTickCloseInventory;

    public int slot;

    private int armorWait;
    private int sortWait;
    private int dropWait;

    private final StopWatch armorTimer = new StopWatch();
    private final StopWatch sortTimer = new StopWatch();
    private final StopWatch dropTimer = new StopWatch();
    private final StopWatch startDelayTimer = new StopWatch();

    @Override
    public void onEnable() {
        this.clientOpen = mc.currentScreen instanceof GuiInventory;
        this.serverOpen = this.clientOpen;
    }

    @Override
    public void onDisable() {
        this.close();
        this.clear();
    }

    @Override
    public void onEvent(Event event) {
        switch (event) {
            case PacketEvent packetEvent -> {
                final Packet<?> packet = packetEvent.getPacket();
                switch (packet) {
                    case C16PacketClientStatus clientStatus when clientStatus.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT -> {
                        this.clientOpen = true;
                        this.serverOpen = true;
                    }

                    case C0DPacketCloseWindow packetCloseWindow when packetCloseWindow.windowId == mc.thePlayer.inventoryContainer.windowId -> {
                        this.clientOpen = false;
                        this.serverOpen = false;
                        slot = -1;
                    }

                    case S2DPacketOpenWindow _ -> {
                        this.clientOpen = false;
                        this.serverOpen = false;
                    }

                    default -> {}
                }
            }

            case TickEvent _ -> {
                if (!clientOpen) startDelayTimer.reset();

                if ((this.clientOpen && startDelayTimer.reachedMS(startDelay.getRandomizedIntValue())) || (mc.currentScreen == null && !Objects.equals(this.mode.getMode(), "OpenInv"))) {
                    this.clear();

                    for (int slot = InventoryUtils.INCLUDE_ARMOR_BEGIN; slot < InventoryUtils.END; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                        if (stack != null) processInventoryItem(slot, stack);
                    }

                    boolean armorReady = armorTimer.reachedMS(armorWait);
                    boolean sortReady = sortTimer.reachedMS(sortWait);
                    boolean dropReady = dropTimer.reachedMS(dropWait);

                    boolean busy = false;

                    if (armorReady && this.equipArmor()) {
                        busy = true;
                        resetTimings();
                    } else if (dropReady && this.dropItem(this.trash)) {
                        busy = true;
                        resetTimings();
                    } else if (sortReady && this.sortItems()) {
                        busy = true;
                        resetTimings();
                    }

                    if (!busy) {
                        if (this.nextTickCloseInventory) {
                            this.close();
                            this.nextTickCloseInventory = false;
                        } else {
                            this.nextTickCloseInventory = true;
                        }
                    } else {
                        this.open();

                        this.nextTickCloseInventory = false;
                    }
                }
            }

            default -> {}
        }
    }

    private boolean dropItem(final List<Integer> listOfSlots) {
        if (this.dropItems.isToggled()) {
            if (!listOfSlots.isEmpty()) {
                int slot = listOfSlots.removeFirst();
                windowClick(slot, 1, 4);
                return true;
            }
        }
        return false;
    }

    private void processInventoryItem(int slot, ItemStack stack) {
        if (stack == null) return;

        if (processCombatItems(slot, stack)) return;
        if (processToolsAndArmor(slot, stack)) return;
        if (processUtilityItems(slot, stack)) return;

        if (!trash.contains(slot) && !InventoryUtils.isValidStack(stack)) trash.add(slot);
    }

    private boolean processCombatItems(int slot, ItemStack stack) {
        switch (stack.getItem()) {
            case ItemSword _ when InventoryUtils.isBestSword(stack) -> {
                bestSwordSlot = slot;
                return true;
            }

            case ItemBow _ when InventoryUtils.isBestBow(stack) -> {
                bestBowSlot = slot;
                return true;
            }

            case ItemAppleGold _ -> {
                gappleStackSlots.add(slot);
                return true;
            }

            default -> {}
        }
        return false;
    }

    private boolean processToolsAndArmor(int slot, ItemStack stack) {
        switch (stack.getItem()) {
            case ItemTool _ when InventoryUtils.isBestTool(mc.thePlayer, stack) -> {
                updateBestTool(slot, stack);
                return true;
            }

            case ItemArmor armor when InventoryUtils.isBestArmor(mc.thePlayer, stack) -> {
                updateBestArmor(slot, armor);
                return true;
            }

            default -> {}
        }
        return false;
    }

    private boolean processUtilityItems(int slot, ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && slot == InventoryUtils.findBestBlockStack()) {
            blockSlot.add(slot);
            return true;
        }
        return false;
    }

    private void updateBestTool(int slot, ItemStack stack) {
        int toolType = InventoryUtils.getToolType(stack);
        if (toolType != -1 && slot != bestToolSlots[toolType]) {
            bestToolSlots[toolType] = slot;
        }
    }

    private void updateBestArmor(int slot, ItemArmor armor) {
        int currentBestSlot = bestArmorPieces[armor.armorType];
        if (currentBestSlot == -1 || slot != currentBestSlot) {
            bestArmorPieces[armor.armorType] = slot;
        }
    }

    private void resetTimings() {
        armorTimer.reset();
        dropTimer.reset();
        sortTimer.reset();

        armorWait = armorDelay.getRandomizedIntValue();
        dropWait = dropDelay.getRandomizedIntValue();
        sortWait = sortDelay.getRandomizedIntValue();
    }

    private boolean sortItems() {
        if (this.sortItems.isToggled()) {
            if (this.bestSwordSlot != -1) {
                if (this.bestSwordSlot != 36) {
                    this.putItemInSlot(36, this.bestSwordSlot);
                    this.bestSwordSlot = 36;
                    return true;
                }
            }

            if (this.bestBowSlot != -1) {
                if (this.bestBowSlot != 38) {
                    this.putItemInSlot(38, this.bestBowSlot);
                    this.bestBowSlot = 38;
                    return true;
                }
            }

            if (!this.gappleStackSlots.isEmpty()) {
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));

                final int bestGappleSlot = this.gappleStackSlots.getFirst();

                if (bestGappleSlot != 37) {
                    this.putItemInSlot(37, bestGappleSlot);
                    this.gappleStackSlots.set(0, 37);
                    return true;
                }
            }

            if (!this.blockSlot.isEmpty()) {
                this.blockSlot.sort(Comparator.comparingInt(slot -> -mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));

                final int blockSlot = this.blockSlot.getFirst();

                if (blockSlot != 42) {
                    this.putItemInSlot(42, blockSlot);
                    this.blockSlot.set(0, 42);
                    return true;
                }
            }

            final int[] toolSlots = {39, 40, 41};

            for (final int toolSlot : this.bestToolSlots) {
                if (toolSlot != -1) {
                    final int type = InventoryUtils.getToolType(mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());

                    if (type != -1) {
                        if (toolSlot != toolSlots[type]) {
                            this.putToolsInSlot(type, toolSlots);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean equipArmor() {
        if (this.autoArmor.isToggled()) {
            for (int i = 0; i < this.bestArmorPieces.length; i++) {
                final int piece = this.bestArmorPieces[i];

                if (piece != -1) {
                    int armorPieceSlot = i + 5;
                    final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                    if (stack != null)
                        continue;

                    windowClick(piece, 0, 1);

                    return true;
                }
            }
        }
        return false;
    }

    public void windowClick(int slotId, int mouseButtonClicked, int mode) {
        slot = slotId;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, mouseButtonClicked, mode, mc.thePlayer);
    }

    private void putItemInSlot(final int slot, final int slotIn) {
        windowClick(slotIn, slot - 36, 2);
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        final int toolSlot = toolSlots[tool];

        windowClick(this.bestToolSlots[tool],
            toolSlot - 36,
            2);
        this.bestToolSlots[tool] = toolSlot;
    }

    private void open() {
        if (!this.clientOpen && !this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.serverOpen = true;
        }
    }

    private void close() {
        if (!this.clientOpen && this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.serverOpen = false;
        }
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.gappleStackSlots.clear();
        this.blockSlot.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }

}
