package fuguriprivatecoding.autotoolrecode.settings.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.settings.Setting;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public class IntegerSetting extends Setting {
    public int min, max, value;

    private float animatedValue;
    private final float animationSpeed = 0.2f;

    public IntegerSetting(String name, SettingAble parent, int min, int max, int value) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
    }

    public IntegerSetting(String name, SettingAble parent, BooleanSupplier visible, int min, int max, int value) {
        super(name, parent, visible);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
    }

    public void setValue(int value) {
        this.value = Math.clamp(value, min, max);
    }

    public float normalize() {
        return (float) (value - min) / (max - min);
    }

    public float getAnimatedNormalize() {
        return (animatedValue - min) / (max - min);
    }

    public void updateAnimation() {
        animatedValue += (value - animatedValue) * animationSpeed;

        if (Math.abs(value - animatedValue) < 0.1f) {
            animatedValue = value;
        }

        animatedValue = Math.max(min, Math.min(max, animatedValue));
    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("value", value);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        value = object.get("value").getAsInt();
    }
}