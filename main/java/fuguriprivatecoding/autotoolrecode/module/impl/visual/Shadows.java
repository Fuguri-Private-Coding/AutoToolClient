package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;

@ModuleInfo(name = "Shadows", category = Category.VISUAL)
public class Shadows extends Module {

    public MultiMode module = new MultiMode("Modules", this)
            .addModes("ClickGui","ConsoleGui","MainMenu","ItemsFirstPerson","ArrayList",
                    "TargetESP","Dot" ,"BreakIndicator", "CPSCounter", "BPSCounter",
                    "FPSCounter", "BlockOverlay", "ConfigGui", "Chat", "Trails",
                    "NameTags", "Scaffold", "ChestESP", "BedESP", "WorldParticles",
                    "Weather", "TargetHUD"
            );

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 35, 6);
    public FloatSetting brightness = new FloatSetting("Brightness", this, 0,3,1,0.1f);
    public FloatSetting offset1 = new FloatSetting("Offset1", this, 1,5,1,0.1f);
    public FloatSetting offset2 = new FloatSetting("Offset2", this, 1,5,1,0.1f);

    public CheckBox fade = new CheckBox("Fade", this, false);
    public ColorSetting color = new ColorSetting("ShadowColor", this, 0,0,0,1);
    public ColorSetting twoColor = new ColorSetting("TwoShadowColor", this,() -> fade.isToggled(), 0,0,0,1);
    public FloatSetting speed = new FloatSetting("Speed", this,() -> fade.isToggled(),0.1f, 20, 1, 0.1f);
}