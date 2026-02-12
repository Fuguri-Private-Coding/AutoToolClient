package fuguriprivatecoding.autotoolrecode.utils;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import net.minecraft.util.BlockPos;

@UtilityClass
public class Utils implements Imports {
    public boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public boolean isWorldLoaded() {
        return nullCheck() && mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ));
    }
}
