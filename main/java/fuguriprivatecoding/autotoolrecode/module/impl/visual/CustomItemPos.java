package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;

@ModuleInfo(name = "CustomItemPos", category = Category.VISUAL, description = "Позволяет двигать предмет в руке.")
public class CustomItemPos extends Module {

    public FloatSetting x = new FloatSetting("X", this, -2,2,0,0.01f);
    public FloatSetting y = new FloatSetting("Y", this, -2,2,0,0.01f);
    public FloatSetting z = new FloatSetting("Z", this, -2,2,0,0.01f);

    public FloatSetting rotateX = new FloatSetting("RotateX", this, -90,90,0,0.1f);
    public FloatSetting rotateY = new FloatSetting("RotateY", this, -90,90,0,0.1f);
    public FloatSetting rotateZ = new FloatSetting("RotateZ", this, -90,90,0,0.1f);

    public FloatSetting blockX = new FloatSetting("BlockX", this, -2,2,0,0.01f);
    public FloatSetting blockY = new FloatSetting("BlockY", this, -2,2,-0.1f,0.01f);
    public FloatSetting blockZ = new FloatSetting("BlockZ", this, -2,2,-0.2f,0.01f);

    public FloatSetting blockRotateX = new FloatSetting("BlockRotateX", this, -90,90,0,0.1f);
    public FloatSetting blockRotateY = new FloatSetting("BlockRotateY", this, -90,90,0,0.1f);
    public FloatSetting blockRotateZ = new FloatSetting("BlockRotateZ", this, -90,90,0,0.1f);

}
