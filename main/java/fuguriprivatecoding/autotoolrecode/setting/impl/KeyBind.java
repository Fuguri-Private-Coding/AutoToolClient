package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
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

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        ImGui.text(getName());
//        ImGui.sameLine();
//        String keyName = Keyboard.getKeyName(key);
//        if (keyName == null) keyName = "UNKNOWN";
//
//        if (listeningForKey) {
//            ImGui.text("[Press a key]"); // Индикатор ожидания
//            for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++) {
//                if (ImGui.isKeyPressed(i)) {
//                    key = i;
//                    listeningForKey = false;
//                    break;
//                }
//            }
//        } else if (ImGui.button(keyName)) {
//            listeningForKey = true;
//        }
//        ImGui.popID();
//    }


    @Override
    public float draw(float x, float y, ClientFontRenderer font, Color elementColor, float alpha) {
//        float offset = 0;
//        float nameWidth = (float) font.getStringWidth(getName());
//
//        font.drawString(getName(), x, y, Colors.WHITE.withAlphaClamp(alpha));
//        font.drawString(Keyboard.getKeyName(key), x + nameWidth, y, Colors.WHITE.withAlphaClamp(alpha));
//
//        offset += 15;
//
//        return offset;

        return 0;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFontRenderer font) {

        return 0;
    }

    @Override
    public void keyTyped(int key) {

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
