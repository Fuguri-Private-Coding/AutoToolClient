package me.hackclient.module.impl.visual;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;

@ModuleInfo(name = "Shadows", category = Category.VISUAL)
public class Shadows extends Module {

    public MultiBooleanSetting module = new MultiBooleanSetting("Modules", this)
            .add("ClickGui")
            .add("ConsoleGui")
            .add("MainMenuGui")
            .add("ItemsFirstPerson")
            .add("ArrayList")
            .add("TargetESP")
            .add("Dot")
            .add("BreakIndicator")
            .add("CPSCounter")
            .add("BPSCounter")
            .add("PREMIUMFPSCOUNTER")
            .add("BlockOverlay")
            .add("ConfigGui")
            .add("Chat")
            .add("Trails")
            .add("NameTags")
            .add("Scaffold")
            ;

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 35, 6);
    public FloatSetting brightness = new FloatSetting("Brightness", this, 0,3,1,0.1f);
    public FloatSetting horizontal1Compress = new FloatSetting("Horizontal1Compress", this, 0, 10, 2, 0.1f);
    public FloatSetting vertical1Compress = new FloatSetting("Vertical1Compress", this, 0, 10, 0, 0.1f);
    public FloatSetting vertical2Compress = new FloatSetting("Vertical2Compress", this, 0, 10, 2, 0.1f);

    public BooleanSetting fade = new BooleanSetting("Fade", this, false);
    public ColorSetting color = new ColorSetting("ShadowColor", this, 0,0,0,1);
    public ColorSetting twoColor = new ColorSetting("TwoShadowColor", this, fade::isToggled, 0,0,0,1);
    public FloatSetting speed = new FloatSetting("Speed", this, fade::isToggled,0.1f, 20, 1, 0.1f);
}