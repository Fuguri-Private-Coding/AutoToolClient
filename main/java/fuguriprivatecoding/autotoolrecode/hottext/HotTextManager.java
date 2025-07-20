package fuguriprivatecoding.autotoolrecode.hottext;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.KeyEvent;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class HotTextManager implements Imports {
    private List<HotText> hotTexts = new CopyOnWriteArrayList<>();

    public HotTextManager() {
        Client.INST.getEventManager().register(this);
        updateHotKeys();
    }

    public void updateHotKeys() {
        hotTexts = new CopyOnWriteArrayList<>();
        hotTexts.addAll(Client.INST.getHotTextGui().hotKeys);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof KeyEvent e) {
            for (HotText hotText : hotTexts) {
                if (hotText.getKey() != e.getKey()) {
                    continue;
                }

                mc.thePlayer.sendChatMessage(hotText.getText());
            }
        }
    }
}
