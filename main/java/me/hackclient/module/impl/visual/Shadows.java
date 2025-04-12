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
    public BooleanSetting mainMenu = new BooleanSetting("MainMenu", this, true);
    public BooleanSetting item = new BooleanSetting("ItemsFirstPerson", this, true);
    public BooleanSetting arrayList = new BooleanSetting("ArrayList", this, true);
    public BooleanSetting scaffold = new BooleanSetting("Scaffold", this, true);
    public BooleanSetting targetEsp = new BooleanSetting("TargetESP", this, true);
    public BooleanSetting dot = new BooleanSetting("Dot", this, true);
    public BooleanSetting nameTags = new BooleanSetting("NameTags", this, true);
    public BooleanSetting trails = new BooleanSetting("Trails", this, true);
    public BooleanSetting breakIndicator = new BooleanSetting("BreakIndicator", this, true);
    public BooleanSetting cpsCounter = new BooleanSetting("CPSCounter", this, true);
    public BooleanSetting bpsCounter = new BooleanSetting("BPSCounter", this, true);
    //public BooleanSetting nameTags = new BooleanSetting("NameTags", this, true);

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 15, 6);
    public FloatSetting brightness = new FloatSetting("Brightness", this, 0,2,1,0.1f);
    public FloatSetting horizontal1Compress = new FloatSetting("Horizontal1Compress", this, -10, 10, 2, 0.1f);
    public FloatSetting vertical1Compress = new FloatSetting("Vertical1Compress", this, -10, 10, 0, 0.1f);
    public FloatSetting vertical2Compress = new FloatSetting("Vertical2Compress", this, -10, 10, 2, 0.1f);

    public BooleanSetting fade = new BooleanSetting("Fade", this, false);
    public ColorSetting color = new ColorSetting("ShadowColor", this, 0,0,0,1);
    public ColorSetting twoColor = new ColorSetting("TwoShadowColor", this, fade::isToggled, 0,0,0,1);
    public FloatSetting speed = new FloatSetting("Speed", this, fade::isToggled,0.1f, 20, 1, 0.1f);
}
