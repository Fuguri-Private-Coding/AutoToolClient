package fuguriprivatecoding.autotoolrecode.newsetting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.newsetting.Value;
import fuguriprivatecoding.autotoolrecode.utils.value.Doubles;

// created by dicves_recode on 11.02.2026
public class DoubleNumberValue extends Value<Doubles<Number, Number>> {
    private final Number min, max, step;

    public DoubleNumberValue(String name, Number min, Number max, Number defaultMin, Number defaultMax, Number step) {
        super(name, new Doubles<>(defaultMin, defaultMax));
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public JsonObject getJson() {
        JsonObject object = new JsonObject();
        object.addProperty("first", value.getFirst());
        object.addProperty("second", value.getSecond());
        return object;
    }

    @Override
    public void setJson(JsonObject object) {
        if (!object.has("first") || !object.has("second")) {
            System.out.println("при загрузке \"" + name + "\" не был найден параметр \"min\" или \"max\"");
            return;
        }

        value.setFirst(object.get("first").getAsNumber());
        value.setSecond(object.get("second").getAsNumber());
    }
}
