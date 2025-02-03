package me.hackclient.module.impl.connection;

import me.hackclient.event.Event;
import me.hackclient.event.PacketDirection;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.misc.ClientHandler;
import me.hackclient.utils.Utils;
import me.hackclient.utils.doubles.Doubles;

@ModuleInfo(name = "Blink", category = Category.CONNECTION)
public class Blink extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
        ClientHandler.PacketHandler.clientPacketBuffer.forEach(p -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.getFirst()));
        ClientHandler.PacketHandler.clientPacketBuffer.clear();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent && packetEvent.getDirection() == PacketDirection.OUTGOING && !packetEvent.isCanceled() && Utils.isWorldLoaded()) {
            packetEvent.setCanceled(true);
            ClientHandler.PacketHandler.clientPacketBuffer.add(new Doubles<>(packetEvent.getPacket(), packetEvent.getSendTime()));
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(ClientHandler.PacketHandler.clientPacketBuffer.size());
    }
}
