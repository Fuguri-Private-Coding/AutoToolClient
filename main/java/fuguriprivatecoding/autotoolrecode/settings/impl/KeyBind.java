package fuguriprivatecoding.autotoolrecode.settings.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class KeyBind extends Setting {

    int key;

    boolean listeningForKey;

    public KeyBind(String name, SettingAble parent, int key) {
        super(name, parent);
        this.key = key;
    }

    public KeyBind(String name, SettingAble parent, BooleanSupplier visible, int key) {
        super(name, parent, visible);
        this.key = key;
    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("key", key);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        key = object.get("key").getAsInt();
    }
}
