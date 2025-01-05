package me.hackclient.settings.impl;

import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import net.minecraft.util.MathHelper;

import java.util.function.BooleanSupplier;

public class FloatSetting extends Setting {

    private float min, max, value, step;

    public FloatSetting(String name, Module parent, float min, float max, float value, float step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
    }

    public FloatSetting(String name, Module parent, BooleanSupplier visible, float min, float max, float value, float step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
        this.setVisible(visible);
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = Math.round(MathHelper.clamp(value, min, max) / step) * step;
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public float normalize() {
        return (value - min) / (max - min);
    }
}
