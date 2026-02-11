package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {
        float widthName = font.getStringWidth(getName() + ": ");

        font.drawString(getName() + ": ", x, y, Colors.WHITE.withAlphaClamp(alpha));
        font.drawString(listeningForKey ? "_" : Keyboard.getKeyName(getKey()),x + widthName, y, elementColor);

        return 15;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        float widthName = font.getStringWidth(getName() + ": ");

        boolean hovered = GuiUtils.isHovered(mouseX, mouseY, x + widthName, y, 40, 10);

        listeningForKey = hovered && key == 0;

        return 15;
    }

    @Override
    public float mouseReleased(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        return 15;
    }

    @Override
    public void keyTyped(int key) {
        if (listeningForKey) {
            setKey(key);
        }
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
