package fuguriprivatecoding.autotoolrecode.utils.player;

import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.*;

@UtilityClass
public class PlayerUtils implements Imports {

    public final List<Material> BLACKLIST_MATERIAL = Arrays.asList(
        Material.lava, Material.water, Material.cactus, Material.air, Material.anvil, Material.cake,
        Material.carpet, Material.dragonEgg, Material.fire, Material.portal, Material.redstoneLight,
        Material.circuits, Material.coral
    );

    public final List<Block> BLACKLIST_BLOCK = Arrays.asList(
        Blocks.chest, Blocks.anvil, Blocks.ender_chest, Blocks.crafting_table,
        Blocks.furnace, Blocks.dragon_egg, Blocks.skull, Blocks.trapped_chest
    );

    /**
     * Gets the block at a position
     *
     * @return block
     */
    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static boolean isReplaceable(BlockPos blockPos) {
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }

    /**
     * Gets the block relative to the player from the offset
     *
     * @return block relative to the player
     */
    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }
//
//    public BlockPos getPossibleBlockPos() {
//        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
//        ArrayList<Vec3> positions = new ArrayList<>();
//        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();
//
//        for (float x = playerPos.getX() - 5; x <= playerPos.getX() + 5; ++x) {
//            for (float y = playerPos.getY() - 3; y <= playerPos.getY(); ++y) {
//                for (float z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
//                    if (!PlayerUtils.isReplaceable(new BlockPos(x, y, z))) {
//                        BlockPos blockPos = new BlockPos(x, y, z);
//                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
//                        double ex = MathHelper.clamp(mc.thePlayer.posX, blockPos.getX(), (double) blockPos.getX() + block.getBlockBoundsMaxX());
//                        double ey = MathHelper.clamp(mc.thePlayer.posY, blockPos.getY(), (double) blockPos.getY() + block.getBlockBoundsMaxY());
//                        double ez = MathHelper.clamp(mc.thePlayer.posZ, blockPos.getZ(), (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
//                        Vec3 vec3 = new Vec3(ex, ey, ez);
//                        if (!BLACKLIST_MATERIAL.contains(block.getMaterial()) && !BLACKLIST_BLOCK.contains(block)) {
//                            positions.add(vec3);
//                            hashMap.put(vec3, blockPos);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!positions.isEmpty()) {
//            positions.sort(Comparator.comparingDouble(pos -> mc.thePlayer.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord)));
//            return hashMap.get(positions.getFirst());
//        }
//
//        return null;
//    }

    public BlockPos getPossibleBlockPos() {
        final double playerX = mc.thePlayer.posX;
        final double playerY = mc.thePlayer.posY;
        final double playerZ = mc.thePlayer.posZ;

        final int baseX = (int) playerX;
        final int baseY = (int) (playerY - 1.0);
        final int baseZ = (int) playerZ;

        BlockPos bestPos = null;
        double bestDist = Double.MAX_VALUE;

        for (int x = baseX - 5; x <= baseX + 5; x++) {
            for (int y = baseY - 3; y <= baseY; y++) {
                for (int z = baseZ - 5; z <= baseZ + 5; z++) {

                    BlockPos blockPos = new BlockPos(x, y, z);

                    if (PlayerUtils.isReplaceable(blockPos)) {
                        continue;
                    }

                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                    if (BLACKLIST_MATERIAL.contains(block.getMaterial()) || BLACKLIST_BLOCK.contains(block)) {
                        continue;
                    }

                    double ex = MathHelper.clamp(playerX, x, x + block.getBlockBoundsMaxX());
                    double ey = MathHelper.clamp(playerY, y, y + block.getBlockBoundsMaxY());
                    double ez = MathHelper.clamp(playerZ, z, z + block.getBlockBoundsMaxZ());

                    double dist = mc.thePlayer.getDistanceSq(ex, ey, ez);

                    if (dist < bestDist) {
                        bestDist = dist;
                        bestPos = blockPos;
                    }
                }
            }
        }

        return bestPos;
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
}