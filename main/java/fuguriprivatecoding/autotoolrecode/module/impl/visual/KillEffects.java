package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "KillEffects", category = Category.VISUAL)
public class KillEffects extends Module {

    CheckBox effect = new CheckBox("Effect", this);

    Mode effects = new Mode("Effects", this, effect::isToggled)
            .addModes("Lightning")
            .setMode("Lightning")
            ;

    CheckBox sound = new CheckBox("Sound", this);
    Mode sounds = new Mode("Sounds", this, sound::isToggled)
            .addModes("NeverLose", "Skeet", "HalfLife")
            .setMode("HalfLife")
            ;

    FloatSetting volume = new FloatSetting("Volume",this, sound::isToggled, 0f,1f,1f,0.1f);

    Entity target;
    EntityLightningBolt bolt;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof AttackEvent e) {
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (!rayCast.isFriend()) target = e.getHittingEntity();
        }
        if (event instanceof TickEvent) {
            if (target != null) {
                if (effect.isToggled()) {
                    switch (effects.getMode()) {
                        case "Lightning" -> {
                            if (mc.theWorld.getLoadedEntityList().contains(target)) {
                                bolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                                bolt.setEntityId(-777);
                            }
                            if (!mc.theWorld.getLoadedEntityList().contains(target) && bolt != null) {
                                mc.theWorld.addEntityToWorld(bolt.getEntityId(), bolt);
                                mc.theWorld.playSound(bolt.posX, bolt.posY, bolt.posZ, "ambient.weather.thunder", 1f, 1f, false);
                                target = null;
                            }
                        }
                        case "TNT" -> {

                        }
                    }
                }

                if (sound.isToggled()) {
                    switch (sounds.getMode()) {
                        case "Skeet" -> {
                            if (!mc.theWorld.getLoadedEntityList().contains(target) && bolt != null) {
                                Client.INST.getSoundsManager().getSkeetSound().asyncPlay(volume.getValue());
                                target = null;
                            }
                        }

                        case "NeverLose" -> {
                            if (!mc.theWorld.getLoadedEntityList().contains(target) && bolt != null) {
                                Client.INST.getSoundsManager().getNeverLoseSound().asyncPlay(volume.getValue());
                                target = null;
                            }
                        }

                        case "HalfLife" -> {
                            if (!mc.theWorld.getLoadedEntityList().contains(target) && bolt != null) {
                                Client.INST.getSoundsManager().getKilledSound().asyncPlay(volume.getValue());
                                target = null;
                            }
                        }
                    }
                }
            }
        }
    }
}
