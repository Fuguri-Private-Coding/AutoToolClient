package me.hackclient.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.hackclient.module.Module;
import me.hackclient.settings.Setting;
import net.minecraft.util.MathHelper;

import java.util.function.BooleanSupplier;

public class FloatSetting extends Setting {

    @Getter @Setter float min, max, step;

    @Getter float value;

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

    public void setValue(float value) {
        this.value = Math.round(MathHelper.clamp(value, min, max) / step) * step;
    }

    public float normalize() {
        return (value - min) / (max - min);
    }
}
