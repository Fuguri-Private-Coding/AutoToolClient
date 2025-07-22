package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "AirStuck", category = Category.PLAYER, description = "Позволяет вам зависнуть в воздухе.")
public class AirStuck extends Module {

    Mode mode = new Mode("Mode", this)
            .addModes("Default")
            .setMode("Default");

    @EventTarget
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Default" -> {
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
        }
    }
}