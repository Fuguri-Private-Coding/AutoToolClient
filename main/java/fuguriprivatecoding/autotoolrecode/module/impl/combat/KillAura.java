package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    private final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 8, 6, 0.1f);
    private final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 8, 6f, 0.1f);

    private final MultiMode targets = new MultiMode("Targets", this)
        .addModes("Players", "Mobs", "Animals", "Villagers");

    private final Mode sortType = new Mode("SortType", this)
        .addModes("Distance", "FOV", "HurtTime", "Switch")
        .setMode("FOV");

    private final IntegerSetting maxSwitchEntity = new IntegerSetting("MaxSwitchEntity", this, () -> sortType.is("Switch"), 0, 5,5);

    private final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Best");

    private final BooleanSupplier boxSize = () -> hitVec.is("Best") || hitVec.is("Nearest");
    private final IntegerSetting hBoxSize = new IntegerSetting("HBoxSize", this, boxSize, 1, 100, 100);
    private final IntegerSetting vBoxSize = new IntegerSetting("VBoxSize", this, boxSize, 1, 100, 100);

    private final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 180, 90, 1);
    private final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0, 180, 90, 1);

    private final CheckBox smartAim = new CheckBox("SmartAim", this);

    private final CheckBox teleportPredictFix = new CheckBox("TeleportPredictFix", this);

    private final MultiMode smoothMode = new MultiMode("SmoothModes", this)
        .addModes("Linear", "Basic", "MixDelta", "ReactionTime", "Offset", "Advanced");

    private final CheckBox cameraAim = new CheckBox("CameraAim", this, false);

    private final DoubleSlider mixYawDelta = new DoubleSlider("MixYawDelta", this, () -> smoothMode.get("MixDelta"), 0, 100, 1, 1f);
    private final DoubleSlider mixPitchDelta = new DoubleSlider("MixPitchDelta", this, () -> smoothMode.get("MixDelta"), 0, 100, 1, 1f);

    private final FloatSetting randomizeStrength = new FloatSetting("RandomizeStrength", this, () -> smoothMode.get("Basic"), 0, 20, 5, 0.1f);
    private final FloatSetting reactionTime = new FloatSetting("ReactionTime", this, () -> smoothMode.get("ReactionTime"), 0, 5, 1, 0.1f);

    private final FloatSetting offsetX = new FloatSetting("OffsetX", this, () -> smoothMode.get("Offset"), -1, 1, 0, 0.01f);
    private final FloatSetting offsetY = new FloatSetting("OffsetY", this, () -> smoothMode.get("Offset"), -1, 1, -0.2f, 0.01f);
    private final FloatSetting offsetZ = new FloatSetting("OffsetZ", this, () -> smoothMode.get("Offset"), -1, 1, 0, 0.01f);

    private final FloatSetting linearSmoothStrength = new FloatSetting(
        "LinearSmoothStrength", this,
        () -> smoothMode.get("Linear"),
        1, 5, 1.5f, 0.1f
    );

    private final IntegerSetting moveSpeed = new IntegerSetting("MoveSpeed", this, () -> smoothMode.get("Advanced"), 0, 100, 75);

    private final IntegerSetting frictionAtLargeMove = new IntegerSetting("FrictionAtLargeMove", this, () -> smoothMode.get("Advanced"), 0, 100, 50);
    private final IntegerSetting howManyDegreesPerTickIsLargeMove = new IntegerSetting("HowManyDegreesPerTickIsLargeMove?", this, () -> smoothMode.get("Advanced"), 0, 180, 60);

    private final DoubleSlider stopTimeInVerySmallMove = new DoubleSlider("StopTimeInVerySmallMoveTicks", this, () -> smoothMode.get("Advanced"), 0, 20, 4, 1);
    private final DoubleSlider multiplierToSnapAfterSmallMove = new DoubleSlider("MultiplierToSnapAfterSmallMove", this, () -> smoothMode.get("Advanced"), 0, 5, 2, 0.01);
    private final FloatSetting howManyDegreesPerTickIsVerySmallMove = new FloatSetting("HowManyDegreesPerTickIsVerySmallMove?", this, () -> smoothMode.get("Advanced"), 0, 20, 5, 0.1f);

    private final DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 80, 16, 1);

    private final Mode moveFix = new Mode("MoveFix", this)
        .addModes("OFF", "Legit", "Silent")
        .setMode("Silent");

    private final MultiMode autoDisableIf = new MultiMode("AutoDisableIf", this)
        .addModes("ChangeWorld");

    private final StopWatch clickTimer = new StopWatch();
    private long delay;

    private int waitTicks;
    private boolean startSlowRotation;

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
            if (event instanceof RunGameLoopEvent && needClicking(target)) {
                if (clickTimer.reachedMS(delay)) {
                    delay = Math.round(1000f / CPS.getRandomizedIntValue());
                    Clicks.addClick();
                    clickTimer.reset();
                }
            }

            if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
                e.cancel();
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

    private Rot getRotation(EntityLivingBase target, Rot lr, AxisAlignedBB box) {
        boolean teleport = TimerRange.isTeleporting() && teleportPredictFix.isToggled();

        Rot needRot = switch (hitVec.getMode()) {
                case "Best" -> RotUtils.getBestRotation(box);
                case "Nearest" -> RotUtils.getNearestRotations(lr, box);
                case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
                case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                default -> null;
            };

        if (teleport) needRot = RotUtils.getBestRotation(box);

        if (needRot == null) return null;

        if (smartAim.isToggled()) {
            RayTrace hit = RayCastUtils.rayCast(needRot, findDistance.getValue(), 0.3f);

            if (hit == null || hit.typeOfHit != RayTrace.RayType.ENTITY) {
                needRot = RotUtils.getPossibleBestRotation(needRot, box);
            }
        }

        if (cameraAim.isToggled()) {
            RayTrace hit = RayCastUtils.rayCast(CameraRot.INST, findDistance.getValue() + 3, 0);

            Rot dp = RotUtils.getDelta(CameraRot.INST, CameraRot.INST.getPrevRot());

//            AxisAlignedBB hitBox = box.expand(-0.1, -0.1, -0.1);
//            Vec3 hitVec = new Vec3(
//                Math.clamp(hit.hitVec.xCoord, hitBox.minX, hitBox.maxX),
//                Math.clamp(hit.hitVec.yCoord, hitBox.minY, hitBox.maxY),
//                Math.clamp(hit.hitVec.zCoord, hitBox.minZ, hitBox.maxZ)
//            );
//
//            if (hit.entityHit == target) {
//                needRot = RotUtils.getRotationToPoint(hit.hitVec);
//            }

            if (hit.typeOfHit == RayTrace.RayType.ENTITY) needRot = CameraRot.INST;

        }

        return needRot;
    }


    private void rotate(EntityLivingBase target) {
        Rot lr = mc.thePlayer.getRotation();

        boolean teleport = TimerRange.isTeleporting() && teleportPredictFix.isToggled();

        double offset = target.getCollisionBorderSize();

        AxisAlignedBB box = RotUtils.getHitBox(
                target,
                teleport ? 100 : hBoxSize.getValue(),
                teleport ? 100 : vBoxSize.getValue()
        ).expand(offset, offset, offset);

        Rot needRotation = getRotation(target, lr, box);

        if (needRotation == null) return;

        Rot delta;

        if (smoothMode.get("Advanced")) {
            float moveMultiplier = Math.clamp((moveSpeed.getValue() + RandomUtils.nextInt(1, 20)) / 100f, 0, 1);
            delta = RotUtils.getDelta(lr, needRotation).multiplier(moveMultiplier);
        } else {
            delta = RotUtils.getDelta(lr, needRotation);
        }

        Rot speed = new Rot(
                yawSpeed.getRandomizedIntValue(),
                pitchSpeed.getRandomizedIntValue()
        );

        if (!teleport) {
            if (smoothMode.get("Offset")) {
                Rot rotation = RotUtils.getRotationToPoint(RotUtils.getBestHitVec(box).addVector(offsetX.getValue(), offsetY.getValue(), offsetZ.getValue()));
                delta = RotUtils.getDelta(lr, rotation);
            }

            if (smoothMode.get("Basic")) {
                Rot rot = new Rot(
                        RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue()),
                        RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue())
                );

                delta.setYaw(MathHelper.wrapDegree(delta.getYaw() - rot.getYaw()));
                delta.setPitch(MathHelper.wrapDegree(delta.getPitch() - rot.getPitch()));
            }

            if (smoothMode.get("Linear")) {
                delta = delta.divine(linearSmoothStrength.getValue(), linearSmoothStrength.getValue());
            }

            if (smoothMode.get("ReactionTime")) {
                Rot delta1 = delta.copy();

                delta = delta.multiplier(RandomUtils.nextFloat(0.6f, 0.7f));

                if (startSlowRotation) {
                    delta = delta.multiplier(0.2f);
                    startSlowRotation = false;
                }

                if (delta1.hypot() < reactionTime.getValue()) startSlowRotation = true;
            }

            if (smoothMode.get("Advanced")) {
                double deltaScale = delta.hypot();
                boolean large = deltaScale >= howManyDegreesPerTickIsLargeMove.getValue();
                boolean verySmall = deltaScale <= howManyDegreesPerTickIsVerySmallMove.getValue();

                if (large) {
                    delta.setYaw(MathHelper.lerp(frictionAtLargeMove.getValue() / 100f, lastDelta.getYaw(), delta.getYaw()));
                    delta.setPitch(MathHelper.lerp(frictionAtLargeMove.getValue() / 100f, lastDelta.getPitch(), delta.getPitch()));
                }

                if (verySmall) {
                    if (waitTicks > 0) {
                        waitTicks--;
                        delta.setYaw(0);
                        delta.setPitch(0);
                    } else if (waitTicks == 0) {
                        delta = delta.multiplier((float) multiplierToSnapAfterSmallMove.getRandomizedDoubleValue());
                        waitTicks = stopTimeInVerySmallMove.getRandomizedIntValue();
                    }
                }
            }

            RotUtils.limitDelta(delta, speed);

            if (smoothMode.get("MixDelta")) {
                delta.setYaw(MathHelper.lerp((float) mixYawDelta.getRandomizedIntValue() / 100f, lastDelta.getYaw(), delta.getYaw()));
                delta.setPitch(MathHelper.lerp((float) mixPitchDelta.getRandomizedIntValue() / 100f, lastDelta.getPitch(), delta.getPitch()));
            }
        }

        delta = RotUtils.fixDelta(delta);
        lastDelta = delta.copy();

        CameraRot.INST.setUnlocked(true);
        mc.thePlayer.moveRotation(delta);
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

        List<Entity> switchList = new CopyOnWriteArrayList<>();

        for (EntityLivingBase ent : entityList) {
            double distance = DistanceUtils.getDistance(ent);
            if (distance <= 3 && switchList.size() < maxSwitchEntity.getValue()) switchList.add(ent);
        }

        entityList.sort(
            switch (sortType.getMode()) {
                case "Distance" -> Comparator.comparingDouble(DistanceUtils::getDistance);
                case "HurtTime" -> Comparator.comparingDouble(ent -> ent.hurtTime);
                case "Switch" -> Comparator.comparingDouble(entity -> {
                    double distance = DistanceUtils.getDistance(entity);
                    double hurtTime = entity.hurtTime;

                    if (switchList.size() > 1) {
                        return hurtTime + distance;
                    } else {
                        return distance;
                    }
                });
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
            case EntityPlayer player -> targets.get("Players") && !player.isFriend() && !player.isTeam();
            case EntityMob ignore -> targets.get("Mobs");
            case EntityAnimal ignore -> targets.get("Animals");
            case EntityVillager ignore -> targets.get("Villagers");
            default -> false;
        };
    }
}
