package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(name = "FovModifier", category = Category.VISUAL, description = "Настройки фова.")
public class FovModifier extends Module {

    public IntegerSetting defaultFov = new IntegerSetting("DefaultFov", this,10,150,95);
    public IntegerSetting fovWithSpeed1 = new IntegerSetting("FovWithSpeed1", this,10,150,85);
    public IntegerSetting fovWithSpeed2 = new IntegerSetting("FovWithSpeed2", this,10,150,75);

    public CheckBox dynamicFov = new CheckBox("DynamicFov", this, false);

    public static float getFov() {
        FovModifier fovModifier = Modules.getModule(FovModifier.class);
        if (fovModifier == null || !fovModifier.isToggled()) return mc.gameSettings.fovSetting;

        float fov = fovModifier.defaultFov.getValue();
        int effectLvl = getSpeedEffectLvl();

        if (effectLvl == 0) fov = fovModifier.fovWithSpeed1.getValue();
        if (effectLvl == 1) fov = fovModifier.fovWithSpeed2.getValue();

        if (fovModifier.dynamicFov.isToggled()) {
            float dynamicFovEffect = mc.entityRenderer.fovModifierHandPrev + (mc.entityRenderer.fovModifierHand - mc.entityRenderer.fovModifierHandPrev) * mc.timer.renderPartialTicks;
            fov *= dynamicFovEffect;
        }

        return fov;
    }

    private static int getSpeedEffectLvl() {
        Potion speed = Potion.moveSpeed;
        if (speed == null) return -1;

        PotionEffect effect = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed);
        if (effect == null) return -1;

        return effect.getAmplifier();
    }
}
