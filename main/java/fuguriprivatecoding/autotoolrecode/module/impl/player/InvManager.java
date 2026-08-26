package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
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
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "InvManager", category = Category.PLAYER, description = "Автоматически сортирует ваш инвентарь и выкидывает мусор.")
public class InvManager extends Module {

    private final Mode mode = new Mode("InvMode", this)
        .addModes("OpenInv", "Spoof")
        .setMode("OpenInv")
        ;

    private final DoubleSlider startDelay = new DoubleSlider("StartDelay", this, 0, 10, 0, 1f);

    private final CheckBox instant = new CheckBox("Instant", this, false);

    BooleanSupplier notInstant = () -> !instant.isToggled();

    private final CheckBox autoArmor = new CheckBox("AutoArmor", this);
    private final DoubleSlider armorDelay = new DoubleSlider("ArmorDelay", this, () -> autoArmor.isToggled() && notInstant.getAsBoolean(), 0, 10, 0, 1f);

    private final CheckBox sortItems = new CheckBox("SortItems", this);
    private final DoubleSlider sortDelay = new DoubleSlider("SortDelay", this, () -> sortItems.isToggled() && notInstant.getAsBoolean(), 0, 10, 0, 1f);

    private final CheckBox dropItems = new CheckBox("DropItems", this);
    private final DoubleSlider dropDelay = new DoubleSlider("DropDelay", this, () -> dropItems.isToggled() && notInstant.getAsBoolean(), 0, 10, 0, 1f);

    private final IntegerSetting swordSlot = new IntegerSetting("SwordSlot", this, sortItems::isToggled, 1, 9, 1);
    private final IntegerSetting bowSlot = new IntegerSetting("BowSlot", this, sortItems::isToggled, 1, 9, 3);
    private final IntegerSetting gappleSlot = new IntegerSetting("GappleSlot", this, sortItems::isToggled, 1, 9, 2);
    private final IntegerSetting blockSlotSetting = new IntegerSetting("BlockSlot", this, sortItems::isToggled, 1, 9, 7);
    private final IntegerSetting pickaxeSlot = new IntegerSetting("PickaxeSlot", this, sortItems::isToggled, 1, 9, 4);
    private final IntegerSetting axeSlot = new IntegerSetting("AxeSlot", this, sortItems::isToggled, 1, 9, 5);
    private final IntegerSetting spadeSlot = new IntegerSetting("SpadeSlot", this, sortItems::isToggled, 1, 9, 6);

    private final IntegerSetting maxBlockStacks = new IntegerSetting("MaxBlockStacks", this, 0, 30, 5);
    private final IntegerSetting maxRods = new IntegerSetting("MaxRods", this, 0, 9, 1);

    private final IntegerSetting foodSlot = new IntegerSetting("FoodSlot", this, 1, 9, 8);
    private final IntegerSetting maxFoodStacks = new IntegerSetting("MaxFoodStacks", this, 0, 30, 5);

    private final int[] bestArmorPieces = new int[4];
    private final int[] bestToolSlots = new int[3];

    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private final List<Integer> blockSlot = new ArrayList<>();
    private final List<Integer> fishingRods = new ArrayList<>();
    private final List<Integer> foodSlotList = new ArrayList<>();
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

