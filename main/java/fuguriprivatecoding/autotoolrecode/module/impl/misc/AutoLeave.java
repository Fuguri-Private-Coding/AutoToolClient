package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.KeyEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.KeyBind;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "AutoLeave", category = Category.MISC)
public class AutoLeave extends Module {

    KeyBind key = new KeyBind("Key", this, Keyboard.KEY_M);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof KeyEvent keyEvent && keyEvent.getKey() == key.getKey()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/hub"));
        }
    }
}