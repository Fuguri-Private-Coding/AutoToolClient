package me.hackclient.module.impl.combat;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;

@ModuleInfo(
        name = "ClickSettings",
        category = Category.COMBAT,
        toggled = true
)
public class ClickSettings extends Module {


    public final IntegerSetting maxStartHurtTime = new IntegerSetting("MaxStartHurtTime", this, 0, 10, 3);
    public final IntegerSetting minStartHurtTime = new IntegerSetting("MinStartHurtTime", this, 0, 10, 3);
    public final IntegerSetting maxEndHurtTime = new IntegerSetting("MaxEndHurtTime", this, 0, 10, 7);
    public final IntegerSetting minEndHurtTime = new IntegerSetting("MinEndHurtTime", this, 0, 10, 7);

    @Override
    public boolean isToggled() { return true; }
}
