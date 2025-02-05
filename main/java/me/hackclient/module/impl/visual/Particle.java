package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.entity.Entity;

@ModuleInfo(
        name = "Particle",
        category = Category.VISUAL
)
public class Particle extends Module {

    IntegerSetting multiple = new IntegerSetting("Multiple", this, 1, 5, 2);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof AttackEvent attackEvent) {
            Entity target = attackEvent.getHittingEntity();
            for (int i = 0; i < multiple.getValue(); i++) {
                mc.thePlayer.onEnchantmentCritical(target);
            }
        }
    }
}
