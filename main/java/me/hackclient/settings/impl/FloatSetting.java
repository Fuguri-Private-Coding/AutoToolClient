package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import me.hackclient.utils.math.MathUtils;

import java.util.function.BooleanSupplier;

@Getter
public class FloatSetting extends Setting {

    @Setter public float min, max, step;

    public float value;

    public FloatSetting(String name, Module parent, float min, float max, float value, float step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
    }

    public FloatSetting(String name, Module parent, BooleanSupplier visible, float min, float max, float value, float step) {
        super(name, parent, visible);
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
    }

    public void setValue(float value) {
        this.value = (float) MathUtils.round(value, step);
    }

    public float normalize() {
        return (value - min) / (max - min);
    }
}
