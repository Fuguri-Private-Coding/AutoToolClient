package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Blink", category = Category.CONNECTION)
public class Blink extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
        PacketHandler.clientPacketBuffer.forEach(p -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.getFirst()));
        PacketHandler.clientPacketBuffer.clear();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getDirection() == PacketDirection.OUTGOING && !packetEvent.isCanceled()) {
            packetEvent.setCanceled(true);
            PacketHandler.clientPacketBuffer.add(new Doubles<>(packetEvent.getPacket(), packetEvent.getSendTime()));
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(PacketHandler.clientPacketBuffer.size());
    }
}
