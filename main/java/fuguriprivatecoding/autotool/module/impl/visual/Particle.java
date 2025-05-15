package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.AttackEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "Particle", category = Category.VISUAL)
public class Particle extends Module {

    IntegerSetting multiple = new IntegerSetting("Multiple", this, 1, 5, 2);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof AttackEvent attackEvent) {
            Entity target = attackEvent.getHittingEntity();
            if (target instanceof EntityPlayer) {
                for (int i = 0; i < multiple.getValue(); i++) {
                    mc.thePlayer.onEnchantmentCritical(target);
                }
            }
        }
    }
}
