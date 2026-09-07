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
public class BlockBBEvent extends Event {
    @Getter
    private static final BlockBBEvent instance = new BlockBBEvent();
    private BlockBBEvent() {}

    private World world;
    private Block block;
    private BlockPos blockPos;
    private AxisAlignedBB boundingBox, maskBoundingBox;
}
