package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import net.minecraft.network.play.client.C01PacketChatMessage;

public class AutoLeave extends Module {

    Mode mode = new Mode("Mode", this)
            .addMode("HotKey")
            .setMode("HotKey")
            ;

    @Override
    public void onEnable() {
        if (mode.getMode().equalsIgnoreCase("HotKey")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/hub"));
            toggle();
        }
    }
}
