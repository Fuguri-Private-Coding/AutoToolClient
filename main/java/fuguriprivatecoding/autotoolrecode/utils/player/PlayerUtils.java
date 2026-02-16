package fuguriprivatecoding.autotoolrecode.utils.player;

import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.*;

@UtilityClass
public class PlayerUtils implements Imports {

    private final List<Material> blackListMaterial = Arrays.asList(
        Material.lava, Material.water, Material.cactus, Material.air, Material.anvil, Material.cake,
        Material.carpet, Material.dragonEgg, Material.fire, Material.portal, Material.redstoneLight,
        Material.circuits, Material.coral
    );

    private final List<Block> blackListBlock = Arrays.asList(
        Blocks.chest, Blocks.anvil, Blocks.ender_chest, Blocks.crafting_table,
        Blocks.furnace, Blocks.dragon_egg, Blocks.skull, Blocks.trapped_chest
    );

    private final HashMap<Integer, Integer> GOOD_POTIONS = new HashMap<>() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static int findTool(final BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;

        final IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack == null) {
                continue;
            }

            final float speed = itemStack.getStrVsBlock(blockState.getBlock());

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static boolean isReplaceable(BlockPos blockPos) {
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }

    /**
     * Gets a potions ranking
     *
     * @return potion ranking
     */
    public int potionRanking(final int id) {
        return GOOD_POTIONS.getOrDefault(id, -1);
    }

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    /**
     * Gets the distance between 2 positions
     *
     * @return distance
     */
    public double distance(final BlockPos pos1, final BlockPos pos2) {
        final double x = pos1.getX() - pos2.getX();
        final double y = pos1.getY() - pos2.getY();
        final double z = pos1.getZ() - pos2.getZ();
        return x * x + y * y + z * z;
    }

    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    public Block blockAheadOfPlayer(final double offsetXZ, final double offsetY) {
        return blockRelativeToPlayer(-Math.sin(MoveUtils.direction()) * offsetXZ, offsetY, Math.cos(MoveUtils.direction()) * offsetXZ);
    }

    public BlockPos getPossibleBlockPos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();

        for (float x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
            for (float y = playerPos.getY() - 3; y <= playerPos.getY(); ++y) {
                for (float z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (!PlayerUtils.isReplaceable(new BlockPos(x, y, z))) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                        double ex = MathHelper.clamp(mc.thePlayer.posX, blockPos.getX(), (double) blockPos.getX() + block.getBlockBoundsMaxX());
                        double ey = MathHelper.clamp(mc.thePlayer.posY, blockPos.getY(), (double) blockPos.getY() + block.getBlockBoundsMaxY());
                        double ez = MathHelper.clamp(mc.thePlayer.posZ, blockPos.getZ(), (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
                        Vec3 vec3 = new Vec3(ex, ey, ez);
                        if (!blackListMaterial.contains(block.getMaterial()) && !blackListBlock.contains(block)) {
                            positions.add(vec3);
                            hashMap.put(vec3, blockPos);
                        }
                    }
                }
            }
        }

        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(pos -> mc.thePlayer.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord)));
            return hashMap.get(positions.getFirst());
        }

        return null;
    }

    public int teleport(int ticks, int additionalTicks) {
        int balance = 0;

        for (int i = 0; i < ticks; i++) {
            try {
                mc.runTick();
                balance++;
                if (i == ticks - 1) {
                    balance += additionalTicks;
                }
            } catch (Exception ignored) {}
        }

        return balance;
    }

    /**
     * Checks if a potion is good
     *
     * @return good potion
     */
    public boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

}