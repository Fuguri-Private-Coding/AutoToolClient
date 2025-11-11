package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 8, 6, 0.1f);
    final FloatSetting rotateDistance = new FloatSetting("RotateDistance", this, 3, 8, 6, 0.1f);
    final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 8, 6f, 0.1f);

    final MultiMode targets = new MultiMode("Targets", this)
        .addModes("Players", "Mobs", "Animals", "Villagers");

    final Mode sortType = new Mode("SortType", this)
        .addModes("Distance", "FOV", "HurtTime")
        .setMode("FOV");

    final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Best");

    final BooleanSupplier hitBoxSizeVisible = () -> hitVec.getMode().equalsIgnoreCase("Best") || hitVec.getMode().equalsIgnoreCase("Nearest");
    final IntegerSetting horizontalHitBoxSize = new IntegerSetting("HorizontalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);
    final IntegerSetting verticalHitBoxSize = new IntegerSetting("VerticalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 180, 90, 1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0, 180, 90, 1);

    final CheckBox gcd = new CheckBox("GCDFix", this);
    final CheckBox smartAim = new CheckBox("SmartAim", this);

    final CheckBox teleportPredictFix = new CheckBox("TeleportPredictFix", this);

    final MultiMode smoothMode = new MultiMode("SmoothModes", this)
        .addModes("Linear", "Basic", "MixDelta");

    DoubleSlider mixYawDelta = new DoubleSlider("MixYawDelta", this, () -> smoothMode.get("MixDelta"), 0, 1, 1, 0.01f);
    DoubleSlider mixPitchDelta = new DoubleSlider("MixPitchDelta", this, () -> smoothMode.get("MixDelta"), 0, 1, 1, 0.01f);

    FloatSetting randomizeStrength = new FloatSetting("RandomizeStrength", this, () -> smoothMode.get("Basic"), 0, 20, 5, 0.1f);

    final FloatSetting linearSmoothStrength = new FloatSetting(
        "LinearSmoothStrength", this,
        () -> smoothMode.get("Linear"),
        1, 5, 1.5f, 0.1f
    );

    final CheckBox lockView = new CheckBox("LockView", this);

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 80, 16, 1);

    final Mode moveFix = new Mode("MoveFix", this)
        .addModes("OFF", "Legit", "Silent", "Target")
        .setMode("Silent");

    final StopWatch clickTimer = new StopWatch();
    private long delay;

    Rot lastDelta = new Rot();

    @Override
    public void onDisable() {
        TargetStorage.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) TargetStorage.setTarget(findNewTarget());
        EntityLivingBase target = TargetStorage.getTarget();
        if (Modules.getModule(Scaffold.class).isToggled() || target == null) return;

        if (event instanceof RunGameLoopEvent && DistanceUtils.getDistance(target) < clickDistance.getValue()) {
            if (TimerRange.balance == 0) {
                if (clickTimer.reachedMS(delay)) {
                    clickTimer.reset();
                    Clicks.addClick();
                    delay = Math.round(1000f / CPS.getRandomizedIntValue());
                }
            }
        }

        if (DistanceUtils.getDistance(target) < rotateDistance.getValue()) {
            Rot lr = Rot.getServerRotation();

            if (event instanceof TickEvent) {
                AxisAlignedBB box = RotUtils.getHitBox(
                    target,
                    (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled() ? 100 : horizontalHitBoxSize.getValue(),
                    (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled() ? 100 : verticalHitBoxSize.getValue()
                );
                Rot needRotation = getRotation(target, lr, box);

                if (needRotation == null) return;

                if (!mc.thePlayer.canVecBeSeen(RotUtils.getVectorForRotation(needRotation)) && smartAim.isToggled()) {
                    needRotation = RotUtils.getPossibleBestRotation(needRotation, box.expand(0.1f,0.1f,0.1f));
                }

                if ((TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled()) {
                    needRotation = getRotation(target, lr, box);
                }

                Rot delta = RotUtils.getDelta(lr, needRotation);

                Rot speed = new Rot(
                    (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled() ? 180 : yawSpeed.getRandomizedIntValue(),
                    (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled() ? 180 : pitchSpeed.getRandomizedIntValue()
                );

                if (smoothMode.get("Basic")) {
                    Rot rot = new Rot(
                        RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue()),
                        RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue())
                    );

                    delta.setYaw(MathHelper.wrapDegree(delta.getYaw() - rot.getYaw()));
                    delta.setPitch(MathHelper.wrapDegree(delta.getPitch() - rot.getPitch()));
                }

                if (smoothMode.get("Linear")) {
                    if ((TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled()) return;

                    delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / linearSmoothStrength.getValue()));
                    delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / linearSmoothStrength.getValue()));
                }

                RotUtils.limitDelta(delta, speed);

                if (smoothMode.get("MixDelta")) {
                    if (TimerRange.balance <= 0 && !TimerRange.teleporting) {
                        delta.setYaw(MathHelper.lerp((float) mixYawDelta.getRandomizedDoubleValue(), lastDelta.getYaw(), delta.getYaw()));
                        delta.setPitch(MathHelper.lerp((float) mixPitchDelta.getRandomizedDoubleValue(), lastDelta.getPitch(), delta.getPitch()));
                    }
                }

                lastDelta = new Rot(delta.getYaw(), delta.getPitch());

                if (gcd.isToggled()) delta = RotUtils.fixDelta(delta);
                lr = lr.add(delta);

                Rot.setServerRotation(lr);

                if (lockView.isToggled()) {
                    mc.thePlayer.rotationYaw = Rot.getServerRotation().getYaw();
                    mc.thePlayer.rotationPitch = Rot.getServerRotation().getPitch();
                }
            }

            if (event instanceof MotionEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());
            }

            if (event instanceof LookEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());
            }

            if (event instanceof ChangeHeadRotationEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());
            }

            if (event instanceof UpdateBodyRotationEvent e) {
                e.setYaw(lr.getYaw());
            }

            if (!moveFix.getMode().equalsIgnoreCase("OFF")) {
                if (event instanceof MoveFlyingEvent e) e.setYaw(lr.getYaw());
                if (event instanceof JumpEvent e) e.setYaw(lr.getYaw());
            }

            if (event instanceof MoveEvent e) {
                switch (moveFix.getMode()) {
                    case "Silent" -> MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                    case "Target" -> MoveUtils.moveFix(e, RotUtils.getRotationToPoint(target.getPositionVector()).getYaw());
                }
            }
        }
    }

    private Rot getRotation(EntityLivingBase target, Rot lr, AxisAlignedBB box) {
        return (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled() ?
            RotUtils.getBestRotation(box.expand(0.1f,0.1f,0.1f)) :
            switch (hitVec.getMode()) {
                case "Best" -> RotUtils.getBestRotation(box.expand(0.1f,0.1f,0.1f));
                case "Nearest" -> RotUtils.getNearestRotations(lr, box);
                case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
                case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                default -> throw new IllegalStateException("Unexpected value: " + hitVec.getMode());
            };
    }

    private EntityLivingBase findNewTarget() {
        List<EntityLivingBase> entityList = new ArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity != mc.thePlayer && DistanceUtils.getDistance(entity) < findDistance.getValue()) {
                switch (entity) {
                    case EntityPlayer entityPlayer when targets.get("Players") && !entityPlayer.isFriend() && !entityPlayer.isTeam() -> entityList.add(entityPlayer);
                    case EntityMob entityMob when targets.get("Mobs") -> entityList.add(entityMob);
                    case EntityAnimal entityAnimal when targets.get("Animals") -> entityList.add(entityAnimal);
                    case EntityVillager entityVillager when targets.get("Villagers") -> entityList.add(entityVillager);

                    default -> {}
                }
            }
        }

        entityList.removeIf(ent -> DistanceUtils.getDistance(ent) > findDistance.getValue());

        switch (sortType.getMode()) {
            case "Distance" -> entityList.sort(Comparator.comparingDouble(DistanceUtils::getDistance));
            case "FOV" -> entityList.sort(Comparator.comparingDouble(RotUtils::getFovToEntity));
            case "HurtTime" -> entityList.sort(Comparator.comparingDouble(ent -> ent.hurtTime));
        }

        return entityList.getFirst();
    }
}