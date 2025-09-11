package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import imgui.ImGui;
import lombok.Getter;
import lombok.Setter;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class DoubleSlider extends Setting {

    @Setter
    public double min, max, step;
    public double minValue, maxValue;

    private double animatedValueMin;
    private double animatedValueMax;
    private final float animationSpeed = 0.2f;

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


    public void updateAnimations() {
        animatedValueMin += (minValue - animatedValueMin) * animationSpeed;

        if (Math.abs(minValue - animatedValueMin) < 0.01f) {
            animatedValueMin = minValue;
        }

        animatedValueMin = Math.max(min, Math.min(max, animatedValueMin));

        animatedValueMax += (maxValue - animatedValueMax) * animationSpeed;

        if (Math.abs(maxValue - animatedValueMax) < 0.01f) {
            animatedValueMax = maxValue;
        }

        animatedValueMax = Math.max(min, Math.min(max, animatedValueMax));
    }

    @Override
    public void render() {
        ImGui.pushID(hashCode());
        float[] minV = new float[] { (float) minValue };
        float[] maxV = new float[] { (float) maxValue };

        if (ImGui.collapsingHeader(getName())) {
            ImGui.indent();
            if (ImGui.sliderFloat("Min", minV, (float) min, (float) max)) {
                setMinValue(minV[0]);
            }
            if (ImGui.sliderFloat("Max", maxV, (float) min, (float) max)) {
                setMaxValue(maxV[0]);
            }
            ImGui.unindent();
        }

        ImGui.popID();
    }
}
