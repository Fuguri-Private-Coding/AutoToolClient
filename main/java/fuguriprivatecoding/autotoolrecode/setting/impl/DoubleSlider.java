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
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {
        font.drawString(getName(), x, y, Color.WHITE);

        sliderMinAnim.update(4, Easing.OUT_CUBIC);
        sliderMinAnim.setEnd((float) minValue);

        sliderMaxAnim.update(4, Easing.OUT_CUBIC);
        sliderMaxAnim.setEnd((float) maxValue);

        setAnimatedValueMin(sliderMinAnim.getValue());
        setAnimatedValueMax(sliderMaxAnim.getValue());

        float sliderLength = 100;
        float filledStart = (float) (sliderLength * getAnimatedNormalizeMin());
        float filledLength = (float) (sliderLength * (getAnimatedNormalizeMax() - getAnimatedNormalizeMin()));

        RoundedUtils.drawRect(x, y + font.FONT_HEIGHT, sliderLength, 4, 1.5f, Colors.GRAY.withAlphaClamp(0.3f));
        RoundedUtils.drawRect(x + filledStart, y + font.FONT_HEIGHT, filledLength, 4, 1.5f, elementColor);

        RoundedUtils.drawRect(x + filledStart - 2, y + font.FONT_HEIGHT - 1, 6, 6, 3f, Color.WHITE);
        RoundedUtils.drawRect(x + filledStart + filledLength - 2, y + font.FONT_HEIGHT - 1, 6, 6, 3f, Color.WHITE);

        String valueText = String.format("%.2f %.2f", getMinValue(), getMaxValue());
        float valueWidth = font.getStringWidth(valueText);

        font.drawString(valueText, x + sliderLength - valueWidth, y, Color.WHITE);

        //RoundedUtils.drawRect(x - 2, y - 2, sliderLength + 4 + 4, font.FONT_HEIGHT + 8, 0, Colors.WHITE.withAlpha(0.3f));

        final ScaledResolution sc = new ScaledResolution(mc);
        int i1 = sc.getScaledWidth();
        final int mouseX = Mouse.getX() * i1 / mc.displayWidth;
        float sliderCenter = x + filledStart + filledLength / 2;
        boolean hovered = GuiUtils.isMouseHovered(x - 2, y - 2, sliderLength + 4 + 4, font.FONT_HEIGHT + 8);


        if (hovered && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && mouseX < sliderCenter) {
            setMinValue(getMinValue() + signum(DeltaTracker.getDeltaScroll()) * getStep());
        }

        if (draggingMin) {
            if (Mouse.isButtonDown(0)) {
                float mx = mouseX - x;
                float p = mx / sliderLength;
                double normalize = getMin() + (getMax() - getMin()) * p;
                setMinValue(normalize);
            }
        }

        if (hovered && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && mouseX > sliderCenter) {
            setMaxValue(getMaxValue() + signum(DeltaTracker.getDeltaScroll()) * getStep());
        }

        if (draggingMax) {
            if (Mouse.isButtonDown(0)) {
                float mx = mouseX - x;
                float p = mx / sliderLength;
                double normalize = getMin() + (getMax() - getMin()) * p;
                setMaxValue(normalize);
            }
        }

        return 20;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        float sliderLength = 100;
        float filledStart = (float) (sliderLength * getAnimatedNormalizeMin());
        float filledLength = (float) (sliderLength * (getAnimatedNormalizeMax() - getAnimatedNormalizeMin()));

        boolean hovered = GuiUtils.isMouseHovered(x - 2, y - 2, sliderLength + 4 + 4, font.FONT_HEIGHT + 8);

        if (hovered && key == 0) {
            float sliderCenter = x + filledStart + filledLength / 2;

            Vector2i mousePos = GuiUtils.getMousePosition();

            draggingMin = mousePos.x < sliderCenter;
            draggingMax = mousePos.x > sliderCenter;

            System.out.println(sliderCenter + " " + mousePos.x + " " + mousePos.y + " " + draggingMin + " " + draggingMax);
        }

        return 20;
    }

    @Override
    public float mouseReleased(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        draggingMin = false;
        draggingMax = false;

        return 20;
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
