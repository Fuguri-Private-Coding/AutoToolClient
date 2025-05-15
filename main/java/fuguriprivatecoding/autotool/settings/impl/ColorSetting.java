package fuguriprivatecoding.autotool.settings.impl;

import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.settings.Setting;

import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class ColorSetting extends Setting {

    float red, green, blue, alpha;

    public ColorSetting(String name, Module parent, float red, float green, float blue, float alpha) {
        super(name, parent);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ColorSetting(String name, Module parent, BooleanSupplier visible, float red, float green, float blue, float alpha) {
        super(name, parent, visible);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color getColor() {
        return new Color(red, green, blue, alpha);
    }
}
