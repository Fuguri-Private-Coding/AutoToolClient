package me.hackclient.utils.client;

import me.hackclient.Client;
import me.hackclient.utils.interfaces.Imports;
import net.minecraft.util.ChatComponentText;

public class ClientUtils implements Imports {

    static String prefixLog = "§3AutoTool §8→§7 ";

    /**
     * @param message Объект, который будет выведен в чат игры.
     */
    public static void chatLog(Object message) {
        if (mc.thePlayer == null) {
            System.out.println(Client.INSTANCE.getName() + " → " + message);
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText(prefixLog + message));
        }
    }
}
