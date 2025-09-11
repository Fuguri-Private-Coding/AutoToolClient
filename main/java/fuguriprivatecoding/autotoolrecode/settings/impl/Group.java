package fuguriprivatecoding.autotoolrecode.settings.impl;

import fuguriprivatecoding.autotoolrecode.settings.Setting;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.SettingAble;
import imgui.ImGui;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Group extends Setting {
    private final List<Setting> settings = new ArrayList<>();

    public Group(String name, SettingAble parent) {
        super(name, parent);
    }

    public Group settings(Setting... settings) {
        this.settings.addAll(List.of(settings));
        return this;
    }

    @Override
    public void render() {
        ImGui.pushID(hashCode());
        if (ImGui.collapsingHeader(getName())) {
            ImGui.indent();
            for (Setting setting : settings) {
                if (setting.isVisible()) {
                    setting.render();
                }
            }
            ImGui.unindent();
        }
        ImGui.popID();
    }
}