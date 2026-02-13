package fuguriprivatecoding.autotoolrecode.newsetting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.newsetting.Value;

// created by dicves_recode on 11.02.2026
public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public JsonObject getJson() {
        JsonObject object = new JsonObject();
        object.addProperty("toggled", value);
        return object;
    }

    @Override
    public void setJson(JsonObject object) {
        if (!object.has("toggled")) {
            System.out.println("при загрузке \"" + name + "\" не был найден параметр \"toggled\"");
            return;
        }

        value = object.get("toggled").getAsBoolean();
    }
}
