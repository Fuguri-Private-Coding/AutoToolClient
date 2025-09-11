package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import imgui.ImGui;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MultiMode extends Setting {

    CopyOnWriteArrayList<Doubles<String, Boolean>> values;

    private final Map<String, Float> modeProgress = new HashMap<>();
    private final float animationSpeed = 0.15f;
    private Color selectedColor = new Color(1f,1f,1f,1f);
    private Color unselectedColor = new Color(1f,1f,1f,1f);

    public MultiMode(String name, SettingAble parent) {
        super(name, parent);
        values = new CopyOnWriteArrayList<>();
    }

    public MultiMode(String name, SettingAble parent, BooleanSupplier visible) {
        super(name, parent);
        setVisible(visible);
        values = new CopyOnWriteArrayList<>();
    }

    public MultiMode add(String name, boolean value) {
        values.add(new Doubles<>(name, value));
        modeProgress.put(name, value ? 1.0f : 0.0f);
        return this;
    }

    public MultiMode add(String name) {
        values.add(new Doubles<>(name, false));
        modeProgress.put(name, 0.0f);
        return this;
    }

    public MultiMode addModes(String... modes) {
        for (String mode : modes) {
            values.add(new Doubles<>(mode, false));
            modeProgress.put(mode, 0.0f);
        }
        return this;
    }

    public boolean get(String name) {
        Doubles<String, Boolean> value = values.stream().filter(v -> v.getFirst().equals(name))
                .findFirst().orElse(null);

        if (value == null) {
            throw new IllegalStateException();
        }

        return value.getSecond();
    }

    public void set(String name, boolean newValue) {
        for (Doubles<String, Boolean> value : values) {
            if (value == null) {
                throw new IllegalStateException();
            }

            if (!value.getFirst().equals(name))
                continue;

            value.setSecond(newValue);
            break;
        }
    }

    public void updateAnimation() {
        for (Doubles<String, Boolean> value : values) {
            String mode = value.getFirst();
            boolean isSelected = value.getSecond();

            float targetProgress = isSelected ? 1.0f : 0.0f;
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

    public Color getModeColor(Doubles<String, Boolean> value) {
        return getModeColor(value.getFirst());
    }

    @Override
    public void render() {
        ImGui.pushID(hashCode());
        ImGui.text(getName());
        ImGui.sameLine();
        if (ImGui.button("Select Modes")) { // Кнопка для открытия popup
            ImGui.openPopup("multiModePopup");
        }

        if (ImGui.beginPopup("multiModePopup")) { // Popup с чекбоксами
            for (Doubles<String, Boolean> mode : values) {
                if (ImGui.checkbox(mode.getFirst(), mode.getSecond())) mode.setSecond(!mode.getSecond());
            }
            ImGui.endPopup();
        }

        StringBuilder current = new StringBuilder();

        List<Doubles<String, Boolean>> toggledModes = new ArrayList<>();

        for (Doubles<String, Boolean> mode : values) {
            if (mode.getSecond()) {
                toggledModes.add(mode);
            }
        }

        for (Doubles<String, Boolean> toggledMode : toggledModes) {
            current.append(toggledMode.getFirst());
            if (toggledMode != toggledModes.getLast()) {
                current.append(", ");
            }
        }

        ImGui.sameLine();
        ImGui.text(current.toString());
        ImGui.popID();
    }
}