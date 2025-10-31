package fuguriprivatecoding.autotoolrecode.utils.interfaces;

import fuguriprivatecoding.autotoolrecode.setting.Setting;

import java.util.List;

public interface SettingAble {
    List<Setting> getSettings();
    void addSettings(Setting... settings);
    void addSetting(Setting setting);
}