package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

@ModuleInfo(name = "FlagDetector", category = Category.MISC, description = "Показывает когда вас телепортирует античит.")
public class FlagDetector extends Module {

    CheckBox resetFlagsOnWorld = new CheckBox("ResetFlagsOnWorld", this, false);

    int flagCount = 0;

    @Override
    public void onEvent(Event event) {
        if (event instanceof PacketEvent packetEvent) {
            if (packetEvent.getPacket() instanceof S08PacketPlayerPosLook) {
                flagCount++;
                if (mc.thePlayer.ticksExisted > 40) {
                    ClientUtils.chatLog("§4Flag Detected: §7" + flagCount);
                }
            }
        }
        if (event instanceof WorldChangeEvent && resetFlagsOnWorld.isToggled()) flagCount = 0;
    }
}
