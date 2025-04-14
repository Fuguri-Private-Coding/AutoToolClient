package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "FlagDetector", category = Category.MISC, toggled = true)
public class FlagDetector extends Module {

    BooleanSetting resetFlagsOnWorld = new BooleanSetting("ResetFlagsOnWorld", this, false);

    int flagCount = 0;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof PacketEvent packetEvent) {
            if (packetEvent.getPacket() instanceof S08PacketPlayerPosLook) {
                flagCount++;
                if (mc.thePlayer.ticksExisted > 5) {
                    ClientUtils.chatLog("§4Flag Detected: §7" + flagCount);
                }
            }
        }

        if (event instanceof TickEvent) {
            if (mc.thePlayer.ticksExisted < 5 && resetFlagsOnWorld.isToggled()) {
                flagCount = 0;
            }
        }
    }
}
