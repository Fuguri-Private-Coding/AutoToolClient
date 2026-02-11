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
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.function.BooleanSupplier;

import static fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports.mc;
import static java.lang.Math.signum;

@Getter
public class FloatSetting extends Setting {

    @Setter public float min, max, step;
    public float value;

    EasingAnimation sliderAnim = new EasingAnimation();

    @Setter
    private float animatedValue;

    public FloatSetting(String name, SettingAble parent, float min, float max, float value, float step) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
        this.step = step;
    }

    public FloatSetting(String name, SettingAble parent, BooleanSupplier visible, float min, float max, float value, float step) {
        super(name, parent, visible);
        this.min = min;
        this.max = max;
        this.value = value;
        this.animatedValue = value;
        this.step = step;
    }

    public void setValue(float value) {
        this.value = (float) Math.clamp(MathUtils.round(value, step), min, max);
    }

    public float normalize() {
        return (value - min) / (max - min);
    }

    public float getAnimatedNormalize() {
        return (animatedValue - min) / (max - min);
    }

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        float[] v = new float[] {value};
//        if (ImGui.sliderFloat(getName(), v, min, max)) {
//            setValue(v[0]);
//        }
//        ImGui.popID();
//    }

    @Override
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {

        float widthName = font.getStringWidth(getName() + ": ");


        font.drawString(getName() + ": ", x, y, Colors.WHITE.withAlphaClamp(alpha));

        EasingAnimation sliderAnim = getSliderAnim();

        sliderAnim.update(4f, Easing.OUT_CUBIC);
        sliderAnim.setEnd(value);

        setAnimatedValue(sliderAnim.getValue());

        float animatedFilledFactor = getAnimatedNormalize();
        final float length = 75;
        final float sliderLength = animatedFilledFactor * length;

        RoundedUtils.drawRect(x + widthName, y, length, 4, 1.5f, Colors.GRAY.withAlphaClamp(0.3f * alpha));
        RoundedUtils.drawRect(x + widthName, y, sliderLength, 4, 1.5f, elementColor);
        RoundedUtils.drawRect(x + widthName + sliderLength - 2, y - 1, 6, 6, 3f, Color.WHITE);
        font.drawString(String.format("%.2f", getValue()), x + widthName + length + 5, y, Color.WHITE);

        boolean hovered = GuiUtils.isMouseHovered(x + widthName - 2, y - 2, length + 4, 8);

        if (hovered) {
            final ScaledResolution sc = new ScaledResolution(mc);
            int i1 = sc.getScaledWidth();

            final int mouseX = Mouse.getX() * i1 / mc.displayWidth;
            
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                setValue(getValue() + signum(DeltaTracker.getDeltaScroll()) * getStep());
            } else if (Mouse.isButtonDown(0)) {
                float mx = mouseX - (x + widthName);
                float p = mx / length;
                float normalize = getMin() + (getMax() - getMin()) * p;
                setValue(normalize);
            }
        }
        
        return 15;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {

        return 15;
    }

    @Override
    public float mouseReleased(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        return 15;
    }

    @Override
    public void keyTyped(int key) {

    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("value", value);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        value = object.get("value").getAsFloat();
    }
}