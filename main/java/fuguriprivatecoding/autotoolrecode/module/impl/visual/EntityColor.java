package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import java.awt.*;

@ModuleInfo(name = "EntityColor", category = Category.VISUAL, description = "Изменяет цвета игроков.")
public class EntityColor extends Module {
    public ColorSetting entityColor = new ColorSetting("EntityColor", this);
    public ColorSetting entityHurtColor = new ColorSetting("EntityHurtColor", this);

    public static Color getEntityColor(int hurtTime) {
        EntityColor entColor = Modules.getModule(EntityColor.class);
        return ColorUtils.interpolateColor(entColor.entityColor.getFadedColor(), entColor.entityHurtColor.getFadedColor(), hurtTime / 10f);
    }
}
