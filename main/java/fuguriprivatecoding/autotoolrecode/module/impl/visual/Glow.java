package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;

@ModuleInfo(name = "Glow", category = Category.VISUAL, description = "Бесплатное свечение скачать.")
public class Glow extends Module {

    public MultiMode module = new MultiMode("Modules", this)
        .addModes("ItemsFirstPerson", "Chat", "Weather", "BackTrack", "Ping");

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 35, 6);
    public FloatSetting brightness = new FloatSetting("Brightness", this, 0,3,1,0.1f);
    public FloatSetting offset1 = new FloatSetting("Offset1", this, 1,5,1,0.1f);
    public FloatSetting offset2 = new FloatSetting("Offset2", this, 1,5,1,0.1f);

    public final ColorSetting color = new ColorSetting("Color", this);
}