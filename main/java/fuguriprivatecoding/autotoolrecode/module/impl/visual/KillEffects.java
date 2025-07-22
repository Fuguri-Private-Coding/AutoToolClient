package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import Effekseer.installer.Loader;
import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "KillEffects", category = Category.VISUAL)
public class KillEffects extends Module {

    CheckBox effect = new CheckBox("Effect", this);

    Mode effects = new Mode("Effects", this, effect::isToggled)
            .addModes("Lightning", "Sacred", "Ember", "MinecraftLightning")
            .setMode("MinecraftLightning")
            ;

    CheckBox sound = new CheckBox("Sound", this);
    Mode sounds = new Mode("Sounds", this, sound::isToggled)
            .addModes("NeverLose", "Skeet", "HalfLife")
            .setMode("HalfLife")
            ;

    FloatSetting volume = new FloatSetting("Volume",this, sound::isToggled, 0f,1f,1f,0.1f);

    Entity target;
    EntityLightningBolt bolt;

    EffekseerEffectCore effectEmber;
    EffekseerEffectCore effectSacred;
    EffekseerEffectCore effectLightning;

    Vec3 targetPos;
    int effectHandle;

    public KillEffects() {
        effectEmber = Loader.loadEffect("killEffects/Ember.efkefc", 0.2f);
        effectSacred = Loader.loadEffect("killEffects/Sacred.efkefc", 0.2f);
        effectLightning = Loader.loadEffect("killEffects/lightning/lightning.efkefc", 0.2f);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof AttackEvent e) {
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (!rayCast.isFriend()) target = e.getHittingEntity();
        }
        if (event instanceof TickEvent) {
            if (target != null) {
                if (effect.isToggled()) {
                    EffekseerManagerCore effekseerManagerCore = Client.INST.getLoadNatives().getEffekseerManagerCore();

                    if (mc.theWorld.getLoadedEntityList().contains(target)) targetPos = new Vec3(target.posX,target.posY,target.posZ);
                    if (!mc.theWorld.getLoadedEntityList().contains(target)) {
                        switch (effects.getMode()) {
                            case "MinecraftLightning" -> {
                                bolt = new EntityLightningBolt(mc.theWorld, targetPos.xCoord, targetPos.yCoord, targetPos.zCoord);
                                bolt.setEntityId(-777);
                                mc.theWorld.addEntityToWorld(bolt.getEntityId(), bolt);
                                mc.theWorld.playSound(bolt.posX, bolt.posY, bolt.posZ, "ambient.weather.thunder", 1f, 1f, false);
                            }
                            case "Lightning" -> effectHandle = effekseerManagerCore.Play(effectLightning);
                            case "Sacred" -> effectHandle = effekseerManagerCore.Play(effectSacred);
                            case "Ember" -> effectHandle = effekseerManagerCore.Play(effectEmber);
                        }
                        effekseerManagerCore.SetEffectPosition(effectHandle, targetPos);
                        if (sound.isToggled()) {
                            switch (sounds.getMode()) {
                                case "Skeet" -> Client.INST.getSoundsManager().getSkeetSound().asyncPlay(volume.getValue());
                                case "NeverLose" -> Client.INST.getSoundsManager().getNeverLoseSound().asyncPlay(volume.getValue());
                                case "HalfLife" -> Client.INST.getSoundsManager().getKilledSound().asyncPlay(volume.getValue());
                            }
                        }
                        target = null;
                    }
                }


            }
        }
    }
}
