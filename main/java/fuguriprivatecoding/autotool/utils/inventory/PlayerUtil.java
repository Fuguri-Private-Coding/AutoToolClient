package fuguriprivatecoding.autotool.utils.inventory;

import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.block.Block;
import net.minecraft.util.*;
import java.util.HashMap;

@UtilityClass
public class PlayerUtil implements Imports {

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
     * Checks if a potion is good
     *
     * @return good potion
     */
    public boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

}