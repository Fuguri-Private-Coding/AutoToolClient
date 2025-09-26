package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import Effekseer.installer.Loader;
import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.AttackEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;

@ModuleInfo(name = "Effects", category = Category.VISUAL, description = "Показывает еффекты и воспроизводит звук при убийстве противника.")
public class Effects extends Module {

    CheckBox attackEffect = new CheckBox("AttackEffect", this, true);

    MultiMode attackEffects = new MultiMode("AttackEffects",this, () -> attackEffect.isToggled())
            .addModes("Sharpness", "Critical")
            ;

    IntegerSetting attackMultiplier = new IntegerSetting("AttackMultiplier", this, attackEffect::isToggled, 1, 5, 2);


    CheckBox customAttackEffect = new CheckBox("CustomAttackEffect", this, attackEffect::isToggled, true);

    BooleanSupplier customAttackEffectBool = () -> customAttackEffect.isToggled() && attackEffect.isToggled();

    Mode customAttackEffects = new Mode("CustomAttackEffects", this, customAttackEffectBool)
        .addModes("Firefly","Heart","Snow","Star")
        .setMode("Snow")
        ;

    IntegerSetting count = new IntegerSetting("Count", this,customAttackEffectBool, 1,60,5);

    FloatSetting friction = new FloatSetting("Friction", this,customAttackEffectBool, 0, 1f,0.02f,0.01f);
    FloatSetting gravity = new FloatSetting("Gravity", this,customAttackEffectBool, 0, 1f,0.02f,0.01f);
    DoubleSlider initialSpeed = new DoubleSlider("InitialSpeed", this,customAttackEffectBool, 0, 1f,0.02f,0.01f);
    FloatSetting particleSize = new FloatSetting("ParticleSize", this, customAttackEffectBool, 0.1f,50f, 0.25f,0.01f);
    ColorSetting particleColor = new ColorSetting("ParticleColor", this, customAttackEffectBool);
    @Getter IntegerSetting lifeTime = new IntegerSetting("LifeTime", this,customAttackEffectBool, 100,5000,500);

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

    List<Particle> particleList = new CopyOnWriteArrayList<>();

    Vec3 targetPos;
    int effectHandle;

    public Effects() {
        effectEmber = Loader.loadEffect("killEffects/Ember.efkefc", 0.2f);
        effectSacred = Loader.loadEffect("killEffects/Sacred.efkefc", 0.2f);
        effectLightning = Loader.loadEffect("killEffects/lightning/lightning.efkefc", 0.2f);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof AttackEvent e) {
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);
            if (!rayCast.isFriend()) target = e.getHittingEntity();

            if (e.getHittingEntity() instanceof EntityPlayer entityPlayer) {
                if (attackEffect.isToggled()) {
                    if (attackEffects.get("Sharpness")) {
                        for (int i = 0; i < attackMultiplier.getValue(); i++) {
                            mc.thePlayer.onEnchantmentCritical(entityPlayer);
                        }
                    }
                    if (attackEffects.get("Critical")) {
                        for (int i = 0; i < attackMultiplier.getValue(); i++) {
                            mc.thePlayer.onCriticalHit(entityPlayer);
                        }
                    }

                    if (customAttackEffectBool.getAsBoolean()) {
                        Vec3 hit = mc.objectMouseOver.hitVec;
                        for (int i = 0; i < count.getValue(); i++) {
                            particleList.add(
                                new Particle(
                                    this,
                                    hit.xCoord, hit.yCoord, hit.zCoord,
                                    (RandomUtils.nextDouble(initialSpeed.getMinValue(), initialSpeed.getMaxValue())) * MathUtils.rs(),
                                    (RandomUtils.nextDouble(initialSpeed.getMinValue(), initialSpeed.getMaxValue())) * MathUtils.rs(),
                                    (RandomUtils.nextDouble(initialSpeed.getMinValue(), initialSpeed.getMaxValue())) * MathUtils.rs()
                                )
                            );
                        }
                    }

                }
            }
        }
        if (event instanceof TickEvent) {
            if (customAttackEffectBool.getAsBoolean()) {
                particleList.forEach(particle -> {
                    particle.updateMotions(friction.getValue(), gravity.getValue());
                    particle.updatePoses();
                });

                particleList.removeIf(Particle::needRemove);
            }

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

        if (event instanceof Render3DEvent && customAttackEffectBool.getAsBoolean()) {
            glPushAttrib(GL_ENABLE_BIT);
            glPushMatrix();
            glEnable(GL_LIGHTING);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);

            for (Particle particle : particleList) {
                glPushMatrix();
                double x = particle.prevX + (particle.x - particle.prevX) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosX;
                double y = particle.prevY + (particle.y - particle.prevY) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosY;
                double z = particle.prevZ + (particle.z - particle.prevZ) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosZ;

                glTranslated(x, y, z);
                glNormal3f(0.0f, 1.0f, 0.0f);
                glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
                glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
                glScalef(-1f, -1f, 1f);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);

                ResourceLocation image = new ResourceLocation("minecraft", "autotool/image/" + customAttackEffects.getMode().toLowerCase() + ".png");

                ColorUtils.glColor(particleColor.getMixedColor(particleList.indexOf(particle)), 1f - (float) (System.currentTimeMillis() - particle.createdTime) / lifeTime.getValue());
                RenderUtils.drawImage(image,-particleSize.getValue() / 2f,-particleSize.getValue() / 2f,particleSize.getValue(),particleSize.getValue(), true);
                glPopMatrix();
            }

            glDisable(GL_BLEND);
            glDisable(GL_LINE_SMOOTH);
            glPopMatrix();
            glPopAttrib();
            ColorUtils.resetColor();
        }
    }

    @Getter
    private static class Particle {
        final Effects parent;

        final long createdTime = System.currentTimeMillis();
        double x,y,z;
        double prevX,prevY,prevZ;
        double motionX,motionY,motionZ;

        public Particle(Effects parent, double x, double y, double z, double motionX, double motionY, double motionZ) {
            this.parent = parent;
            this.x = x;
            this.y = y;
            this.z = z;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
        }

        public void updateMotions(double friction, double gravity) {
            double multiplier = 1 - Math.min(friction, 1);

            motionX *= multiplier;
            motionY -= gravity;
            motionZ *= multiplier;
        }

        public void updatePoses() {
            prevX = x;
            prevY = y;
            prevZ = z;

            x += motionX;
            y += motionY;
            z += motionZ;
        }

        public boolean needRemove() {
            return (System.currentTimeMillis() - createdTime >= parent.getLifeTime().getValue());
        }

    }
}
