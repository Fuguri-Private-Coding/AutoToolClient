package me.hackclient.utils.client;

import me.hackclient.utils.interfaces.InstanceAccess;
import net.minecraft.util.ChatComponentText;

public class ClientUtils implements InstanceAccess {

    public static void chatLog(Object message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(String.valueOf(message)));
    }



}
