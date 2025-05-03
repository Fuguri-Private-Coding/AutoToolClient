package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.deeplearn.rotation.AIRotationSmooth;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.*;
import me.hackclient.managers.CombatManager;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.io.File;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "KillAura", category = Category.COMBAT)
public class KillAura extends Module {

    final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 8, 6, 0.1f);
    final MultiBooleanSetting targets = new MultiBooleanSetting("Targets", this)
            .add("Players", true)
            .add("Mobs")
            .add("Animals")
            .add("Villagers");
    final ModeSetting sortType = new ModeSetting("SortType", this)
            .addModes("Distance", "FOV")
            .setMode("FOV");

    final FloatSetting rotationDistance = new FloatSetting("RotationDistance", this, 3, 8, 4.5f, 0.1f);

    final ModeSetting hitVec = new ModeSetting("HitVec", this)
            .addModes("Best", "Nearest", "Head", "Body")
            .setMode("Best");

    final BooleanSupplier hitBoxSizeVisible = () -> hitVec.getMode().equalsIgnoreCase("Best") || hitVec.getMode().equalsIgnoreCase("Nearest");
    final IntegerSetting horizontalHitBoxSize = new IntegerSetting("HorizontalHitBoxSize", this, hitBoxSizeVisible, 0, 100, 100);
    final IntegerSetting verticalHitBoxSize = new IntegerSetting("VerticalHitBoxSize", this, hitBoxSizeVisible, 0, 100, 100);

    final IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this, 0, 180, 90) {
        @Override
        public int getValue() {
            if (maxYawSpeed.value < value) { value = maxYawSpeed.value; }
            return value;
        }
    };
    final IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this, 0, 180, 30) {
        @Override
        public int getValue() {
            if (minYawSpeed.value > value) { value = minYawSpeed.value; }
            return value;
        }
    };
    final IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this, 0, 180, 90) {
        @Override
        public int getValue() {
            if (maxPitchSpeed.value < value) { value = maxPitchSpeed.value; }
            return value;
        }
    };
    final IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this, 0, 180, 30) {
        @Override
        public int getValue() {
            if (minPitchSpeed.value > value) { value = minPitchSpeed.value; }
            return value;
        }
    };

    final ModeSetting smoothMode = new ModeSetting("SmoothMode", this)
            .addModes("Linear", "AIModel")
            .setMode("Linear");

    final FloatSetting linearSmoothStrength = new FloatSetting(
            "LinearSmoothStrength", this,
            () -> smoothMode.getMode().equalsIgnoreCase("Linear"),
            1, 5, 1.5f, 0.1f
    );

    final BooleanSupplier modelVisible = () -> smoothMode.getMode().equalsIgnoreCase("AIModel");
    public final ModeSetting model = new ModeSetting("AIModel", this, modelVisible);
    final FloatSetting yawMultiplier = new FloatSetting("YawMultiplier", this, modelVisible, 0.5f, 2, 1, 0.1f);
    final FloatSetting pitchMultiplier = new FloatSetting("PitchMultiplier", this, modelVisible, 0.5f, 2, 1, 0.1f);

    final BooleanSetting correction = new BooleanSetting("Correction", this, modelVisible);
    final BooleanSupplier correctionVisible = () -> modelVisible.getAsBoolean() && correction.isToggled();
    final IntegerSetting yawCorrectionSpeed = new IntegerSetting("YawCorrectionSpeed", this, correctionVisible, 0, 180, 90);
    final IntegerSetting pitchCorrectionSpeed = new IntegerSetting("PitchCorrectionSpeed", this, correctionVisible, 0, 180, 30);

    final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 8, 6f, 0.1f);
    final IntegerSetting minCPS = new IntegerSetting("MinCPS", this, 0, 20, 17) {
        @Override
        public int getValue() {
            if (maxCPS.value < value) { value = maxCPS.value; }
            return value;
        }
    };
    final IntegerSetting maxCPS = new IntegerSetting("MaxCPS", this, 0, 20, 17) {
        @Override
        public int getValue() {
            if (minCPS.value > value) { value = minCPS.value; }
            return value;
        }
    };

    final ModeSetting moveFix = new ModeSetting("MoveFix", this)
            .addModes("OFF", "Legit", "Silent", "Target")
            .setMode("Silent");

    final StopWatch clickTimer = new StopWatch();
    private long delay;

    public KillAura() {
        updateModels();
    }

    @Override
    public void onEnable() {
        updateModels();
    }

    @Override
    public void onDisable() {
        Client.INSTANCE.getCombatManager().setTarget(null);
    }

    @EventTarget
    public void onEvent(Event event) {
        CombatManager combatManager = Client.INSTANCE.getCombatManager();
        if (event instanceof TickEvent) combatManager.setTarget(findNewTarget());
        if (combatManager.getTarget() == null) return;
        EntityLivingBase target = combatManager.getTarget();
        Rotation lr = Rotation.getServerRotation().copy();
        if (DistanceUtils.getDistance(target) < rotationDistance.getValue()) {
            if (event instanceof MotionEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());

                AxisAlignedBB box = getHitBox(target);
                Rotation needRotation = switch (hitVec.getMode()) {
                    case "Best" -> RotationUtils.getBestRotation(box);
                    case "Nearest" -> RotationUtils.getNearestRotation(lr, box);
                    case "Head" -> RotationUtils.getRotationToPoint(target.getPositionEyes(1f));
                    case "Body" -> RotationUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                    default -> throw new IllegalStateException("Unexpected value: " + hitVec.getMode());
                };

                Rotation delta = RotationUtils.getDelta(lr, needRotation);
                Rotation speed = new Rotation(
                        RandomUtils.nextFloat(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                        RandomUtils.nextFloat(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
                );

                RotationUtils.limitDelta(delta, speed);

                switch (smoothMode.getMode()) {
                    case "Linear" -> {
                        delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / linearSmoothStrength.getValue()));
                        delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / linearSmoothStrength.getValue()));
                    }
                    case "AIModel" -> {
                        if (!AIRotationSmooth.currentModelName.equalsIgnoreCase(model.getMode())) {
                            AIRotationSmooth.changeModel(model.getMode());
                        }

                        Rotation targetRot = lr.add(delta);

                        Rotation aiRotation = AIRotationSmooth.compute(
                                lr, targetRot, target,
                                yawMultiplier.getValue(), pitchMultiplier.getValue(),
                                correction.isToggled(), yawCorrectionSpeed.getValue(), pitchCorrectionSpeed.getValue()
                        );

                        delta = RotationUtils.getDelta(lr, aiRotation);
                    }
                }

                RotationUtils.limitDelta(delta, speed);
                RotationUtils.fixDelta(delta);

                lr = lr.add(delta);
                lr.setPitch(Math.clamp(lr.getPitch(), -90, 90));
                Rotation.setServerRotation(lr);
            }
        }
        if (event instanceof RunGameLoopEvent && DistanceUtils.getDistance(target) < clickDistance.getValue()) {
            if (clickTimer.reachedMS(delay)) {
                clickTimer.reset();
                double cps = RandomUtils.nextDouble(minCPS.getValue(), maxCPS.getValue());
                if (cps == 0) {
                    return;
                }
                delay = (long) (1000d / cps);
                Client.INSTANCE.getClickManager().addClick();
            }
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
            if (event instanceof MoveFlyingEvent e) {
                e.setYaw(lr.getYaw());
            }
            if (event instanceof JumpEvent e) {
                e.setYaw(lr.getYaw());
            }
        }
        if (event instanceof MoveEvent e) {
            switch (moveFix.getMode()) {
                case "Silent" -> MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                case "Target" -> MoveUtils.moveFix(e, RotationUtils.getRotationToPoint(target.getPositionVector()).getYaw());
            }
        }
    }

    private AxisAlignedBB getHitBox(EntityLivingBase target) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double horizontalPercent = 0.5 + horizontalHitBoxSize.getValue() / 200d;
        double verticalPercent = 0.5 + verticalHitBoxSize.getValue() / 200d;

        double invertHorizontalPercent = 1 - horizontalPercent;
        double invertVerticalPercent = 1 - verticalPercent;

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * invertHorizontalPercent,
                box.minY + box.getLengthY() * invertVerticalPercent,
                box.minZ + box.getLengthZ() * invertHorizontalPercent,
                box.minX + box.getLengthX() * horizontalPercent,
                box.minY + box.getLengthY() * verticalPercent,
                box.minZ + box.getLengthZ() * horizontalPercent
        );
        return box;
    }

    private EntityLivingBase findNewTarget() {
        EntityLivingBase target = null;

        double bestValue = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (DistanceUtils.getDistance(entity) > findDistance.getValue()) continue;
            if (!(entity instanceof EntityLivingBase ent)) continue;
            if (entity instanceof EntityPlayer player && player.isFriend()) continue;
            switch (ent) {
                case EntityPlayer _ when !targets.get("Players") -> {
                    continue;
                }
                case EntityMob _ when !targets.get("Mobs") -> {
                    continue;
                }
                case EntityAnimal _ when !targets.get("Animals") -> {
                    continue;
                }
                case EntityVillager _ when !targets.get("Villagers") -> {
                    continue;
                }
                default -> {
                }
            }

            double value = Double.MAX_VALUE;

            switch (sortType.getMode()) {
                case "Distance" -> value = DistanceUtils.getDistance(entity);
                case "FOV" -> value = RotationUtils.getFovToEntity(entity);
            }

            if (value < bestValue) {
                bestValue = value;
                target = ent;
            }
        }

        return target;
    }

    private void updateModels() {
        model.getModes().clear();
        for (File modelFile : Client.INSTANCE.getModelsDirectory().listFiles()) {
            model.getModes().add(modelFile.getName().replaceAll(".params", ""));
        }
    }
}
