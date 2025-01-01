package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "AutoBot", category = Category.MISC)
public class AutoBot extends Module {

    String[] names = new String[] {
            "dilik_tot",
            "BestAttacking",
            "renessansez",
            "Dimoshik",
            "AMFETAMONI",
            "flugger",
            "heal",
            "mvdfresko",
            "Kethadie",
            "666qvlentines",
            "windows007",
            "NASR1K",
            "0hweexanny"
    };

    boolean zaMapoi;
    boolean zaMapoiHuesos;
    List<String> huesosi = new ArrayList<>();
    List<String> onlineHuesosi = new ArrayList<>();

    void register(String... names) {
        huesosi = List.of(names);
    }

    public AutoBot() {
        register(names);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent && mc.thePlayer.ticksExisted % 20 == 0) {
            updateInfo();
        }

        if (event instanceof PacketEvent packetEvent
        && packetEvent.getPacket() instanceof S02PacketChat s02
        && s02.getType() != 2)  handleChatMsg(s02.getChatComponent().getFormattedText());

        // Screen debug
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            int offset = 0;
            int color = new Color(150, 15, 15, 255).getRGB();
            mc.fontRendererObj.drawString(
                    "Za Mapoi: " + zaMapoi,
                    sc.getScaledWidth() / 2f,
                    sc.getScaledHeight() / 2f + offset,
                    color, true
            );
            offset += 11;
            mc.fontRendererObj.drawString(
                    "Za Mapoi Huesos: " + zaMapoiHuesos,
                    sc.getScaledWidth() / 2f,
                    sc.getScaledHeight() / 2f + offset,
                    color, true
            );
            offset += 11;
            if (zaMapoiHuesos) {
//                String str = "";
//                for (String s : onlineHuesosi) {
//                    str += s + (onlineHuesosi.get(onlineHuesosi.size() - 1).equals(s));
//                }

                mc.fontRendererObj.drawString(
                        "Huesosi: " + onlineHuesosi,
                        sc.getScaledWidth() / 2f,
                        sc.getScaledHeight() / 2f + offset,
                        color, true
                );
                offset += 11;

            }


        }
    }

    void updateInfo() {
        onlineHuesosi.clear();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            for (String name : huesosi) {
                if (name.equalsIgnoreCase(player.getName())) {
                    onlineHuesosi.add(player.getName());
                }
            }
        }
        zaMapoiHuesos = !onlineHuesosi.isEmpty();
    }

    void handleChatMsg(String msg) {
        System.out.println("checking msg -> " + msg);
        if (msg.contains("подключились") && msg.contains("biomas")) {

        }
    }
}
