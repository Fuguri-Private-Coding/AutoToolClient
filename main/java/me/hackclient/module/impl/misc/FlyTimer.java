package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.event.events.RunGameLoopEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
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

    float leftTime;
    final StopWatch timer;
    boolean canUse;

    public FlyTimer() {
        timer = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof WorldChangeEvent) {
            canUse = true;
            leftTime = 0;
        }
        if (event instanceof PacketEvent packetEvent) {
            Packet packet = packetEvent.getPacket();
            if (packet instanceof S02PacketChat s02 && s02.getType() != 2) {
                String msg = s02.getChatComponent().getFormattedText();
                if (msg.contains("Вы использовали свои способности!") ) {
                    ClientUtils.chatLog("Detected fly");
                    leftTime = 60;
                    canUse = false;
                }
            }
        }
        if (event instanceof RunGameLoopEvent) {
            if (leftTime > 0) {
                leftTime -= timer.reachedMS() / 1000f;
            } else {
                leftTime = 0;
                canUse = true;
            }
            timer.reset();
        }
        if (event instanceof Render2DEvent) {
            ScaledResolution sc = new ScaledResolution(mc);
            X.setMax(sc.getScaledWidth());
            Y.setMax(sc.getScaledHeight());

            mc.fontRendererObj.drawString("Can use: " + canUse, X.getValue(), Y.getValue(), -1);
            mc.fontRendererObj.drawString(String.format("Time: %.2f", leftTime), X.getValue(), Y.getValue() + 11, -1);
        }
    }

}
