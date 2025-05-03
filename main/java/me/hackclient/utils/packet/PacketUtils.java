package me.hackclient.utils.packet;

import lombok.experimental.UtilityClass;
import me.hackclient.utils.interfaces.Imports;
import net.minecraft.network.Packet;

@UtilityClass
public class PacketUtils implements Imports {

    /**
     * отправлает {@code packet} без вызова {@code PacketEvent}
     * @param packet пакет который нужно отправить
     */
    public void sendPacket(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
    }


    /**
     * отправлает {@code packet} без вызова {@code PacketEvent}
     * @param packet пакет который нужно принять
     */
    public void recievePacket(Packet packet) {
        try {
            packet.processPacket(mc.getNetHandler().getNetworkManager().packetListener);
        } catch (Exception ignored) {}
    }
}
