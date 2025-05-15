package fuguriprivatecoding.autotool.utils.client;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.utils.interfaces.Imports;
import net.minecraft.util.ChatComponentText;

public class ClientUtils implements Imports {

    static String prefixLog = "§3AutoTool §8→§7 ";

    /**
     * @param message Объект, который будет выведен в чат игры.
     */
    public static void chatLog(Object message) {
        if (mc.thePlayer == null) {
            System.out.println(Client.INST.getName() + " → " + message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(prefixLog + message));
        }
    }
}
