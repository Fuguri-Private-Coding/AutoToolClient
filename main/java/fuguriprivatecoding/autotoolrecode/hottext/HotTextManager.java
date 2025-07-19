package fuguriprivatecoding.autotoolrecode.hottext;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.KeyEvent;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: Луфа сделай гуишку для хокеев, где им можно будет менять текс и клавишу
@Getter
public class HotTextManager implements Imports {
    private final List<HotText> hotTexts = new CopyOnWriteArrayList<>();

    public HotTextManager() {
        Client.INST.getEventManager().register(this);
//        hotTexts.add(new HotText(Keyboard.KEY_U, "/kp1"));
//        hotTexts.add(new HotText(Keyboard.KEY_I, "/kp2"));
//        hotTexts.add(new HotText(Keyboard.KEY_O, "/bw"));
//        hotTexts.add(new HotText(Keyboard.KEY_P, "/sw"));
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
