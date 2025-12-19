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
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.function.BooleanSupplier;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;
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
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {
        font.drawString(getName() + ": ", x, y, Colors.WHITE.withAlphaClamp(alpha));

        EasingAnimation sliderMinAnim = getSliderMinAnim();
        EasingAnimation sliderMaxAnim = getSliderMaxAnim();

        sliderMinAnim.update(4f, Easing.OUT_CUBIC);
        sliderMinAnim.setEnd((float) minValue);
        sliderMaxAnim.update(4f, Easing.OUT_CUBIC);
        sliderMaxAnim.setEnd((float) maxValue);

        setAnimatedValueMin(sliderMinAnim.getValue());
        setAnimatedValueMax(sliderMaxAnim.getValue());

        final float length = 75;
        float animatedFilledFactorMin = (float) getAnimatedNormalizeMin();
        float animatedFilledFactorMax = (float) getAnimatedNormalizeMax();
        final float sliderLengthMin = animatedFilledFactorMin * length;
        final float sliderLengthMax = animatedFilledFactorMax * length;

        RoundedUtils.drawRect(x, y + 10, length, 4, 1.5f, Colors.GRAY.withAlphaClamp(0.3f * alpha));
        RoundedUtils.drawRect(x, y + 10, sliderLengthMin, 4, 1.5f, elementColor);
        RoundedUtils.drawRect(x + sliderLengthMin - 2, y - 1 + 10, 6, 6, 3f, Color.WHITE);
        font.drawString(String.format("%.2f", getMinValue()), x + length + 5, y + 10, Color.WHITE);

        RoundedUtils.drawRect(x, y + 10 + 10, length, 4, 1.5f, Colors.GRAY.withAlphaClamp(0.3f * alpha));
        RoundedUtils.drawRect(x, y + 10 + 10, sliderLengthMax, 4, 1.5f, elementColor);
        RoundedUtils.drawRect(x + sliderLengthMax - 2, y - 1 + 10 + 10, 6, 6, 3f, Color.WHITE);
        font.drawString(String.format("%.2f", getMaxValue()), x + length + 5, y + 10 + 10, Color.WHITE);

        boolean hoveredMin = GuiUtils.isMouseHovered(x - 2, y - 2 + 10, length + 4, 8);
        boolean hoveredMax = GuiUtils.isMouseHovered(x - 2, y - 2 + 10 + 10, length + 4, 8);

        final ScaledResolution sc = new ScaledResolution(mc);
        int i1 = sc.getScaledWidth();

        final int mouseX = Mouse.getX() * i1 / mc.displayWidth;

        if (hoveredMin) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                setMinValue(getMinValue() + signum(DeltaTracker.getDeltaScroll()) * getStep());
            } else if (Mouse.isButtonDown(0)) {
                float mx = mouseX - x;
                float p = mx / length;
                float normalize = (float) (getMin() + (getMax() - getMin()) * p);
                setMinValue(normalize);
            }
        }

        if (hoveredMax) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                setMaxValue(getMaxValue() + signum(DeltaTracker.getDeltaScroll()) * getStep());
            } else if (Mouse.isButtonDown(0)) {
                float mx = mouseX - x;
                float p = mx / length;
                float normalize = (float) (getMin() + (getMax() - getMin()) * p);
                setMaxValue(normalize);
            }
        }

        return 35;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {

        return 35;
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