                if ((this.clientOpen && startDelayTimer.reachedMS(startDelay.getRandomizedIntValue() * 50L)) || (mc.currentScreen == null && !Objects.equals(this.mode.getMode(), "OpenInv"))) {
                    this.clear();

                    for (int slot = InventoryUtils.INCLUDE_ARMOR_BEGIN; slot < InventoryUtils.END; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                        if (stack != null) processInventoryItem(slot, stack);
                    }

                    this.applyLimits();

                    boolean armorReady = armorTimer.reachedMS(armorWait);
                    boolean sortReady = sortTimer.reachedMS(sortWait);
                    boolean dropReady = dropTimer.reachedMS(dropWait);

                    boolean busy = false;

                    if (instant.isToggled()) {
                        if (armorReady && this.equipArmor(true)) {
                            busy = true;
                            resetTimings();
                        }

                        if (dropReady && this.dropItem(this.trash, true)) {
                            busy = true;
                            resetTimings();
                        }

                        if (sortReady && this.sortItems(true)) {
                            busy = true;
                            resetTimings();
                        }
                    } else {
                        if (armorReady && this.equipArmor(false)) {
                            busy = true;
                            resetTimings();
                        } else if (dropReady && this.dropItem(this.trash, false)) {
                            busy = true;
                            resetTimings();
                        } else if (sortReady && this.sortItems(false)) {
                            busy = true;
                            resetTimings();
                        }
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

    private boolean dropItem(final List<Integer> listOfSlots, boolean instant) {
        if (this.dropItems.isToggled()) {
            if (!listOfSlots.isEmpty()) {
                if (instant) {
                    for (Integer slot : listOfSlots) {
                        windowClick(slot, 1, 4);
                    }
                } else {
                    int slot = listOfSlots.removeFirst();
                    windowClick(slot, 1, 4);
                    return true;
                }
            }
        }
        return false;
    }

    private void processInventoryItem(int slot, ItemStack stack) {
        if (stack == null) return;

        if (processCombatItems(slot, stack)) return;
        if (processToolsAndArmor(slot, stack)) return;
        if (processUtilityItems(slot, stack)) return;
        if (processFishingRods(slot, stack)) return;
        if (processGoodFood(slot, stack)) return;

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
        if (stack.getItem() instanceof ItemBlock && InventoryUtils.isGoodBlockStack(stack)) {
            blockSlot.add(slot);
            return true;
        }
        return false;
    }

    private boolean processFishingRods(int slot, ItemStack stack) {
        if (stack.getItem() instanceof ItemFishingRod) {
            fishingRods.add(slot);
            return true;
        }
        return false;
    }

    private boolean processGoodFood(int slot, ItemStack stack) {
        if (stack.getItem() instanceof ItemFood && InventoryUtils.isGoodFood(stack) && !(stack.getItem() instanceof ItemAppleGold && stack.getMetadata() > 0)) {
            foodSlotList.add(slot);
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
        if (instant.isToggled()) return;
        armorTimer.reset();
        dropTimer.reset();
        sortTimer.reset();

        armorWait = armorDelay.getRandomizedIntValue() * 50;
        dropWait = dropDelay.getRandomizedIntValue() * 50;
        sortWait = sortDelay.getRandomizedIntValue() * 50;
    }

    private void applyLimits() {
        blockSlot.sort(Comparator.comparingInt(s -> -mc.thePlayer.inventoryContainer.getSlot(s).getStack().stackSize));
        for (int i = maxBlockStacks.getValue(); i < blockSlot.size(); i++) {
            trash.add(blockSlot.get(i));
        }

        fishingRods.sort(Comparator.comparingInt(s -> -rodDurability(mc.thePlayer.inventoryContainer.getSlot(s).getStack())));
        for (int i = maxRods.getValue(); i < fishingRods.size(); i++) {
            trash.add(fishingRods.get(i));
        }

        foodSlotList.sort(Comparator.comparingInt(s -> -foodValue(mc.thePlayer.inventoryContainer.getSlot(s).getStack())));
        for (int i = maxFoodStacks.getValue(); i < foodSlotList.size(); i++) {
            trash.add(foodSlotList.get(i));
        }
    }

    private int rodDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getItemDamage();
    }

    private int foodValue(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemFood food)) return 0;
        return food.getHealAmount(stack) + (int) (food.getSaturationModifier(stack) * food.getHealAmount(stack));
    }

