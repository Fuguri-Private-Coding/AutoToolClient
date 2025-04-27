package me.hackclient.module.impl.combat;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ModeSetting;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Criticals", category = Category.COMBAT)
public class Criticals extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "FunnyMcSkyPvp",
            new String[] {
                    "FunnyMcSkyPvp",
                    "Packet"
            }
    );

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof AttackEvent) {
            switch (mode.getMode()) {
                case "FunnyMcSkyPvp" -> {
                    if (!mc.thePlayer.isCollidedHorizontally)
                        break;

                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624, mc.thePlayer.posZ, true));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.11E-4, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                }
                case "Packet" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                }
            }
        }
    }
}
