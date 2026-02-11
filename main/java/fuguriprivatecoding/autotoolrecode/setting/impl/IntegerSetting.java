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

//    @Override
//    public void render() {
//        ImGui.pushID(hashCode());
//        float[] v = new float[] {value};
//        if (ImGui.sliderFloat(getName(), v, min, max)) {
//            setValue( Math.round(v[0]));
//        }
//        ImGui.popID();
//    }

    @Override
    public float draw(float x, float y, ClientFont font, Color elementColor, float alpha) {
        font.drawString(getName(), x, y, Color.WHITE);

        EasingAnimation sliderAnim = getSliderAnim();
        sliderAnim.update(4, Easing.OUT_CUBIC);
        sliderAnim.setEnd(value);

        setAnimatedValue(sliderAnim.getValue());

        float sliderX = x;
        float sliderY = y + font.FONT_HEIGHT;

        float sliderLength = 100;
        float filledLength = sliderLength * getAnimatedNormalize();

        RoundedUtils.drawRect(sliderX, sliderY, sliderLength, 4, 1.5f, Colors.GRAY.withAlphaClamp(0.3f));
        RoundedUtils.drawRect(sliderX, sliderY, filledLength, 4, 1.5f, elementColor);
        RoundedUtils.drawRect(sliderX + filledLength - 2, sliderY - 1, 6, 6, 3f, Color.WHITE);

        String valueText = String.valueOf(getValue());
        float valueWidth = font.getStringWidth(valueText);

        font.drawString(String.valueOf(getValue()), x + sliderLength - valueWidth, y, Color.WHITE);

        boolean hovered = GuiUtils.isMouseHovered(x - 2, y - 2, sliderLength + 4, font.FONT_HEIGHT + 8);
        //RoundedUtils.drawRect(x - 2, y - 2, sliderLength + 4, font.FONT_HEIGHT + 8, 0, Colors.WHITE.withAlpha(0.3f));

        if (hovered) {
            final ScaledResolution sc = new ScaledResolution(mc);
            int i1 = sc.getScaledWidth();

            final int mouseX = Mouse.getX() * i1 / mc.displayWidth;

            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                setValue((int) (getValue() + signum(DeltaTracker.getDeltaScroll())));
            } else if (Mouse.isButtonDown(0)) {
                float mx = mouseX - x;
                float p = mx / sliderLength;
                float normalize = getMin() + (getMax() - getMin()) * p;
                setValue(round(normalize));
            }
        }

        return 20;
    }

    @Override
    public float mouseClicked(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {

        return 20;
    }

    @Override
    public float mouseReleased(int mouseX, int mouseY, float x, float y, int key, ClientFont font) {
        return 20;
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
        value = object.get("value").getAsInt();
    }
}