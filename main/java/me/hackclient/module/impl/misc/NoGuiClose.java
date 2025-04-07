package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.network.play.server.S2EPacketCloseWindow;

@ModuleInfo(
        name = "NoGuiClose",
        category = Category.MISC
)
public class NoGuiClose extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof S2EPacketCloseWindow) {
            packetEvent.setCanceled(true);
        }
    }
}
