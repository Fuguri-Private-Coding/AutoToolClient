package fuguriprivatecoding.autotoolrecode.utils.inventory;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.UpdateEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryUtil implements Imports {

    private static boolean selector;

    public static boolean selector() {
        return selector;
    }

    public InventoryUtil() {
        Client.INST.getEventManager().register(this);
    }


    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int ONLY_HOT_BAR_BEGIN = 36;
    public static final int END = 45;

    public static final List<Block> blacklistedBlocks = Arrays.asList(
            Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest,
            Blocks.flowing_lava, Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull,
            Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore,
            Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore, Blocks.wooden_pressure_plate,
            Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire,
            Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower, Blocks.red_mushroom,
            Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace,
            Blocks.sand, Blocks.cactus, Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table,
            Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall, Blocks.oak_fence, Blocks.activator_rail,
            Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs, Blocks.birch_stairs,
            Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs,
            Blocks.oak_stairs, Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs,
            Blocks.spruce_stairs, Blocks.stone_brick_stairs, Blocks.stone_stairs, Blocks.double_wooden_slab,
            Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2, Blocks.web,
            Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston,
            Blocks.piston_extension, Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest,
            Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner, Blocks.wall_banner, Blocks.deadbush,
            Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
            Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate
    );

    public static boolean isValidStack(final ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && isGoodBlockStack(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemPotion && isBuffPotion(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemFood && isGoodFood(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemEgg || stack.getItem() instanceof ItemSnowball) {
            return true;
        } else if (stack.getItem() instanceof ItemTool && isBestTool(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemArmor && isBestArmor(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemBow && isBestBow(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemSword && isBestSword(stack)) {
            return true;
        } else {
            return stack.getItem() == Items.arrow ||
                    stack.getItem() instanceof ItemFishingRod ||
                    stack.getItem() instanceof ItemEnderPearl ||
                    stack.getItem() == Items.water_bucket;
        }
    }

    public static double getBowDamage(final ItemStack stack) {
        double damage = 0.0;
        if (stack.getItem() instanceof ItemBow && stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
        }
        return damage;
    }

    public static boolean isBestBow(ItemStack itemStack) {
        AtomicDouble bestBowDmg = new AtomicDouble(-1.0D);
        AtomicReference<ItemStack> bestBow = new AtomicReference<>(null);

        forEachInventorySlot(InventoryUtil.EXCLUDE_ARMOR_BEGIN, InventoryUtil.END, ((slot, stack) -> {
            if (stack.getItem() instanceof ItemBow) {
                double damage = getBowDamage(stack);

                if (damage > bestBowDmg.get()) {
                    bestBow.set(stack);
                    bestBowDmg.set(damage);
                }
            }
        }));

        return itemStack == bestBow.get() ||
                getBowDamage(itemStack) > bestBowDmg.get();
    }

    public static void forEachInventorySlot(final int begin, final int end, final SlotConsumer consumer) {
        for (int i = begin; i < end; ++i) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null) {
                consumer.accept(i, stack);
            }
        }
    }

    public static boolean isBuffPotion(final ItemStack stack) {
        final ItemPotion potion = (ItemPotion) stack.getItem();
        final List<PotionEffect> effects = potion.getEffects(stack);
        final List<Integer> BAD_EFFECTS_IDS = Arrays.asList(Potion.poison.id, Potion.weakness.id, Potion.wither.id, Potion.blindness.id, Potion.digSlowdown.id, Potion.harm.id);
        if (effects.isEmpty()) {
            return false;
        }
        for (final PotionEffect effect : effects) {
            if (BAD_EFFECTS_IDS.contains(effect.getPotionID())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGoodFood(final ItemStack stack) {
        final ItemFood food = (ItemFood) stack.getItem();
        return food instanceof ItemAppleGold || (food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3f);
    }

    public static boolean isBestArmor(final ItemStack itemStack) {
        final ItemArmor itemArmor = (ItemArmor) itemStack.getItem();

        double reduction = 0.0;
        ItemStack bestStack = null;

        for (int i = INCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemArmor stackArmor) {
                if (stackArmor.armorType == itemArmor.armorType) {
                    final double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction) {
                        reduction = newReduction;
                        bestStack = stack;
                    }
                }
            }
        }

        return bestStack == itemStack || getDamageReduction(itemStack) > reduction;
    }

    public static boolean isBestSword(ItemStack itemStack) {
        AtomicDouble damage = new AtomicDouble(0.0);
        AtomicReference<ItemStack> bestStack = new AtomicReference<>(null);

        forEachInventorySlot(EXCLUDE_ARMOR_BEGIN, END, (slot, stack) -> {
            if (stack.getItem() instanceof ItemSword) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage.get()) {
                    damage.set(newDamage);
                    bestStack.set(stack);
                }
            }
        });

        return bestStack.get() == itemStack || damage.get() < getItemDamage(itemStack);
    }

    public static boolean isBestTool(final ItemStack itemStack) {
        final int type = getToolType(itemStack);

        Tool bestTool = new Tool(0, -1, null);

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
                final double efficiency = getToolEfficiency(stack);
                if (efficiency > bestTool.efficiency)
                    bestTool = new Tool(0, efficiency, stack);
            }
        }

        return bestTool.stack == itemStack ||
                getToolEfficiency(itemStack) > bestTool.efficiency;
    }

    public static int getToolType(final ItemStack stack) {
        final ItemTool tool = (ItemTool) stack.getItem();
        if (tool instanceof ItemPickaxe) return 0;
        if (tool instanceof ItemAxe) return 1;
        if (tool instanceof ItemSpade) return 2;

        return -1;
    }

    public static float getToolEfficiency(ItemStack itemStack) {
        float efficiency = 4;

        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);

        if (lvl > 0)
            efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public static double getItemDamage(final ItemStack stack) {
        double damage = 0.0;
        final Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();
        for (final String attributeName : attributeModifierMap.keySet()) {
            if (attributeName.equals("generic.attackDamage")) {
                final Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext()) {
                    damage += attributeModifiers.next().getAmount();
                    break;
                }
                break;
            }
        }
        if (stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }
        return damage;
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0;

        ItemArmor armor = (ItemArmor) stack.getItem();
        reduction += armor.damageReduceAmount;
        if (stack.isItemEnchanted()) {
            reduction += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25D;
        }
        return reduction;
    }

    public static boolean isGoodBlockStack(final ItemStack stack) {
        return stack.stackSize >= 1 && !blacklistedBlocks.contains(Block.getBlockFromItem(stack.getItem()));
    }

    public static boolean isInventoryEmpty(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i) == null) continue;

            if (InventoryUtil.isValidStack(inventory.getStackInSlot(i)))
                return false;
        }
        return true;
    }

    public static boolean isInventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack())
                return false;
        }
        return true;
    }

    public static int findBestBlockStack() {
        int bestSlot = -1;
        int blockCount = -1;
        for (int i = 44; i >= 9; --i) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBlock && InventoryUtil.isGoodBlockStack(stack) && stack.stackSize > blockCount) {
                bestSlot = i;
                blockCount = stack.stackSize;
            }
        }
        return bestSlot;
    }

    public static int getEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack == null || itemStack.getEnchantmentTagList() == null || itemStack.getEnchantmentTagList().hasNoTags())
            return 0;

        for (int i = 0; i < itemStack.getEnchantmentTagList().tagCount(); i++) {
            final NBTTagCompound tagCompound = itemStack.getEnchantmentTagList().getCompoundTagAt(i);

            if ((tagCompound.hasKey("ench") && tagCompound.getShort("ench") == enchantment.effectId) || (tagCompound.hasKey("id") && tagCompound.getShort("id") == enchantment.effectId))
                return tagCompound.getShort("lvl");
        }

        return 0;
    }

    @FunctionalInterface
    public interface SlotConsumer {
        void accept(final int p0, final ItemStack p1);
    }

    public record Tool(int slot, double efficiency, ItemStack stack) {}

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof UpdateEvent) {
            if (getItemStack() != null) {
                ItemStack itemStack = getItemStack();

                selector = !trueName(itemStack).contains(itemStack.getDisplayName());
            } else {
                selector = false;
            }
        }
    }

    public static boolean selector(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        } else if (itemStack == getItemStack()) {
            return selector();
        } else {
            return !trueName(itemStack).contains(itemStack.getDisplayName());
        }
    }

    public static ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    public static int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.currentItem;
    }

    public static String trueName(ItemStack itemStack) {
        String name = (StatCollector.translateToLocal(itemStack.getUnlocalizedName() + ".name")).trim();
        final String s1 = EntityList.getStringFromID(itemStack.getMetadata());

        if (s1 != null) {
            name = name + " " + StatCollector.translateToLocal("entity." + s1 + ".name");
        }

        return name;
    }

    public static boolean selector(int itemSlot) {
        return selector(mc.thePlayer.inventory.getStackInSlot(itemSlot));
    }


    public static boolean needDropGOVNO(ItemStack itemStack) {
        ItemStack bestSword = getBestWeapon();

        if (bestSword == null) return false;

        return itemStack.getItem() instanceof ItemSword && itemStack != bestSword;
    }

    public static ItemStack getBestWeapon() {
        ItemStack bestSword = null;
        float damage = -1;

        for (int i = 0; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

                if (is.getItem() instanceof ItemSword) {
                    float swordDamage = getItemDamages(is);

                    if (swordDamage > damage) {
                        damage = getItemDamages(is);
                        bestSword = is;
                    }
                }
            }
        }

        return bestSword;
    }

    public static void drop(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
    }

    public static float getItemDamages(ItemStack itemStacks) {
        float damage = getToolMaterialRating(itemStacks, true);
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStacks) * 1.25F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStacks) * 0.50F;
        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStacks) * 0.01F;
        damage += (itemStacks.getMaxDamage() - itemStacks.getItemDamage()) * 0.000000000001F;

        if (itemStacks.getItem() instanceof ItemSword) {
            damage += 0.2f;
        }

        return damage;
    }

    public static float getToolMaterialRating(ItemStack itemStack, boolean checkForDamage) {
        final Item is = itemStack.getItem();
        float rating = 0;

        if (is instanceof ItemSword) {
            rating = switch (((ItemSword) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 4;
                case "STONE" -> 5;
                case "IRON" -> 6;
                case "EMERALD" -> 7;
                default -> rating;
            };
        } else if (is instanceof ItemPickaxe) {
            rating = switch (((ItemPickaxe) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 2;
                case "STONE" -> 3;
                case "IRON" -> checkForDamage ? 4 : 40;
                case "EMERALD" -> checkForDamage ? 5 : 50;
                default -> rating;
            };
        } else if (is instanceof ItemAxe) {
            rating = switch (((ItemAxe) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 3;
                case "STONE" -> 4;
                case "IRON" -> 5;
                case "EMERALD" -> 6;
                default -> rating;
            };
        } else if (is instanceof ItemSpade) {
            rating = switch (((ItemSpade) is).getToolMaterialName()) {
                case "WOOD", "GOLD" -> 1;
                case "STONE" -> 2;
                case "IRON" -> 3;
                case "EMERALD" -> 4;
                default -> rating;
            };
        }

        return rating;
    }
}