package fuguriprivatecoding.autotoolrecode.utils.client;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import net.minecraft.util.ChatComponentText;

public class ClientUtils implements Imports {

    public static String prefixLog = "§f[§9AutoTool§f] ";

    /**
     * @param message Объект, который будет выведен в чат игры.
     */
    public static void chatLog(Object message) {
        if (mc.thePlayer == null) {
            System.out.println("[" + Client.INST.CLIENT_NAME + "] " + message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(prefixLog + message));
        }
    }

    public static void chatLogWithoutPrefix(Object message) {
        if (mc.thePlayer == null) {
            System.out.println(message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText((String) message));
        }
    }
}
