package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;

@ModuleInfo(
        name = "CustomScoreboard",
        category = Category.VISUAL
)
public class CustomScoreboard extends Module {

    public final BooleanSetting remove = new BooleanSetting("Remove", this, true);
    public final FloatSetting scale = new FloatSetting("Scale", this, 0f, 2f, 1f, 0.1f);

}
