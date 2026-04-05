package fuguriprivatecoding.autotoolrecode.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import java.util.Arrays;
import java.util.List;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@UtilityClass
public class ItemUtils {

    private final List<Block> BLACKLIST_BLOCKS = Arrays.asList(
        Blocks.crafting_table,
        Blocks.furnace,
        Blocks.lit_furnace,
        Blocks.chest,
        Blocks.trapped_chest,
        Blocks.ender_chest,
        Blocks.anvil,
        Blocks.enchanting_table,
        Blocks.hopper,
        Blocks.dispenser,
        Blocks.dropper
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

            if (!block.isFullCube() || !block.isOpaqueCube()) continue;

            if (BLACKLIST_BLOCKS.contains(block)) continue;

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
}
