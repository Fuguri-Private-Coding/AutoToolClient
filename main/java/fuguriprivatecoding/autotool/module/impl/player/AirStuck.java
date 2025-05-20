package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.Mode;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@ModuleInfo(name = "AirStuck", category = Category.PLAYER)
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