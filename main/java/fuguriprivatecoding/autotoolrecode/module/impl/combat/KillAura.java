package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
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

    final CheckBox smartAim = new CheckBox("SmartAim", this);

    final CheckBox teleportPredictFix = new CheckBox("TeleportPredictFix", this);

    final MultiMode smoothMode = new MultiMode("SmoothModes", this)
        .addModes("Linear", "Basic", "MixDelta", "ReactionTime");

    DoubleSlider mixYawDelta = new DoubleSlider("MixYawDelta", this, () -> smoothMode.get("MixDelta"), 0, 1, 1, 0.01f);
    DoubleSlider mixPitchDelta = new DoubleSlider("MixPitchDelta", this, () -> smoothMode.get("MixDelta"), 0, 1, 1, 0.01f);

    FloatSetting randomizeStrength = new FloatSetting("RandomizeStrength", this, () -> smoothMode.get("Basic"), 0, 20, 5, 0.1f);
    FloatSetting reactionTime = new FloatSetting("ReactionTime", this, () -> smoothMode.get("ReactionTime"), 0, 5, 1, 0.1f);

    final FloatSetting linearSmoothStrength = new FloatSetting(
        "LinearSmoothStrength", this,
        () -> smoothMode.get("Linear"),
        1, 5, 1.5f, 0.1f
    );

    DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 80, 16, 1);

    final Mode moveFix = new Mode("MoveFix", this)
        .addModes("OFF", "Legit", "Silent")
        .setMode("Silent");

    final StopWatch clickTimer = new StopWatch();
    private long delay;

    boolean startSlowRotation;

    Rot lastDelta = new Rot();

    @Override
    public void onDisable() {
        CameraRot.INST.setWillChange(false);
        TargetStorage.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) TargetStorage.setTarget(findNewTarget());
        EntityLivingBase target = TargetStorage.getTarget();
        if (Modules.getModule(Scaffold.class).isToggled()) return;

        if (target != null) {
            if (event instanceof RunGameLoopEvent && DistanceUtils.getDistance(target) < clickDistance.getValue()) {
                if (TimerRange.balance == 0) {
                    if (clickTimer.reachedMS(delay)) {
                        clickTimer.reset();
                        Clicks.addClick();
                        delay = Math.round(1000f / CPS.getRandomizedIntValue());
                    }
                }
            }

            if (event instanceof TickEvent) {
                Rot lr = mc.thePlayer.getRotation();

                boolean teleport = (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled();

                AxisAlignedBB box = RotUtils.getHitBox(
                    target,
                    teleport ? 100 : horizontalHitBoxSize.getValue(),
                    teleport ? 100 : verticalHitBoxSize.getValue()
                ).expand(0.1D, 0.1D, 0.1D);

                Rot needRotation = getRotation(target, lr, box);

                if (needRotation == null) return;

                Rot delta = RotUtils.getDelta(lr, needRotation);

                Rot speed = new Rot(
                    yawSpeed.getRandomizedIntValue(),
                    pitchSpeed.getRandomizedIntValue()
                );

                if (!teleport) {
                    if (smoothMode.get("Basic")) {
                        Rot rot = new Rot(
                            RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue()),
                            RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue())
                        );

                        delta.setYaw(MathHelper.wrapDegree(delta.getYaw() - rot.getYaw()));
                        delta.setPitch(MathHelper.wrapDegree(delta.getPitch() - rot.getPitch()));
                    }

                    if (smoothMode.get("Linear")) {
                        delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / linearSmoothStrength.getValue()));
                        delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / linearSmoothStrength.getValue()));
                    }

                    if (smoothMode.get("ReactionTime")) {
                        Rot delta1 = delta.copy();

                        if (startSlowRotation) {
                            startSlowRotation = false;
                            delta.setYaw(delta.getYaw() * 0.2f);
                            delta.setPitch(delta.getPitch() * 0.2f);
                        }

                        if (delta1.hypot() < reactionTime.getValue()) startSlowRotation = true;
                    }

                    RotUtils.limitDelta(delta, speed);

                    if (smoothMode.get("MixDelta")) {
                        delta.setYaw(MathHelper.lerp((float) mixYawDelta.getRandomizedDoubleValue(), lastDelta.getYaw(), delta.getYaw()));
                        delta.setPitch(MathHelper.lerp((float) mixPitchDelta.getRandomizedDoubleValue(), lastDelta.getPitch(), delta.getPitch()));
                    }
                }

                lastDelta = delta.copy();
                delta = RotUtils.fixDelta(delta);

                CameraRot.INST.setUnlocked(true);
                mc.thePlayer.moveRotation(delta);
            }

            if (moveFix.is("OFF")) {
                if (event instanceof MoveFlyingEvent e) e.setYaw(CameraRot.INST.getYaw());
                if (event instanceof JumpEvent e) e.setYaw(CameraRot.INST.getYaw());
            }

            if (event instanceof MoveEvent e) {
                if (moveFix.is("Silent")) MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));
            }
        }
    }

    private Rot getRotation(EntityLivingBase target, Rot lr, AxisAlignedBB box) {
        boolean teleport = (TimerRange.balance > 0 || TimerRange.teleporting) && teleportPredictFix.isToggled();

        Rot needRot = teleport ?
            RotUtils.getBestRotation(box) :
            switch (hitVec.getMode()) {
                case "Best" -> RotUtils.getBestRotation(box);
                case "Nearest" -> RotUtils.getNearestRotations(lr, box);
                case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
                case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                default -> throw new IllegalStateException("Unexpected value: " + hitVec.getMode());
            };

        if (needRot == null) return null;

        if (smartAim.isToggled()) {
            MovingObjectPosition hit = RayCastUtils.rayCast(needRot, findDistance.getValue());

            if (mc.thePlayer.canVecBeSeen(hit.hitVec)) {
                needRot = RotUtils.getPossibleBestRotation(needRot, box);
            }
        }

        return needRot;
    }

    private EntityLivingBase findNewTarget() {
        List<EntityLivingBase> entityList = new ArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity != mc.thePlayer && DistanceUtils.getDistance(entity) < findDistance.getValue() && !entity.isDead) {
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

        EntityLivingBase newTarget = null;

        if (!entityList.isEmpty()) {
            newTarget = entityList.getFirst();
        }

        if (TargetStorage.getTarget() != null && newTarget == null) CameraRot.INST.setWillChange(false);

        return newTarget;
    }
}