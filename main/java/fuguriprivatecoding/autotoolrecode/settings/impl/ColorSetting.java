package fuguriprivatecoding.autotoolrecode.settings.impl;

import com.viaversion.viaversion.util.ChatColorUtil;
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
        if (isFade())  return ColorUtils.fadeColor(getColor(), getFadeColor(), getSpeed());
        return ColorUtils.fadeColor(getColor(), getColor(), getSpeed());
    }

    public Color getFadeColor() {
        if (isFade()) return new Color(fadeRed, fadeGreen, fadeBlue, fadeAlpha);
        return new Color(red, green, blue, alpha);
    }

    public void toggleFade() {
        this.fade = !this.fade;
    }

    public int getRedInt() {
        return (int)(red * 255);
    }

    public int getGreenInt() {
        return (int)(green * 255);
    }

    public int getBlueInt() {
        return (int)(blue * 255);
    }

    public int getAlphaInt() {
        return (int)(alpha * 255);
    }

    public int getFadeRedInt() {
        return (int)(fadeRed * 255);
    }

    public int getFadeGreenInt() {
        return (int)(fadeGreen * 255);
    }

    public int getFadeBlueInt() {
        return (int)(fadeBlue * 255);
    }

    public int getFadeAlphaInt() {
        return (int)(fadeAlpha * 255);
    }

    public void updateAnimation() {
        currentRadius.x += (targetRadius.x - currentRadius.x) * animationSpeed;
        currentRadius.y += (targetRadius.y - currentRadius.y) * animationSpeed;
        currentRadius.z += (targetRadius.z - currentRadius.z) * animationSpeed;
        currentRadius.w += (targetRadius.w - currentRadius.w) * animationSpeed;
    }

    public void setTargetRadius(boolean isHide) {
        targetRadius = isHide ? new Vector4f(1,5,5,1) : new Vector4f(5,1,1,5);
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

    public void setFadeColor(Color color) {
        this.fadeRed = color.getRed() / 255.0f;
        this.fadeGreen = color.getGreen() / 255.0f;
        this.fadeBlue = color.getBlue() / 255.0f;
        this.fadeAlpha = color.getAlpha() / 255.0f;
    }

    public void copyToFade() {
        this.fadeRed = this.red;
        this.fadeGreen = this.green;
        this.fadeBlue = this.blue;
        this.fadeAlpha = this.alpha;
    }

    public void copyFromFade() {
        this.red = this.fadeRed;
        this.green = this.fadeGreen;
        this.blue = this.fadeBlue;
        this.alpha = this.fadeAlpha;
    }

    public void resetFade() {
        this.fadeRed = this.red;
        this.fadeGreen = this.green;
        this.fadeBlue = this.blue;
        this.fadeAlpha = this.alpha;
        this.offset = 0.5f;
        this.speed = 1.0f;
    }
}