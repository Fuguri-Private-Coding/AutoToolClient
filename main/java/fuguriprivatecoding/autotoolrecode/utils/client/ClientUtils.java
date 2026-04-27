package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ChatComponentText;

@UtilityClass
public class ClientUtils implements Imports {

    public String prefixLog = "§f[§9AutoTool§f] ";

    public void chatLog(Object message) {
        if (!Utils.nullCheck()) {
            System.out.println("[" + Client.CLIENT_NAME + "] " + message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(prefixLog + message));
        }
    }

    public void chatLogWithoutPrefix(Object message) {
        if (!Utils.nullCheck()) {
            System.out.println(message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText((String) message));
        }
    }
}
