package fuguriprivatecoding.autotoolrecode.event;

import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class EventCaller {
    @Getter
    private static final EventCaller instance = new EventCaller();
    private EventCaller() {}

    private final List<EventListener> listeners = new ArrayList<>();

    void register(EventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    void unRegister(EventListener listener) {
        listeners.remove(listener);
    }

    void call(Event event, boolean onlyInWorld) {
        Minecraft mc = Minecraft.getMinecraft();

        if ((mc.thePlayer == null || mc.theWorld == null) && onlyInWorld) {
            return;
        }

        // TODO Исправить брейк поинты и в принципе куча экзепшенов.

        try {
            for (EventListener listener : listeners) {
                if (!listener.shouldListenEvents())
                    continue;

                listener.onEvent(event);
            }
        } catch (Exception ignored) {
//            System.out.println(ignored.getMessage());
        }
    }
}