package fuguriprivatecoding.autotoolrecode.key;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class KeyCaller implements Imports, EventListener {

    @Getter private static final KeyCaller instance = new KeyCaller();

    private final List<KeyListener> listeners = new ArrayList<>();

    private KeyCaller() {}

    public void init() {
        registerToEvents();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof KeyEvent keyEvent) {
            for (KeyListener listener : listeners) {
                if (!listener.shouldListenKey() || listener.getKey() != keyEvent.getKey())
                    continue;

                listener.onTick(keyEvent.isPressed());
            }
        }
    }

    @Override
    public boolean shouldListenEvents() {
        return Utils.isWorldLoaded();
    }

    void register(KeyListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
}