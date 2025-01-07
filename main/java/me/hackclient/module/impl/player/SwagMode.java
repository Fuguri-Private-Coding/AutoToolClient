package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "SwagMode", category = Category.PLAYER)
public class SwagMode extends Module {

    int jumps;

    IntegerSetting ticks = new IntegerSetting("GroundTicks", this, 1, 5, 1);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MotionEvent motionEvent) {
            if (jumps % ticks.getValue() == 0) {
                motionEvent.setOnGround(false);
            }
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                jumps++;
            }
        }
//        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S08PacketPlayerPosLook s08) {
//            packetEvent.setCanceled(true);
//            mc.thePlayer.setPosition(
//                    s08.getX(),
//                    s08.getY(),
//                    s08.getZ()
//            );
//            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
//                    mc.thePlayer.posX,
//                    mc.thePlayer.posY,
//                    mc.thePlayer.posZ,
//                    mc.thePlayer.rotationYaw,
//                    mc.thePlayer.rotationPitch,
//                    false
//            ));
//            mc.displayGuiScreen(null);
//        }
    }
}
