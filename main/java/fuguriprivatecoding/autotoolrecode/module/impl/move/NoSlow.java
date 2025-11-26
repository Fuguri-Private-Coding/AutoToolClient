package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
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

    int stage = 0;

    @Override
    public void onDisable() {
        stage = 0;
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer.inventory.getCurrentItem() == null) return;
        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0) return;

        if (event instanceof MotionEvent) {
            if (mc.thePlayer.getHeldItem().getItem() == null
                    || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemFood) || !mc.thePlayer.isUsingItem()) {
                return;
            }

            switch (mode.getMode()) {
                case "Intave" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                }
            }
        }
    }
}
