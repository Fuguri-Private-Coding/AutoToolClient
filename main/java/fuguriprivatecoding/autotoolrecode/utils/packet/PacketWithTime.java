package fuguriprivatecoding.autotoolrecode.utils.packet;

import net.minecraft.network.Packet;

public record PacketWithTime(Packet packet, long time) {}