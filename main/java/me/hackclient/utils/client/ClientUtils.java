package me.hackclient.utils.client;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.core.config.AppenderRef;

public class ClientUtils implements InstanceAccess {

    static String prefixLog = "§bAutoToolClient> →§7 ";

    public static void chatLog(Object message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(prefixLog + message));
    }
}
