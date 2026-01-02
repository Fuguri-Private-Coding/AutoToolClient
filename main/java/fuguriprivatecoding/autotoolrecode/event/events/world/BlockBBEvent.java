package fuguriprivatecoding.autotoolrecode.event.events.world;

import fuguriprivatecoding.autotoolrecode.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Getter
@Setter
@AllArgsConstructor
public class BlockBBEvent extends Event {
    private final World world;
    private final Block block;
    private final BlockPos blockPos;
    private AxisAlignedBB boundingBox, maskBoundingBox;

}
