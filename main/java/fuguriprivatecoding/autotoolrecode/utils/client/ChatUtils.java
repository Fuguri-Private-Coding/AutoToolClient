package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.utils.packet.PacketUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.network.play.client.C01PacketChatMessage;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@UtilityClass
public class ChatUtils {

    public void chatLog(Object message) {
        PacketUtils.sendPacket(new C01PacketChatMessage(message.toString()));
    }
}
