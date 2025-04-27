package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.Utils;
import me.hackclient.utils.doubles.Doubles;
import net.minecraft.network.Packet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ClientHandler", category = Category.MISC)
public class ClientHandler extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
    }

    @Override
    public boolean handleEvents() {
        return Utils.isWorldLoaded();
    }

    @Override
    public boolean isToggled() {
        return true;
    }

    @Override
    public void toggle() {}

    public static class PacketHandler {
        public static final
        List<Doubles<Packet, Long>>
                serverPacketBuffer = new CopyOnWriteArrayList<>(),
                clientPacketBuffer = new CopyOnWriteArrayList<>();

        public static void resetClientPackets() {
            clientPacketBuffer.forEach(p -> mc.getNetHandler().getNetworkManager().sendPacketNoEvent(p.getFirst()));
            clientPacketBuffer.clear();
        }

        public static void resetServerPackets() {
            serverPacketBuffer.forEach(p -> {
                try {
                    p.getFirst().processPacket(mc.getNetHandler().getNetworkManager().packetListener);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
            serverPacketBuffer.clear();
        }
    }

}
