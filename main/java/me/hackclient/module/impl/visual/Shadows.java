package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;

@ModuleInfo(
        name = "Shadows",
        category = Category.VISUAL
)
public class Shadows extends Module {

    public BooleanSetting clickGui = new BooleanSetting("ClickGui", this, true);
    public BooleanSetting console = new BooleanSetting("Console", this, true);
    public BooleanSetting mainMenu = new BooleanSetting("MainMenuButtons", this, true);
    //public BooleanSetting nameTags = new BooleanSetting("NameTags", this, true);

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 15, 6);

    public FloatSetting compression = new FloatSetting("Compression", this, 1f, 3f, 2f, 0.1f);

    public ColorSetting color = new ColorSetting("ShadowColor", this, 0,0,0,1);
}
