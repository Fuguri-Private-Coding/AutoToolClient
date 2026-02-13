package fuguriprivatecoding.autotoolrecode.newsetting.nesting;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.newsetting.Value;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Toggleable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// created by dicves_recode on 11.02.2026
@Getter
@Setter
public class ToggleableConfigureable extends Configureable implements Toggleable {
    protected boolean toggled;

    public ToggleableConfigureable(String name) {
        this(name, false);
    }

    public ToggleableConfigureable(String name, boolean defaultToggled) {
        super(name);
        toggled = defaultToggled;
    }

    public ToggleableConfigureable(String name, List<Value<?>> defaultValue, boolean defaultToggled) {
        super(name, defaultValue);
        toggled = defaultToggled;
    }

    @Override
    public JsonObject getJson() {
        JsonObject object = new JsonObject();

        JsonObject settingsObject = new JsonObject();
        JsonObject statesObject = new JsonObject();

        for (Value<?> value : value) {
            settingsObject.add(value.getName(), value.getJson());
        }

        statesObject.addProperty("toggled", toggled);

        object.add("settings", settingsObject);
        object.add("states", statesObject);

        return object;
    }

    @Override
    public void setJson(JsonObject object) {
        if (!object.has("settings") || !object.has("states")) {
            System.out.println("при загрузке \"" + name + "\" не была найден объект \"settings\" или \"states\"");
            return;
        }

        JsonObject settingsObject = object.get("settings").getAsJsonObject();
        JsonObject statesObject = object.get("states").getAsJsonObject();

        for (Value<?> value : value) {
            if (!settingsObject.has(value.getName())) {
                System.out.println("при загрузке \"" + name + "\" не была найдена настройка \"" + value.getName() + "\"");
                continue;
            }

            value.setJson(settingsObject.get(value.getName()).getAsJsonObject());
        }


        if (!statesObject.has("toggled")) {
            System.out.println("при загрузке \"" + name + "\" не был найден параметр \"toggled\" в \"states\"");
            return;
        }

        toggled = statesObject.get("toggled").getAsBoolean();
    }
}
