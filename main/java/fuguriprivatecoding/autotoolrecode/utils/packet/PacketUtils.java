package fuguriprivatecoding.autotoolrecode.utils.packet;

import lombok.experimental.UtilityClass;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

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
    public void receivePacket(Packet packet) {
        try {
            packet.processPacket(mc.getNetHandler().getNetworkManager().packetListener);
        } catch (Exception ignored) {}
    }
}
