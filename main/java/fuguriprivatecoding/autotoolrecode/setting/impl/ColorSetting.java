package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
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
        this.offset = 1f;
        this.speed = 1.0f;
        this.fade = false;
        this.hide = false;
    }

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
        this.offset = 1f;
        this.speed = 1.0f;
        this.fade = false;
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
//
//    @Override
//    public void render() {
//        if (ImGui.collapsingHeader(getName())) {
//            ImGui.pushID(hashCode());
//            ImGui.indent();
//            ImGui.separator();
//
//
//            if (ImGui.checkbox("Fade", fade)) {
//                fade = !fade;
//            }
//
//            float[] color = new float[] { red, green, blue, alpha };
//            float[] color2 = new float[] { fadeRed, fadeGreen, fadeBlue, fadeAlpha };
//
//            if (ImGui.colorEdit4("First color", color)) {
//                red = color[0];
//                green = color[1];
//                blue = color[2];
//                alpha = color[3];
//            }
//
//            if (fade) {
//                if (ImGui.colorEdit4("Second color", color2)) {
//                    fadeRed = color2[0];
//                    fadeGreen = color2[1];
//                    fadeBlue = color2[2];
//                    fadeAlpha = color2[3];
//                }
//
//                float[] speedV = new float[] { speed };
//                if (ImGui.sliderFloat("Speed: ", speedV, 0, 20, "%.1f")) {
//                    speed = speedV[0];
//                }
//
//                float[] offsetV = new float[] { offset };
//                if (ImGui.sliderFloat("Offset: ", offsetV, 0, 20, "%.1f")) {
//                    offset = offsetV[0];
//                }
//            }
//            ImGui.unindent();
//            ImGui.popID();
//        }
//    }

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