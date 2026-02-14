package fuguriprivatecoding.autotoolrecode.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@UtilityClass
public class ItemUtils {

    private final List<Item> WHITELISTED_ITEMS = Arrays.asList(Items.fishing_rod, Items.water_bucket, Items.bucket, Items.arrow, Items.bow, Items.snowball, Items.egg, Items.ender_pearl);

    private final List<Block> BLACKLISTED_BLOCKS = Arrays.asList(
        Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
        Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
        Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
        Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
        Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
        Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
        Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
        Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
        Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
        Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
        Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs,
        Blocks.stone_stairs, Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
        Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
        Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
        Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
        Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate
    );

    public float getPlayerRelativeBlockHardness(final World worldIn, final BlockPos pos, final int slot) {
        final Block block = mc.theWorld.getBlockState(pos).getBlock();
        final float f = block.getBlockHardness(worldIn, pos);
        return f < 0.0F ? 0.0F : (!canHeldItemHarvest(block, slot) ? getToolDigEfficiency(block, slot) / f / 100.0F : getToolDigEfficiency(block, slot) / f / 30.0F);
    }

    public float getStrVsBlock(final Block blockIn, final int slot) {
        float f = 1.0F;

        if (mc.thePlayer.inventory.mainInventory[slot] != null) {
            f *= mc.thePlayer.inventory.mainInventory[slot].getStrVsBlock(blockIn);
        }
        return f;
    }

    public ItemStack getCurrentItemInSlot(final int slot) {
        return slot < 9 && slot >= 0 ? mc.thePlayer.inventory.mainInventory[slot] : null;
    }

    public float getToolDigEfficiency(final Block blockIn, final int slot) {
        float f = getStrVsBlock(blockIn, slot);

        if (f > 1.0F) {
            final int i = EnchantmentHelper.getEfficiencyModifier(mc.thePlayer);
            final ItemStack itemstack = getCurrentItemInSlot(slot);

            if (i > 0 && itemstack != null) {
                f += (float) (i * i + 1);
            }
        }

        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            f *= 1.0F + (float) (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
            final float f1 = switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= f1;
        }

        if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
            f /= 5.0F;
        }

        if (!mc.thePlayer.onGround) {
            f /= 5.0F;
        }

        return f;
    }

    public int findBlockInHotBar() {
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null || !(item.getItem() instanceof ItemBlock itemBlock) || item.stackSize == 0) continue;

            Block block = itemBlock.getBlock();

            if (ItemUtils.blackListedBlock(block)) continue;

            bestSlot = i;
        }

        return bestSlot;
    }

    public boolean canHeldItemHarvest(final Block blockIn, final int slot) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            final ItemStack itemstack = mc.thePlayer.inventory.getStackInSlot(slot);
            return itemstack != null && itemstack.canHarvestBlock(blockIn);
        }
    }

    public boolean useful(final ItemStack stack) {
        final Item item = stack.getItem();

        if (item instanceof ItemPotion potion) {
            return ItemPotion.isSplash(stack.getMetadata()) && PlayerUtils.goodPotion(potion.getEffects(stack).getFirst().getPotionID());
        }

        if (item instanceof ItemBlock) {
            final Block block = ((ItemBlock) item).getBlock();
            if (block instanceof BlockGlass || block instanceof BlockStainedGlass || (block.isFullBlock() && !(block instanceof BlockTNT || block instanceof BlockSlime || block instanceof BlockFalling))) {
                return true;
            }
        }

        return item instanceof ItemSword ||
                item instanceof ItemTool ||
                item instanceof ItemArmor ||
                item instanceof ItemFood ||
                WHITELISTED_ITEMS.contains(item);
    }

    public boolean blackListedBlock(Block block) {
        return BLACKLISTED_BLOCKS.contains(block);
    }

    public ItemStack getItemStack(String command) {
        try {
            command = command.replace('&', '§');
            final String[] args;
            int i = 1;
            int j = 0;
            args = command.split(" ");
            final ResourceLocation resourcelocation = new ResourceLocation(args[0]);
            final Item item = Item.itemRegistry.getObject(resourcelocation);

            if (args.length >= 2 && args[1].matches("\\d+")) {
                i = Integer.parseInt(args[1]);
            }

            if (args.length >= 3 && args[2].matches("\\d+")) {
                j = Integer.parseInt(args[2]);
            }

            final ItemStack itemstack = new ItemStack(item, i, j);
            if (args.length >= 4) {
                final StringBuilder NBT = new StringBuilder();

                int nbtCount = 3;
                while (nbtCount < args.length) {
                    NBT.append(" ").append(args[nbtCount]);
                    nbtCount++;
                }

                itemstack.setTagCompound(JsonToNBT.getTagFromJson(NBT.toString()));
            }
            return itemstack;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return new ItemStack(Blocks.barrier);
        }
    }
}
