package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import net.minecraft.block.material.Material;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "AutoWater", category = Category.PLAYER)
public class AutoWater extends Module {

    FloatSetting fallDistance = new FloatSetting("Fall Distance", this, 0, 4.5f, 3.5f, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            MovingObjectPosition rayCast = mc.objectMouseOver;
            if (rayCast.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || mc.theWorld.getBlockState(rayCast.getBlockPos()).getBlock().getMaterial() == Material.water
                || mc.thePlayer.isInWater()) return;

            if (mc.thePlayer.fallDistance > fallDistance.getValue()) {
                mc.rightClickMouse();
            }
        }
    }
}
