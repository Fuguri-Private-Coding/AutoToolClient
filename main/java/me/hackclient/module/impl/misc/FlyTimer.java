package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

@ModuleInfo(name = "FlyTimer", category = Category.MISC)
public class FlyTimer extends Module {

    IntegerSetting X = new IntegerSetting("X", this, 0, 10, 0);
    IntegerSetting Y = new IntegerSetting("Y", this, 0, 10, 0);

    final StopWatch timer;
    boolean canUse;

    public FlyTimer() {
        timer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            Packet packet = packetEvent.getPacket();
            if (packet instanceof S02PacketChat s02 && s02.getType() != 2) {
                String msg = s02.getChatComponent().getFormattedText();
                if (msg.equalsIgnoreCase("Вы использовали свои способности!")) {
                    ClientUtils.chatLog("Detected fly");
                    timer.reset();
                }
            }
        }
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            X.setMax(sc.getScaledWidth());
            Y.setMax(sc.getScaledHeight());

            long leftTime = 60000 - timer.reachedMS();

            if (leftTime < 0) {
                leftTime = 0;
                canUse = true;
            }

            mc.fontRendererObj.drawString("Can use: " + canUse, X.getValue(), Y.getValue(), -1);
            mc.fontRendererObj.drawString(String.format("Time: %.1f", leftTime / 1000f), X.getValue(), Y.getValue(), -1);
        }
    }

}
