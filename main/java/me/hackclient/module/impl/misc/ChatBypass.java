package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;

@ModuleInfo(name = "ChatBypass", category = Category.MISC)
public class ChatBypass extends Module {

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            final Packet packet = packetEvent.getPacket();

            if (packet instanceof C01PacketChatMessage c01 && !c01.getMessage().startsWith("/")) {
                String msg = c01.getMessage();

                msg = msg.replace("и", "u")
                        .replace("с", "c")
                        .replace("а", "a")
                        .replace("р", "p")
                        .replace("у", "y")
                        .replace("з", "z")
                        .replace("о", "o")
                        .replace("х", "x")
                        .replace("ь", "b")
                        .replace("ш", "w")
                        .replace("н", "h")
                        .replace("е", "e")
                        .replace("С", "C")
                        .replace("А", "A")
                        .replace("Р", "P")
                        .replace("У", "Y")
                        .replace("З", "Z")
                        .replace("О", "O")
                        .replace("Х", "X")
                        .replace("Ь", "B")
                        .replace("Ш", "W")
                        .replace("Н", "H")
                        .replace("У", "E");

                c01.setMessage(msg.concat(""));
            }
        }
    }
}
