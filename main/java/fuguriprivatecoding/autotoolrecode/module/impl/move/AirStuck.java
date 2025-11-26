package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "AirStuck", category = Category.MOVE, description = "Позволяет вам зависнуть в воздухе.")
public class AirStuck extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Default", "Tick")
            .setMode("Default");

    @Override
    public void onEvent(Event event) {
        if (mode.getMode().equals("Default")) {
            if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S08PacketPlayerPosLook) packetEvent.setCanceled(true);
            if (event instanceof MoveButtonEvent moveButtonEvent) {
                moveButtonEvent.setJump(false);
                moveButtonEvent.setForward(false);
                moveButtonEvent.setRight(false);
                moveButtonEvent.setLeft(false);
                moveButtonEvent.setBack(false);
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
//
//        if (mode.is("Tick") && event instanceof TickEvent e) {
//            e.cancel();
//        }
    }
}