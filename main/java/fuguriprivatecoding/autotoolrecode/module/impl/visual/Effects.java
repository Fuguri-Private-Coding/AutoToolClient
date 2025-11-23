package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Effects", category = Category.VISUAL, description = "Показывает еффекты и воспроизводит звук при убийстве противника.")
public class Effects extends Module {

    CheckBox attackEffect = new CheckBox("AttackEffect", this, true);

    MultiMode attackEffects = new MultiMode("AttackEffects",this, () -> attackEffect.isToggled())
            .addModes("Sharpness", "Critical")
            ;

    IntegerSetting attackMultiplier = new IntegerSetting("AttackMultiplier", this, attackEffect::isToggled, 1, 5, 2);

    CheckBox effect = new CheckBox("Effect", this);

    Mode effects = new Mode("Effects", this, effect::isToggled)
            .addModes("Lightning")
            .setMode("Lightning")
            ;

    Entity target;
    EntityLightningBolt bolt;

    Vec3 targetPos;

    @Override
    public void onEvent(Event event) {
        if (event instanceof AttackEvent e) {
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (!rayCast.isFriend()) target = e.getHittingEntity();

            if (e.getHittingEntity() instanceof EntityPlayer entityPlayer) {
                if (attackEffect.isToggled()) {
                    for (int i = 0; i < attackMultiplier.getValue(); i++) {
                        if (attackEffects.get("Sharpness")) mc.thePlayer.onEnchantmentCritical(entityPlayer);
                        if (attackEffects.get("Critical")) mc.thePlayer.onCriticalHit(entityPlayer);
                    }
                }
            }
        }
        if (event instanceof TickEvent) {
            if (target != null) {
                if (effect.isToggled()) {
                    if (mc.theWorld.getLoadedEntityList().contains(target)) targetPos = new Vec3(target.posX,target.posY,target.posZ);
                    if (!mc.theWorld.getLoadedEntityList().contains(target)) {
                        if (effects.getMode().equals("Lightning")) {
                            bolt = new EntityLightningBolt(mc.theWorld, targetPos.xCoord, targetPos.yCoord, targetPos.zCoord);
                            bolt.setEntityId(-777);
                            mc.theWorld.addEntityToWorld(bolt.getEntityId(), bolt);
                            mc.theWorld.playSound(bolt.posX, bolt.posY, bolt.posZ, "ambient.weather.thunder", 1f, 1f, false);
                        }
                        target = null;
                    }
                }
            }
        }
    }
}
