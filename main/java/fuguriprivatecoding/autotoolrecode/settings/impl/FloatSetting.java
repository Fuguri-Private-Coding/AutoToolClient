package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;

import java.util.function.BooleanSupplier;

@Getter
public class FloatSetting extends Setting {

    @Setter public float min, max, step;
    public float value;

    private float animatedValue;
    private final float animationSpeed = 0.2f;

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

    public void updateAnimation() {
        animatedValue += (value - animatedValue) * animationSpeed;

        if (Math.abs(value - animatedValue) < 0.01f) {
            animatedValue = value;
        }

        animatedValue = Math.max(min, Math.min(max, animatedValue));
    }
}