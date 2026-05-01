package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import imgui.ImGui;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.function.BooleanSupplier;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;
import static java.lang.Math.round;
import static java.lang.Math.signum;

@Getter
@Setter
public class IntegerSetting extends Setting {
    public int min, max, value;

    EasingAnimation sliderAnim = new EasingAnimation();

    public float animatedValue;

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

    @Override
    public void render() {
        ImGui.pushID(hashCode());
        float[] v = new float[] {value};
        if (ImGui.sliderFloat(getName(), v, min, max)) {
            setValue( Math.round(v[0]));
        }
        ImGui.popID();
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