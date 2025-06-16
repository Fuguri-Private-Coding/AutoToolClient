package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.Mode;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;

@ModuleInfo(name = "KillEffects", category = Category.VISUAL)
public class KillEffects extends Module {

    Mode effect = new Mode("Effect", this)
            .addModes("Lightning")
            .setMode("Lightning")
            ;

    int ticks;
    Entity target;

    EntityLightningBolt bolt;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof AttackEvent e) {
            target = e.getHittingEntity();
            ticks = 0;
        }
        if (event instanceof WorldChangeEvent) ticks = 0;
        if (event instanceof TickEvent) {
            if (target != null) {
                switch (effect.getMode()) {
                    case "Lightning" -> {
                        if (mc.theWorld.getLoadedEntityList().contains(target)) {
                            bolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                            bolt.setEntityId(-777);
                        }
                        if (!mc.theWorld.getLoadedEntityList().contains(target) && bolt != null) {
                            mc.theWorld.addEntityToWorld(bolt.getEntityId(), bolt);
                            mc.theWorld.playSound(bolt.posX,bolt.posY,bolt.posZ,"ambient.weather.thunder", 1f,1f,false);
                            target = null;
                        }
                    }
                }
            }
        }
    }
}
