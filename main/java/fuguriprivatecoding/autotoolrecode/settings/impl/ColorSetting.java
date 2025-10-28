package fuguriprivatecoding.autotoolrecode.settings.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import org.lwjgl.util.vector.Vector4f;
import java.awt.*;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class ColorSetting extends Setting {

    float red, green, blue, alpha;
    float fadeRed, fadeGreen, fadeBlue, fadeAlpha;
    float offset;
    float speed;
    boolean fade, hide;

    private Vector4f currentRadius = new Vector4f(1, 5, 5, 1);
    private Vector4f targetRadius = new Vector4f(1, 5, 5, 1);
    private final float animationSpeed = 0.15f;

    public ColorSetting(String name, SettingAble parent, float red, float green, float blue, float alpha) {
        super(name, parent);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.fadeRed = red;
        this.fadeGreen = green;
        this.fadeBlue = blue;
        this.fadeAlpha = alpha;
        this.offset = 0.5f;
        this.speed = 1.0f;
        this.fade = false;
        this.hide = false;
    }

    public ColorSetting(String name, SettingAble parent, BooleanSupplier visible) {
        super(name, parent, visible);
        this.red = 1;
        this.green = 1;
        this.blue = 1;
        this.alpha = 1;
        this.fadeRed = 1;
        this.fadeGreen = 1;
        this.fadeBlue = 1;
        this.fadeAlpha = 1;
        this.offset = 0.5f;
        this.speed = 1.0f;
        this.fade = false;
        this.hide = false;
    }

    public ColorSetting(String name, SettingAble parent) {
        super(name, parent);
        this.red = 1;
        this.green = 1;
        this.blue = 1;
        this.alpha = 1;
        this.fadeRed = 1;
        this.fadeGreen = 1;
        this.fadeBlue = 1;
        this.fadeAlpha = 1;
        this.offset = 0.5f;
        this.speed = 1.0f;
        this.fade = false;
        this.hide = false;
    }

    public ColorSetting(String name, SettingAble parent, BooleanSupplier visible, float red, float green, float blue, float alpha) {
        super(name, parent, visible);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.fadeRed = red;
        this.fadeGreen = green;
        this.fadeBlue = blue;
        this.fadeAlpha = alpha;
        this.offset = 0.5f;
        this.speed = 1.0f;
        this.fade = false;
        this.hide = false;
    }

    public ColorSetting(String name, SettingAble parent, float red, float green, float blue, float alpha, float fadeRed, float fadeGreen, float fadeBlue, float fadeAlpha, float offset, float speed, boolean fade) {
        super(name, parent);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.fadeRed = fadeRed;
        this.fadeGreen = fadeGreen;
        this.fadeBlue = fadeBlue;
        this.fadeAlpha = fadeAlpha;
        this.offset = offset;
        this.speed = speed;
        this.fade = fade;
        this.hide = false;
    }

    public ColorSetting(String name, SettingAble parent, BooleanSupplier visible, float red, float green, float blue, float alpha, float fadeRed, float fadeGreen, float fadeBlue, float fadeAlpha, float offset, float speed, boolean fade) {
        super(name, parent, visible);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.fadeRed = fadeRed;
        this.fadeGreen = fadeGreen;
        this.fadeBlue = fadeBlue;
        this.fadeAlpha = fadeAlpha;
        this.offset = offset;
        this.speed = speed;
        this.fade = fade;
        this.hide = false;
    }

    public Color getColor() {
        return new Color(red, green, blue, alpha);
    }

    public Color getFadedColor() {
        if (isFade()) return ColorUtils.fadeColor(getColor(), getFadeColor(), getSpeed());
        return ColorUtils.fadeColor(getColor(), getColor(), getSpeed());
    }

    public Color getFadedFloatColor() {
        Color mixedColor = isFade() ? ColorUtils.fadeColor(getColor(), getFadeColor(), getSpeed()) : ColorUtils.fadeColor(getColor(), getColor(), getSpeed());
        return new Color(mixedColor.getRed() / 255f, mixedColor.getGreen() / 255f, mixedColor.getBlue() / 255f, mixedColor.getAlpha() / 255f);
    }

    public Color getMixedColor(int i) {
        if (isFade()) return ColorUtils.mixColor(getColor(), getFadeColor(), i, getOffset(), getSpeed());
        return ColorUtils.mixColor(getColor(), getColor(), i, getOffset(), getSpeed());
    }

    public Color getMixedFloatColor(int i) {
        Color mixedColor = isFade() ? ColorUtils.mixColor(getColor(), getFadeColor(), i, getOffset(), getSpeed()) : ColorUtils.mixColor(getColor(), getColor(), i, getOffset(), getSpeed());
        return new Color(mixedColor.getRed() / 255f, mixedColor.getGreen() / 255f, mixedColor.getBlue() / 255f, mixedColor.getAlpha() / 255f);
    }

    public Color getFadeColor() {
        if (isFade()) return new Color(fadeRed, fadeGreen, fadeBlue, fadeAlpha);
        return new Color(red, green, blue, alpha);
    }

    public void updateAnimation() {
        currentRadius.x += (targetRadius.x - currentRadius.x) * animationSpeed;
        currentRadius.y += (targetRadius.y - currentRadius.y) * animationSpeed;
        currentRadius.z += (targetRadius.z - currentRadius.z) * animationSpeed;
        currentRadius.w += (targetRadius.w - currentRadius.w) * animationSpeed;
    }

    public void setTargetRadius(boolean isHide) {
        targetRadius = isHide ? new Vector4f(5,1,1,5) : new Vector4f(1,5,5,1);
    }

    public Vector4f getAnimatedRadius() {
        return currentRadius;
    }

    public void setColor(Color color) {
        this.red = color.getRed() / 255.0f;
        this.green = color.getGreen() / 255.0f;
        this.blue = color.getBlue() / 255.0f;
        this.alpha = color.getAlpha() / 255.0f;
    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();
        object.addProperty("red", red);
        object.addProperty("green", green);
        object.addProperty("blue", blue);
        object.addProperty("alpha", alpha);
        object.addProperty("fadeRed", fadeRed);
        object.addProperty("fadeGreen", fadeGreen);
        object.addProperty("fadeBlue", fadeBlue);
        object.addProperty("fadeAlpha", fadeAlpha);
        object.addProperty("fade", fade);
        object.addProperty("fadeSpeed", speed);
        object.addProperty("fadeOffset", offset);
        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        red = object.get("red").getAsFloat();
        green = object.get("green").getAsFloat();
        blue = object.get("blue").getAsFloat();
        alpha = object.get("alpha").getAsFloat();
        fadeRed = object.get("fadeRed").getAsFloat();
        fadeGreen = object.get("fadeGreen").getAsFloat();
        fadeBlue = object.get("fadeBlue").getAsFloat();
        fadeAlpha = object.get("fadeAlpha").getAsFloat();
        fade = object.get("fade").getAsBoolean();
        speed = object.get("fadeSpeed").getAsFloat();
        offset = object.get("fadeOffset").getAsFloat();
    }
}