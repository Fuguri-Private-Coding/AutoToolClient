package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.DeltaTracker;
import imgui.ImGui;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.function.BooleanSupplier;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;
import static java.lang.Math.round;
import static java.lang.Math.signum;

@Getter
@Setter
public class DoubleSlider extends Setting {

    @Setter
    public double min, max, step;
    public double minValue, maxValue;

    EasingAnimation sliderMinAnim = new EasingAnimation();
    EasingAnimation sliderMaxAnim = new EasingAnimation();

    public double animatedValueMin;
    public double animatedValueMax;

    private boolean draggingMin;
    private boolean draggingMax;

    public DoubleSlider(String name, SettingAble parent, double min, double max, double value, double step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.minValue = value;
        this.maxValue = value;
        this.animatedValueMin = value;
        this.animatedValueMax = value;
        this.step = step;
    }

    public DoubleSlider(String name, SettingAble parent, BooleanSupplier visible, double min, double max, double value, double step) {
        super(name, parent, visible);
        this.min = min;
        this.max = max;
        this.minValue = value;
        this.maxValue = value;
        this.animatedValueMin = value;
        this.animatedValueMax = value;
        this.step = step;
    }

    public void setMinValue(double value) {
        if (minValue >= maxValue) this.maxValue = this.minValue;
        this.minValue = (float) Math.clamp(MathUtils.round(value, step), min, max);
    }

    public void setMaxValue(double value) {
        if (maxValue <= minValue) this.minValue = this.maxValue;
        this.maxValue = (float) Math.clamp(MathUtils.round(value, step), min, max);
    }

    public double getAnimatedNormalizeMin() {
        return (animatedValueMin - min) / (max - min);
    }
    public double getAnimatedNormalizeMax() {
        return (animatedValueMax - min) / (max - min);
    }

    public int getRandomizedIntValue() {
        return RandomUtils.nextInt((int) minValue, (int) maxValue);
    }

    public double getRandomizedDoubleValue() {
        return RandomUtils.nextDouble(minValue, maxValue);
    }

    @Override
    public void render() {
        ImGui.pushID(hashCode());
        float[] vMin = new float[] {(float) minValue};
        float[] vMax = new float[] {(float) minValue};

        float min = (float) this.min;
        float max = (float) this.max;

        if (ImGui.sliderFloat("Min" + getName(), vMin, min, max)) {
            setMinValue(vMin[0]);
        }

        if (ImGui.sliderFloat("Max" + getName(), vMax, min, max)) {
            setMaxValue(vMax[0]);
        }
        ImGui.popID();
    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("min", minValue);
        object.addProperty("max", maxValue);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        minValue = object.get("min").getAsDouble();
        maxValue = object.get("max").getAsDouble();
    }
}
