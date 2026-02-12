package fuguriprivatecoding.autotoolrecode.newsetting.nesting;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.newsetting.Value;
import fuguriprivatecoding.autotoolrecode.newsetting.impl.BooleanValue;
import fuguriprivatecoding.autotoolrecode.newsetting.impl.DoubleNumberValue;
import fuguriprivatecoding.autotoolrecode.newsetting.impl.NumberValue;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// created by dicves_recode on 11.02.2026
public class Configureable extends Value<List<Value<?>>> {
    public Configureable(String name, List<Value<?>> defaultValue) {
        super(name, defaultValue);
    }

    public Configureable(String name) {
        this(name, new ArrayList<>());
    }

    @Setter
    private Configureable owner;

    @Override
    public void reset() {
        for (Value<?> value : value) {
            value.reset();
        }
    }

    public <T extends Configureable> T tree(T configureable) {
        value.add(configureable);
        configureable.setOwner(this);
        return configureable;
    }

    public Configureable group(String name) {
        Configureable value = new Configureable(name);
        this.value.add(value);
        return value;
    }

    public BooleanValue checkbox(String name, boolean defaultValue) {
        BooleanValue value = new BooleanValue(name, defaultValue);
        this.value.add(value);
        return value;
    }

    public NumberValue number(String name, Number min, Number max, Number defaultValue, Number step) {
        NumberValue value = new NumberValue(name, min, max, defaultValue, step);
        this.value.add(value);
        return value;
    }

    public DoubleNumberValue numberRange(String name, Number min, Number max, Number defaultMin, Number defaultMax, Number step) {
        DoubleNumberValue value = new DoubleNumberValue(name, min, max, defaultMin, defaultMax, step);
        this.value.add(value);
        return value;
    }

    @Override
    public JsonObject getJson() {
        JsonObject object = new JsonObject();

        for (Value<?> value : value) {
            object.add(value.getName(), value.getJson());
        }

        return object;
    }

    @Override
    public void setJson(JsonObject object) {
        for (Value<?> value : value) {
            if (!object.has(value.getName())) {
                System.out.println("при загрузке \"" + name + "\" не была найдена настройка \"" + value.getName() + "\"");
                continue;
            }

            value.setJson(object.get(value.getName()).getAsJsonObject());
        }
    }
}
