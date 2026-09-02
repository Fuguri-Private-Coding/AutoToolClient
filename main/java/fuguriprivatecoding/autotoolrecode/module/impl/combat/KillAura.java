package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Clicks;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.math.FastNoiseLite;
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
import fuguriprivatecoding.autotoolrecode.utils.value.Constants;
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

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    private final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 12, 12, 0.1f);
    private final FloatSetting rotateDistance = new FloatSetting("RotateDistance", this, 3, 12, 6, 0.1f);
    private final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 12, 3.5f, 0.1f);

    private final MultiMode targets = new MultiMode("Targets", this)
        .addModes("Players", "Mobs", "Animals", "Villagers");

    private final Mode sortType = new Mode("SortType", this)
        .addModes("Distance", "FOV", "Switch")
        .setMode("FOV");

    private final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Best");

    private final BooleanSupplier boxSize = () -> hitVec.is("Best") || hitVec.is("Nearest");
    private final IntegerSetting hBoxSize = new IntegerSetting("HBoxSize", this, boxSize, 1, 100, 100);
    private final IntegerSetting vBoxSize = new IntegerSetting("VBoxSize", this, boxSize, 1, 100, 100);

    private final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 180, 90, 1);
    private final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0, 180, 90, 1);

    private final CheckBox smartAim = new CheckBox("SmartAim", this);
    private final CheckBox snapForTeleport = new CheckBox("SnapForTeleport", this);

    private final MultiMode smoothModes = new MultiMode("SmoothModes", this)
        .addModes("MouseDelta", "Linear", "Basic", "MixDelta", "Noise");

    private final Mode noiseType = new Mode("NoiseType", this, () -> smoothModes.get("Noise"))
        .addModes("OpenSimplex2", "OpenSimplex2S", "Cellular", "Perlin", "ValueCubic", "Value")
        .setMode("Perlin")
        ;

    private final FloatSetting noiseSpeed = new FloatSetting("NoiseSpeed", this, () -> smoothModes.get("Noise"), 0.1f, 10f, 2f, 0.1f);

    private final FloatSetting yawNoiseStrength = new FloatSetting("YawNoiseStrength", this, () -> smoothModes.get("Noise"), 0, 20, 5, 0.1f);
    private final FloatSetting pitchNoiseStrength = new FloatSetting("PitchNoiseStrength", this, () -> smoothModes.get("Noise"), 0, 20, 5, 0.1f);

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

    private final DoubleSlider CPS = new DoubleSlider("CPS", this, 1, 40, 16, 1);
    private final DoubleSlider CPSUpdateDelay = new DoubleSlider("CPSUpdateDelay", this, 0, 20, 5, 1);

    private final CheckBox lockView = new CheckBox("LockView", this, false);

    private final Mode moveFix = new Mode("MoveFix", this)
        .addModes("OFF", "Legit", "Silent")
        .setMode("Silent");

    private double currentCps;
    private int currentCpsUpdateDelay;

    private final StopWatch clickTimer = new StopWatch();
    private long delay;

    private final FastNoiseLite noise = new FastNoiseLite((int) (System.currentTimeMillis() / 1000f));

    private Rot lastDelta = new Rot();

    @Override
    public void onDisable() {
        if (!Modules.getModule(Scaffold.class).isToggled()) CameraRot.INST.setWillChange(false);
        TargetStorage.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            TargetStorage.setTarget(findNewTarget());
        }

        EntityLivingBase target = TargetStorage.getTarget();
        if (Modules.getModule(Scaffold.class).isToggled()) return;

        if (target != null) {
            if (event instanceof RunGameLoopEvent && needClicking(target)) {
                if (clickTimer.reachedMS(delay)) {
                    updateDelay();
                    clickTimer.reset();
                    Clicks.addClick();
                }
            }

            if (event instanceof TickEvent) {
                if (currentCpsUpdateDelay > 0) currentCpsUpdateDelay--;
            }

            if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.LEFT) {
                e.cancel();
            }

            if (DistanceUtils.getDistance(target) > rotateDistance.getValue()) {
                CameraRot.INST.setWillChange(false);
                return;
            }
            if (lockView.isToggled()) {
                if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.POST) rotate(target);
            } else {
                if (event instanceof TickEvent) rotate(target);
            }

            if (moveFix.is("OFF")) {
                if (event instanceof MoveFlyingEvent e) e.setYaw(CameraRot.INST.getYaw());
                if (event instanceof JumpEvent e) e.setYaw(CameraRot.INST.getYaw());
            } else if (moveFix.is("Silent")) {
                if (event instanceof MoveEvent e) MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));
            }
        }
    }

    private void updateDelay() {
        updateCps();
        delay = Math.round(1000 / currentCps);
    }

    private void updateCps() {
        if (currentCpsUpdateDelay == 0) {
            currentCpsUpdateDelay = CPSUpdateDelay.getRandomizedIntValue();
            currentCps = CPS.getRandomizedDoubleValue();
        }
    }

    private Rot getRotation(EntityLivingBase target, AxisAlignedBB box) {
        boolean teleport = (TimerRange.needSnap()) && snapForTeleport.isToggled();

        AxisAlignedBB fullBox = target.getExpandedBoundingBox();

        Vec3 targetPos = target.getSmoothPositionVector();

        Vec3 needPoint = switch (hitVec.getMode()) {
            case "Best" -> RotUtils.getBestHitVec(box);
            case "Head" -> targetPos.addVector(0, target.getEyeHeight(), 0);
            case "Body" -> targetPos.addVector(0, target.getEyeHeight() / 2f, 0);
            default -> Constants.VEC3_ZERO;
        };

        Rot needRot = RotUtils.getRotationToPoint(needPoint);

        if (hitVec.is("Nearest")) needRot = RotUtils.getNearestRotation(mc.thePlayer.getRotation(), box);
        if (fullBox.isVecInside(mc.thePlayer.getPositionEyes(1f))) needRot = RotUtils.getNearestRotation(mc.thePlayer.getRotation(), fullBox);

        if (teleport) needRot = RotUtils.getBestRotation(fullBox);

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
        boolean teleport = (TimerRange.needSnap()) && snapForTeleport.isToggled();

        AxisAlignedBB box = RotUtils.getHitBox(target, hBoxSize.getValue(), vBoxSize.getValue());

        Rot needRotation = getRotation(target, box);

        if (needRotation == null)
            return;

        Rot delta = mc.thePlayer.getRotation().deltaTo(needRotation);

        if (!teleport) delta = transformDelta(delta);

        delta = delta.fixed();
        lastDelta = delta.copy();

        CameraRot.INST.setUnlocked(!lockView.isToggled());
        mc.thePlayer.moveRotation(delta);

        if (lockView.isToggled()) {
            mc.entityRenderer.getMouseOver(1f);
        }
    }

    private Rot transformDelta(Rot delta) {
        if (smoothModes.get("Noise")) {
            FastNoiseLite.NoiseType type = FastNoiseLite.NoiseType.valueOf(noiseType.getMode());

            noise.SetNoiseType(type);
            noise.SetFrequency(noiseSpeed.getValue());

            float strengthYaw = yawNoiseStrength.getValue();
            float strengthPitch = pitchNoiseStrength.getValue();

            float t = System.nanoTime() / 1_000_000_000f;

            float randomYaw = noise.GetNoise(t, 0f) * strengthYaw;
            float randomPitch = noise.GetNoise(0f, t) * strengthPitch;

            Rot add = new Rot(randomYaw, randomPitch);

            delta = delta.plus(add);
        }

        if (smoothModes.get("Basic")) {
            float randomYaw = RandomUtils.nextFloat(-yawStrength.getValue(), yawStrength.getValue());
            float randomPitch = RandomUtils.nextFloat(-pitchStrength.getValue(), pitchStrength.getValue());

            Rot add = new Rot(randomYaw, randomPitch);

            delta = delta.plus(add);
        }

        if (smoothModes.get("MouseDelta")) {
            Rot mouseDelta = invertDelta.isToggled() ?
                CameraRot.INST.deltaTo(CameraRot.INST.getPrevRot()) :
                CameraRot.INST.getPrevRot().deltaTo(CameraRot.INST);

            float multipleDelta = (float) deltaMultiplier.getRandomizedDoubleValue();

            delta = delta.plus(mouseDelta.multiplied(multipleDelta));
        }

        if (smoothModes.get("Linear")) {
            delta = delta.divided(linearSmoothStrength.getValue());
        }

        Rot speed = new Rot(
            yawSpeed.getRandomizedIntValue(),
            pitchSpeed.getRandomizedIntValue()
        );

        delta = delta.limitedLine(speed);

        if (smoothModes.get("MixDelta")) {
            delta = lastDelta.lerp(delta, mixYawDelta.getRandomizedIntValue() / 100f, mixPitchDelta.getRandomizedIntValue() / 100f);
        }

        return delta;
    }

    private boolean needClicking(EntityLivingBase target) {
        return mc.currentScreen == null && !mc.thePlayer.isUsingItem() && DistanceUtils.getDistance(target) < clickDistance.getValue() && TimerRange.balance == 0;
    }

    private EntityLivingBase findNewTarget() {
        List<EntityLivingBase> entityList = new CopyOnWriteArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (isValidTarget(entity) && entity instanceof EntityLivingBase base && matchesTargetType(base)) {
                entityList.add(base);
            }
        }

        entityList.sort(
            switch (sortType.getMode()) {
                case "Distance" -> Comparator.comparingDouble(DistanceUtils::getDistance);
                case "Switch" -> Comparator.comparingDouble(ent -> {
                    double distance = DistanceUtils.getDistance(ent);
                    int hurtTime = ent.hurtTime;

                    if (distance > 3) {
                        return 10000;
                    }

                    return hurtTime + distance;
                });
                default -> Comparator.comparingDouble(RotUtils::getFovToEntity);
            }
        );

        EntityLivingBase newTarget = !entityList.isEmpty() ? entityList.getFirst() : null;

        if (TargetStorage.getTarget() != null && newTarget == null)
            CameraRot.INST.setWillChange(false);

        return newTarget;
    }

    private boolean isValidTarget(Entity entity) {
        return entity != mc.thePlayer && entity.isEntityAlive() && DistanceUtils.getDistance(entity) <= findDistance.getValue();
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