package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.function.BooleanSupplier;

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

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        float[] minV = new float[] { (float) minValue };
//        float[] maxV = new float[] { (float) maxValue };
//
//        if (ImGui.collapsingHeader(getName())) {
//            ImGui.indent();
//            if (ImGui.sliderFloat("Min", minV, (float) min, (float) max)) {
//                setMinValue(minV[0]);
//            }
//            if (ImGui.sliderFloat("Max", maxV, (float) min, (float) max)) {
//                setMaxValue(maxV[0]);
//            }
//            ImGui.unindent();
//        }
//
//        ImGui.popID();
//    }

    @Override
    public float draw(float x, float y, ClientFontRenderer font, Color elementColor, float alpha) {
        return 0;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFontRenderer font) {

        return 0;
    }

    @Override
    public void keyTyped(int key) {

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
