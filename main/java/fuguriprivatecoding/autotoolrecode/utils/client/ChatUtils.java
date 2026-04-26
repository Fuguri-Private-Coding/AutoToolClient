package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.utils.Utils;
import lombok.experimental.UtilityClass;
import net.minecraft.network.play.client.C01PacketChatMessage;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;

@UtilityClass
public class ChatUtils {

    public void chatLog(Object message) {
        if (Utils.nullCheck()) return;
        mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(message.toString()));
    }
}
