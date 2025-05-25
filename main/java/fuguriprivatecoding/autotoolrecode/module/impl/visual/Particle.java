package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
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
