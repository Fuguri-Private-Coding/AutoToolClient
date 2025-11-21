package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import imgui.ImGui;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
public class FloatSetting extends Setting {

    @Setter public float min, max, step;
    public float value;

    EasingAnimation sliderAnim = new EasingAnimation();

    @Setter
    private float animatedValue;

    public FloatSetting(String name, SettingAble parent, float min, float max, float value, float step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
        this.step = step;
    }

    public FloatSetting(String name, SettingAble parent, BooleanSupplier visible, float min, float max, float value, float step) {
        super(name, parent, visible);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
        this.step = step;
    }

    public void setValue(float value) {
        this.value = (float) Math.clamp(MathUtils.round(value, step), min, max);
    }

    public float normalize() {
        return (value - min) / (max - min);
    }

    public float getAnimatedNormalize() {
        return (animatedValue - min) / (max - min);
    }

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        float[] v = new float[] {value};
//        if (ImGui.sliderFloat(getName(), v, min, max)) {
//            setValue(v[0]);
//        }
//        ImGui.popID();
//    }

    @Override
    public float draw(float x, float y, ClientFontRenderer font, Color elementColor, float alpha) {
        return 0;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFontRenderer font) {

    }

    @Override
    public void keyTyped(int key) {

    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("value", value);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        value = object.get("value").getAsFloat();
    }
}