    private boolean sortItems(boolean instant) {
        if (this.sortItems.isToggled()) {
            final int swordTarget = 35 + this.swordSlot.getValue();
            if (this.bestSwordSlot != -1 && this.bestSwordSlot != swordTarget) {
                this.putItemInSlot(swordTarget, this.bestSwordSlot);
                this.bestSwordSlot = swordTarget;
                if (!instant) return true;
            }

            final int bowTarget = 35 + this.bowSlot.getValue();
            if (this.bestBowSlot != -1 && this.bestBowSlot != bowTarget) {
                this.putItemInSlot(bowTarget, this.bestBowSlot);
                this.bestBowSlot = bowTarget;
                if (!instant) return true;
            }

            if (!this.gappleStackSlots.isEmpty()) {
                this.gappleStackSlots.sort(Comparator.comparingInt(slot -> mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));

                final int bestGappleSlot = this.gappleStackSlots.getFirst();
                final int gappleTarget = 35 + this.gappleSlot.getValue();

                if (bestGappleSlot != gappleTarget) {
                    this.putItemInSlot(gappleTarget, bestGappleSlot);
                    this.gappleStackSlots.set(0, gappleTarget);
                    if (!instant) return true;
                }
            }

            if (!this.blockSlot.isEmpty() && this.maxBlockStacks.getValue() >= 1) {
                this.blockSlot.sort(Comparator.comparingInt(slot -> -mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));

                final int blockSlot = this.blockSlot.getFirst();
                final int blockTarget = 35 + this.blockSlotSetting.getValue();

                final ItemStack atTarget = mc.thePlayer.inventoryContainer.getSlot(blockTarget).getStack();
                final boolean targetHasBlock = atTarget != null && atTarget.getItem() instanceof ItemBlock
                    && InventoryUtils.isGoodBlockStack(atTarget);

                if (blockSlot != blockTarget && !targetHasBlock) {
                    this.putItemInSlot(blockTarget, blockSlot);
                    this.blockSlot.set(0, blockTarget);
                    if (!instant) return true;
                }
            }

            if (!this.foodSlotList.isEmpty() && this.maxFoodStacks.getValue() >= 1) {
                this.foodSlotList.sort(Comparator.comparingInt(s -> -foodValue(mc.thePlayer.inventoryContainer.getSlot(s).getStack())));

                final int foodSlot = this.foodSlotList.getFirst();
                final int foodTarget = 35 + this.foodSlot.getValue();

                final ItemStack atTarget = mc.thePlayer.inventoryContainer.getSlot(foodTarget).getStack();
                final boolean targetHasFood = atTarget != null && atTarget.getItem() instanceof ItemFood && InventoryUtils.isGoodFood(atTarget)
                    && !(atTarget.getItem() instanceof ItemAppleGold && atTarget.getMetadata() > 0);

                if (foodSlot != foodTarget && !targetHasFood) {
                    this.putItemInSlot(foodTarget, foodSlot);
                    this.foodSlotList.set(0, foodTarget);
                    if (!instant) return true;
                }
            }

            final int[] toolSlots = {35 + this.pickaxeSlot.getValue(), 35 + this.axeSlot.getValue(), 35 + this.spadeSlot.getValue()};

            for (final int toolSlot : this.bestToolSlots) {
                if (toolSlot != -1) {
                    final int type = InventoryUtils.getToolType(mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());

                    if (type != -1) {
                        if (toolSlot != toolSlots[type]) {
                            this.putToolsInSlot(type, toolSlots);
                            if (!instant) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean equipArmor(boolean instant) {
        if (this.autoArmor.isToggled()) {
            for (int i = 0; i < this.bestArmorPieces.length; i++) {
                final int piece = this.bestArmorPieces[i];

                if (piece != -1) {
                    int armorPieceSlot = i + 5;
                    final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                    if (stack != null)
                        continue;

                    windowClick(piece, 0, 1);

                    if (!instant) return true;
                }
            }
            return instant;
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
        this.fishingRods.clear();
        this.foodSlotList.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }

}
