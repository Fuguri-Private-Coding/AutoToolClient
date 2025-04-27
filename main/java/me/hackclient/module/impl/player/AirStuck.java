package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveButtonEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@ModuleInfo(name = "AirStuck", category = Category.PLAYER)
public class AirStuck extends Module {

    ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "NoRotate",
            new String[] {
                    "NoRotate"
            }
    );

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        switch (mode.getMode()) {
            case "NoRotate" -> {
                if (event instanceof PacketEvent packetEvent && (packetEvent.getPacket() instanceof S32PacketConfirmTransaction || packetEvent.getPacket() instanceof S08PacketPlayerPosLook)) packetEvent.setCanceled(true);
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