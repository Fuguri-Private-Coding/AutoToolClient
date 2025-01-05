package me.hackclient.module.impl.combat;

import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;

@ModuleInfo(name = "AntiBot", category = Category.COMBAT)
public class AntiBot extends Module {

    BooleanSetting cancelHitsToBot = new BooleanSetting("cancelBotHit", this, true);

    public boolean isBot(Entity entity) {
        if (!isToggled()) {
            return false;
        }
        if (entity.getCustomNameTag().isEmpty()) {
            return true;
        }
        return false;
    }
}
