package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
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

import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    private final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 12, 12, 0.1f);
    private final FloatSetting rotateDistance = new FloatSetting("RotateDistance", this, 3, 12, 6, 0.1f);
    private final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 12, 3.5f, 0.1f);

    private final MultiMode targets = new MultiMode("Targets", this)
        .addModes("Players", "Mobs", "Animals", "Villagers");

    private final Mode sortType = new Mode("SortType", this)
        .addModes("Distance", "FOV", "HurtTime")
        .setMode("FOV");

    private final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Best");

    private final BooleanSupplier boxSize = () -> hitVec.is("Best") || hitVec.is("Nearest") || hitVec.is("Camera");
    private final IntegerSetting hBoxSize = new IntegerSetting("HBoxSize", this, boxSize, 1, 100, 100);
    private final IntegerSetting vBoxSize = new IntegerSetting("VBoxSize", this, boxSize, 1, 100, 100);

    private final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 180, 90, 1);
    private final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0, 180, 90, 1);

    private final CheckBox smartAim = new CheckBox("SmartAim", this);
    private final CheckBox snapForTeleport = new CheckBox("SnapForTeleport", this);

    private final MultiMode smoothModes = new MultiMode("SmoothModes", this)
        .addModes("MouseDelta", "Linear", "Basic", "MixDelta", "Recorded");

    private final DoubleSlider deltaMultiplier = new DoubleSlider("DeltaMultiplier", this, () -> smoothModes.get("MouseDelta"), 1, 15, 8, 0.1f);
    private final CheckBox invertDelta = new CheckBox("InvertDelta", this, () -> smoothModes.get("MouseDelta"), false);

    private final DoubleSlider mixYawDelta = new DoubleSlider("MixYawDelta", this, () -> smoothModes.get("MixDelta"), 0, 100, 1, 1f);
    private final DoubleSlider mixPitchDelta = new DoubleSlider("MixPitchDelta", this, () -> smoothModes.get("MixDelta"), 0, 100, 1, 1f);

    private final FloatSetting yawStrength = new FloatSetting("YawRandomizeStrength", this, () -> smoothModes.get("Basic"), 0, 20, 5, 0.1f);
    private final FloatSetting pitchStrength = new FloatSetting("PitchRandomizeStrength", this, () -> smoothModes.get("Basic"), 0, 20, 5, 0.1f);

    private final FloatSetting linearSmoothStrength = new FloatSetting(
        "LinearSmoothStrength", this,
        () -> smoothModes.get("Linear"),
        1, 5, 1.5f, 0.1f
    );

    public final TestRotationOffsetSetting recordedOffset = new TestRotationOffsetSetting("RecordedOffset", this, () -> smoothModes.get("Recorded"));
    private final DoubleSlider recordedMultiplier = new DoubleSlider("RecordedMultiplier", this, () -> smoothModes.get("Recorded"), 0, 10, 1, 0.01f);
    private final FloatSetting recordedStd = new FloatSetting("YawStd", this, () -> smoothModes.get("Recorded"), 0, 20, 5, 0.1f);
    private final FloatSetting recordedMean = new FloatSetting("YawMean", this, () -> smoothModes.get("Recorded"), 0, 20, 5, 0.1f);

    private final DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 80, 16, 1);

    private final Mode randomizeMode = new Mode("RandomizeMode", this)
        .addModes("None", "Gaussian")
        .setMode("Gaussian");

    private final BooleanSupplier gaussianMode = () -> randomizeMode.is("Gaussian");

    private final DoubleSlider cpsLimiter = new DoubleSlider("CPSLimiter", this, gaussianMode, 0, 40, 20, 1);

    private final FloatSetting consistency = new FloatSetting("Consistency", this, gaussianMode, 0, 2, 0.2f, 0.01f);
    private final FloatSetting instability = new FloatSetting("Instability", this, gaussianMode, 0, 2, 0.2f, 0.01f);

    private final Mode moveFix = new Mode("MoveFix", this)
        .addModes("OFF", "Legit", "Silent")
        .setMode("Silent");

    private final MultiMode autoDisableIf = new MultiMode("AutoDisableIf", this)
        .addModes("ChangeWorld");

    private final StopWatch clickTimer = new StopWatch();
    private long delay;

    private int recordedIndex;

    private Rot lastDelta = new Rot();

    @Override
    public void onDisable() {
        if (!Modules.getModule(Scaffold.class).isToggled()) CameraRot.INST.setWillChange(false);
        TargetStorage.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof WorldChangeEvent && autoDisableIf.get("ChangeWorld")) {
            setToggled(false);
        }

        if (event instanceof TickEvent) {
            TargetStorage.setTarget(findNewTarget());
        }

        EntityLivingBase target = TargetStorage.getTarget();
        if (Modules.getModule(Scaffold.class).isToggled()) return;

        if (target != null) {
            if (event instanceof Render2DEvent && smoothModes.get("Basic")) {
                animX.update((float) RandomUtils.nextGaussian(0.2f, 2f), Easing.OUT_BACK);
                animY.update((float) RandomUtils.nextGaussian(0.2f, 2f), Easing.OUT_BACK);
            }

            if (event instanceof RunGameLoopEvent && needClicking(target)) {
                if (clickTimer.reachedMS(delay)) {
                    this.delay = getDelay();
                    clickTimer.reset();
                    Clicks.addClick();
                }
            }

            if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
                e.cancel();
            }

            if (DistanceUtils.getDistance(target) > rotateDistance.getValue()) {
                CameraRot.INST.setWillChange(false);
                return;
            }

            if (event instanceof TickEvent) {
                rotate(target);
            }

            if (moveFix.is("OFF")) {
                if (event instanceof MoveFlyingEvent e) e.setYaw(CameraRot.INST.getYaw());
                if (event instanceof JumpEvent e) e.setYaw(CameraRot.INST.getYaw());
            } else if (moveFix.is("Silent")) {
                if (event instanceof MoveEvent e) MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));
            }
        }
    }

    EasingAnimation animX = new EasingAnimation();
    EasingAnimation animY = new EasingAnimation();

    private Rot getRotation(EntityLivingBase target, AxisAlignedBB box) {
        boolean teleport = (TimerRange.needSnap()) && snapForTeleport.isToggled();

        AxisAlignedBB fullBox = RotUtils.getHitBox(target, 100, 100).expand(0.1);

        Vec3 eyes = mc.thePlayer.getPositionEyes(1f);

        Vec3 needPoint = switch (hitVec.getMode()) {
            case "Best" -> RotUtils.getBestHitVec(box);
            case "Head" -> RenderUtils.getAbsoluteSmoothPos(target.getLastPositionVector(), target.getPositionVector(), mc.timer.renderPartialTicks).addVector(0, target.getEyeHeight(), 0);
            case "Body" -> RenderUtils.getAbsoluteSmoothPos(target.getLastPositionVector(), target.getPositionVector(), mc.timer.renderPartialTicks).addVector(0, target.getEyeHeight() / 2f, 0);
            default -> Vec3.ZERO;
        };

        Rot needRot = RotUtils.getRotationToPoint(needPoint);

        if (fullBox.isVecInside(eyes)) {
            needRot = RotUtils.getNearestRotation(mc.thePlayer.getRotation(), fullBox);
        }

        if (hitVec.is("Nearest")) needRot = RotUtils.getNearestRotation(mc.thePlayer.getRotation(), box);

        if (smoothModes.get("MouseDelta")) {
            Rot mouseDelta = invertDelta.isToggled() ?
                CameraRot.INST.deltaTo(CameraRot.INST.getPrevRot()) :
                CameraRot.INST.getPrevRot().deltaTo(CameraRot.INST);

            float multipleDelta = (float) deltaMultiplier.getRandomizedDoubleValue();

            mouseDelta = mouseDelta.multiple(multipleDelta);

            needRot = needRot.plus(mouseDelta);
        }

        if (smoothModes.get("Recorded")) {
            if (recordedIndex >= recordedOffset.offsets.size()) recordedIndex = 0;

            Rot recordedDelta = recordedOffset.getByIndex(recordedIndex++);

            float recordedMultiple = (float) RandomUtils.nextGaussianInRange(recordedMultiplier.getMinValue(), recordedMultiplier.getMaxValue(), recordedMean.getValue(), recordedStd.getValue());
            needRot = needRot.plus(recordedDelta.multiple(recordedMultiple));
        }

        if (teleport) needRot = RotUtils.getBestRotation(box);

        if (smartAim.isToggled()) {
            RayTrace hit = RayCastUtils.rayCast(needRot, findDistance.getValue(), 0);
            RayTrace hits = RayCastUtils.rayCast(findDistance.getValue(), 0, needRot);

            if (hit.typeOfHit == RayTrace.RayType.BLOCK && hits.typeOfHit == RayTrace.RayType.ENTITY) {
                needRot = RotUtils.getPossibleBestRotation(needRot, fullBox);
            }
        }

        return needRot;
    }

    private void rotate(EntityLivingBase target) {
        Rot lr = mc.thePlayer.getRotation();

        boolean teleport = (TimerRange.needSnap()) && snapForTeleport.isToggled();

        double expand = target.getCollisionBorderSize();
        AxisAlignedBB box = RotUtils.getHitBox(
                target,
                teleport ? 100 : hBoxSize.getValue(),
                teleport ? 100 : vBoxSize.getValue()
        ).expand(expand, expand, expand);

        Rot needRotation = getRotation(target, box);

        if (needRotation == null) return;

        Rot delta = lr.deltaTo(needRotation);

        if (!teleport) {
            if (smoothModes.get("Basic")) {
                Rot rot = new Rot(
                    (float) RandomUtils.nextGaussian(-yawStrength.getValue(), yawStrength.getValue()),
                    (float) RandomUtils.nextGaussian(-pitchStrength.getValue(), pitchStrength.getValue())
                );

                animX.setEnd(rot.getYaw());
                animY.setEnd(rot.getPitch());

                Rot d = new Rot(animX.getValue(), animY.getValue());

                delta = delta.plus(d);
            }

            if (smoothModes.get("Linear")) {
                delta = delta.divine(linearSmoothStrength.getValue(), linearSmoothStrength.getValue());
            }

            Rot speed = new Rot(
                yawSpeed.getRandomizedIntValue(),
                pitchSpeed.getRandomizedIntValue()
            );

            delta = delta.limit(speed);

            if (smoothModes.get("MixDelta")) {
                delta = lastDelta.lerp(delta, (float) mixYawDelta.getRandomizedIntValue() / 100f, (float) mixPitchDelta.getRandomizedIntValue() / 100f);
            }
        }

        delta = delta.fix();
        lastDelta = delta.copy();

        CameraRot.INST.setUnlocked(true);
        mc.thePlayer.moveRotation(delta);
    }

    private long getDelay() {
        double baseDelay = 1000D / CPS.getRandomizedDoubleValue();

        if (randomizeMode.is("Gaussian")) {
            double minDelay = 1000D / cpsLimiter.getMaxValue();
            double maxDelay = 1000D / cpsLimiter.getMinValue();

            double consistency = this.consistency.getValue();
            double instability = this.instability.getValue();

            double gaussian = RandomUtils.random.nextGaussian() * consistency;
            double noise = (RandomUtils.random.nextDouble() - 0.5) * instability;

            double delay = baseDelay * (1 + gaussian + noise);

            return (long) Math.clamp(delay, minDelay, maxDelay);
        }

        return (long) baseDelay;
    }

    private boolean needClicking(EntityLivingBase target) {
        return mc.currentScreen == null && !mc.thePlayer.isUsingItem() && DistanceUtils.getDistance(target) < clickDistance.getValue() && TimerRange.balance == 0;
    }

    private EntityLivingBase findNewTarget() {
        List<EntityLivingBase> entityList = mc.theWorld.loadedEntityList.stream()
            .filter(this::isValidTarget)
            .filter(this::isWithinDistance)
            .filter(EntityLivingBase.class::isInstance)
            .map(EntityLivingBase.class::cast)
            .filter(this::matchesTargetType)
            .collect(Collectors.toList());

        entityList.removeIf(ent -> DistanceUtils.getDistance(ent) > findDistance.getValue());

        entityList.sort(
            switch (sortType.getMode()) {
                case "Distance" -> Comparator.comparingDouble(DistanceUtils::getDistance);
                case "HurtTime" -> Comparator.comparingDouble(ent -> ent.hurtTime);
                default -> Comparator.comparingDouble(RotUtils::getFovToEntity);
            }
        );

        EntityLivingBase newTarget = null;

        if (!entityList.isEmpty()) {
            newTarget = entityList.getFirst();
        }

        if (TargetStorage.getTarget() != null && newTarget == null) CameraRot.INST.setWillChange(false);

        return newTarget;
    }

    private boolean isValidTarget(Entity entity) {
        return entity != mc.thePlayer && !entity.isDead;
    }

    private boolean isWithinDistance(Entity entity) {
        return DistanceUtils.getDistance(entity) < findDistance.getValue();
    }

    private boolean matchesTargetType(EntityLivingBase entity) {
        return switch (entity) {
            case EntityPlayer player -> targets.get("Players") && !player.isFriend() && !player.isBot() && !player.isTeam();
            case EntityMob ignore -> targets.get("Mobs");
            case EntityAnimal ignore -> targets.get("Animals");
            case EntityVillager ignore -> targets.get("Villagers");
            default -> false;
        };
    }
}