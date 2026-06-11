package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "AirStuck", category = Category.MOVE, description = "Позволяет вам зависнуть в воздухе.")
public class AirStuck extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Vanilla")
            .setMode("Vanilla");

    @Override
    public void onEvent(Event event) {
        if (mode.getMode().equals("Default")) {
            if (event instanceof PacketEvent e && e.getPacket() instanceof S08PacketPlayerPosLook) e.setCanceled(true);
            if (event instanceof MoveButtonEvent e) {
                MoveUtils.keyBindStop();

                e.setJump(false);
                e.setSneak(false);
            }

            if (event instanceof TickEvent) {
                if (mc.thePlayer.noClip) {
                    mc.thePlayer.motionY = 0.0;
                } else {
                    mc.thePlayer.motionY = 0.0;
                    mc.thePlayer.onGround = true;
                }
            }
        }
    }
}