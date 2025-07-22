package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "NoSlow", category = Category.MOVE, description = "Позволяет вам ходить без замедления при использовании предмета.")
public class NoSlow extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Intave")
            .setMode("Intave")
            ;

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer.inventory.getCurrentItem() == null) return;
        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0) return;

        if (event instanceof MotionEvent) {
            if (mc.thePlayer.getHeldItem().getItem() == null
                    || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemFood)) {
                return;
            }

        }
    }
}
