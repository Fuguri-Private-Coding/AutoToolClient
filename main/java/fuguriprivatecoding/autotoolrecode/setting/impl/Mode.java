package fuguriprivatecoding.autotoolrecode.setting.impl;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import lombok.Getter;
import lombok.Setter;
import fuguriprivatecoding.autotoolrecode.setting.Setting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Getter
@Setter
public class Mode extends Setting {

    String mode;
    List<String> modes = new ArrayList<>();

    private final Map<String, Float> modeProgress = new HashMap<>();
    private final float animationSpeed = 0.15f;
    private Color selectedColor = new Color(76, 175, 80);
    private Color unselectedColor = new Color(150, 150, 150);

    public Mode(String name, SettingAble parent) {
        super(name, parent);
        initAnimation();
    }

    public Mode(String name, SettingAble parent, BooleanSupplier visible) {
        super(name, parent);
        this.setVisible(visible);
        initAnimation();
    }

    @Override
    public JsonObject getObject() {
        JsonObject object = new JsonObject();

        object.addProperty("mode", mode);

        return object;
    }

    @Override
    public void setObject(JsonObject object) {
        setMode(object.get("mode").getAsString());
    }

    private void initAnimation() {
        for (String mode : modes) {
            modeProgress.put(mode, mode.equals(this.mode) ? 1.0f : 0.0f);
        }
    }

    public Mode setMode(String mode) {
        if (!modes.contains(mode)) {
            System.out.println("Cant set mode " + mode);
            return this;
        }
        this.mode = mode;
        return this;
    }

    public Mode addMode(String mode) {
        modes.add(mode);
        modeProgress.put(mode, mode.equals(this.mode) ? 1.0f : 0.0f);
        return this;
    }

    public Mode addModes(String... modes) {
        for (String m : modes) {
            this.modes.add(m);
            modeProgress.put(m, m.equals(this.mode) ? 1.0f : 0.0f);
        }
        return this;
    }

    public void updateAnimation() {
        for (String mode : modes) {
            float targetProgress = mode.equals(this.mode) ? 1.0f : 0.0f;
            float currentProgress = modeProgress.getOrDefault(mode, 0.0f);

            float newProgress = currentProgress + (targetProgress - currentProgress) * animationSpeed;

            if (Math.abs(newProgress - targetProgress) < 0.01f) {
                newProgress = targetProgress;
            }

            modeProgress.put(mode, newProgress);
        }
    }

    public Color getModeColor(String mode) {
        float progress = modeProgress.getOrDefault(mode, 0.0f);
        return ColorUtils.interpolateColor(unselectedColor, selectedColor, progress);
    }

    public boolean is(String mode) {
        return this.mode.equalsIgnoreCase(mode);
    }
}