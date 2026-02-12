package fuguriprivatecoding.autotoolrecode.newsetting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.newsetting.Value;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;

// created by dicves_recode on 11.02.2026
public class NumberValue extends Value<Number> {
    private final Number min, max, step;

    public NumberValue(String name, Number min, Number max, Number defaultValue, Number step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void setValue(Number value) {
        this.value = Math.clamp((double) MathUtils.rounds(value, step), (double) min, (double) max);
    }

    @Override
    public JsonObject getJson() {
        JsonObject object = new JsonObject();
        object.addProperty("value", value);
        return object;
    }

    @Override
    public void setJson(JsonObject object) {
        if (!object.has("value")) {
            System.out.println("при загрузке \"" + name + "\" не был найден параметр \"value\"");
            return;
        }

        value = object.get("value").getAsNumber();
    }
}
