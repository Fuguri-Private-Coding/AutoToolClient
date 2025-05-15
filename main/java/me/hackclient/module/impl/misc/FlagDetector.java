package me.hackclient.module.impl.misc;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.utils.client.ClientUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "FlagDetector", category = Category.MISC)
public class FlagDetector extends Module {

    CheckBox resetFlagsOnWorld = new CheckBox("ResetFlagsOnWorld", this, false);

    int flagCount = 0;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent) {
            if (packetEvent.getPacket() instanceof S08PacketPlayerPosLook) {
                flagCount++;
                if (mc.thePlayer.ticksExisted > 5) {
                    ClientUtils.chatLog("§4Flag Detected: §7" + flagCount);
                }
            }
        }
        if (event instanceof WorldChangeEvent && resetFlagsOnWorld.isToggled()) {
            flagCount = 0;
        }
    }
}
