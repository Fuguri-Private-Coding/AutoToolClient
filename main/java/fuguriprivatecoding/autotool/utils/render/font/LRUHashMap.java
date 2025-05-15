package fuguriprivatecoding.autotool.utils.render.font;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LRUHashMap(int size) {
        super(size, 0.75F, true);
        this.maxSize = size;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return (size() > this.maxSize);
    }
}
