package fuguriprivatecoding.autotoolrecode.newsetting;

import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

// created by dicves_recode on 11.02.2026
@Getter
@Setter
public abstract class Value<T> implements SaveLoadable {
    protected final String name;
    private final T defaultValue;
    protected T value;

    protected BooleanSupplier visible;

    public Value(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    public void reset() {
        value = defaultValue;
    }

    public <E extends Value<?>> E visibleIf(BooleanSupplier visible) {
        this.visible = visible;
        return (E) this;
    }

    public boolean visible() {
        return visible.getAsBoolean();
    }
}
