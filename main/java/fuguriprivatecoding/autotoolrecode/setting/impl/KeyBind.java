package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import imgui.ImGui;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;

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
    public void render() {
        ImGui.pushID(hashCode());
        ImGui.text(getName());
        ImGui.sameLine();
        String keyName = Keyboard.getKeyName(key);
        if (keyName == null) keyName = "UNKNOWN";

        if (listeningForKey) {
            ImGui.text("[Press a key]"); // Индикатор ожидания
            for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++) {
                if (ImGui.isKeyPressed(i)) {
                    key = i;
                    listeningForKey = false;
                    break;
                }
            }
        } else if (ImGui.button(keyName)) {
            listeningForKey = true;
        }
        ImGui.popID();
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
