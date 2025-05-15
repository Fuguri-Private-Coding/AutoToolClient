package fuguriprivatecoding.autotool.module.impl.misc;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.PacketEvent;
import fuguriprivatecoding.autotool.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.utils.client.ClientUtils;
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
