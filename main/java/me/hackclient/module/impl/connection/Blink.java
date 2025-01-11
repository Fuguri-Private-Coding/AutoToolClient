package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PackerDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Blink", category = Category.CONNECTION)
public class Blink extends Module {

    List<Packet> packets;

    public Blink() {
        packets = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        packets.forEach(packet -> mc.thePlayer.sendQueue.addToSendQueue(packet));
        packets.clear();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getDirection() == PackerDirection.OUTGOING) {
            packetEvent.setCanceled(true);
            packets.add(packetEvent.getPacket());
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(packets.size());
    }
}
