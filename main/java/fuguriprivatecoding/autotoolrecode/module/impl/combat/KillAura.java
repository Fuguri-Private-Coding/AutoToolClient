package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.deeplearn.rotation.AIRotationSmooth;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.managers.CombatManager;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.io.File;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 8, 6, 0.1f);
    final FloatSetting rotateDistance = new FloatSetting("RotateDistance", this, 3,8,6,0.1f);
    final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 8, 6f, 0.1f);

    final MultiMode targets = new MultiMode("Targets", this)
            .addModes("Players","Mobs","Animals","Villagers");

    final Mode sortType = new Mode("SortType", this)
            .addModes("Distance", "FOV", "HurtTime", "Switch")
            .setMode("FOV");

    final Mode hitVec = new Mode("HitVec", this)
            .addModes("Best", "Nearest", "Head", "Body")
            .setMode("Best");

    final BooleanSupplier hitBoxSizeVisible = () -> hitVec.getMode().equalsIgnoreCase("Best") || hitVec.getMode().equalsIgnoreCase("Nearest");
    final IntegerSetting horizontalHitBoxSize = new IntegerSetting("HorizontalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);
    final IntegerSetting verticalHitBoxSize = new IntegerSetting("VerticalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);

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

    final CheckBox gcd = new CheckBox("GCD (FIX)", this);

    final Mode smoothMode = new Mode("SmoothMode", this)
            .addModes("Linear", "AIModel")
            .setMode("Linear");

    final FloatSetting linearSmoothStrength = new FloatSetting(
            "LinearSmoothStrength", this,
            () -> smoothMode.getMode().equalsIgnoreCase("Linear"),
            1, 5, 1.5f, 0.1f
    );

    final BooleanSupplier modelVisible = () -> smoothMode.getMode().equalsIgnoreCase("AIModel");

    public final Mode model = new Mode("AIModel", this, modelVisible);
    final FloatSetting yawMultiplier = new FloatSetting("YawMultiplier", this, modelVisible, 0.5f, 2, 1, 0.1f);
    final FloatSetting pitchMultiplier = new FloatSetting("PitchMultiplier", this, modelVisible, 0.5f, 2, 1, 0.1f);

    final CheckBox correction = new CheckBox("Correction", this, modelVisible);
    final BooleanSupplier correctionVisible = () -> modelVisible.getAsBoolean() && correction.isToggled();
    final IntegerSetting yawCorrectionSpeed = new IntegerSetting("YawCorrectionSpeed", this, correctionVisible, 0, 180, 90);
    final IntegerSetting pitchCorrectionSpeed = new IntegerSetting("PitchCorrectionSpeed", this, correctionVisible, 0, 180, 30);

    final CheckBox lockView = new CheckBox("LockView", this);

    final IntegerSetting minCPS = new IntegerSetting("MinCPS", this, 1, 40, 17) {
        @Override
        public int getValue() {
            if (maxCPS.value < value) { value = maxCPS.value; }
            return value;
        }
    };
    final IntegerSetting maxCPS = new IntegerSetting("MaxCPS", this, 1, 40, 17) {
        @Override
        public int getValue() {
            if (minCPS.value > value) { value = minCPS.value; }
            return value;
        }
    };

    final Mode moveFix = new Mode("MoveFix", this)
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
        Client.INST.getCombatManager().setTarget(null);
    }

    @EventTarget
    public void onEvent(Event event) {
        CombatManager combatManager = Client.INST.getCombatManager();
        if (event instanceof TickEvent) combatManager.setTarget(findNewTarget());
        if (combatManager.getTarget() == null) return;
        EntityLivingBase target = combatManager.getTarget();
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
        if (event instanceof RunGameLoopEvent && DistanceUtils.getDistance(target) < clickDistance.getValue()) {
            if (clickTimer.reachedMS(delay)) {
                clickTimer.reset();
                Client.INST.getClickManager().addClick();
                delay = Math.round(1000f / RandomUtils.nextFloat(minCPS.getValue(), maxCPS.getValue()));
            }
        }
        if (DistanceUtils.getDistance(target) < rotateDistance.getValue()) {
            Rot lr = Rot.getServerRotation().copy();

            if (event instanceof MotionEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());
                AxisAlignedBB box = getHitBox(target);
                Rot needRotation = switch (hitVec.getMode()) {
                    case "Best" -> RotUtils.getBestRotation(box);
                    case "Nearest" -> RotUtils.getNearestRotations(lr, box);
                    case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
                    case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                    default -> throw new IllegalStateException("Unexpected value: " + hitVec.getMode());
                };

                if (needRotation == null) return;
                Rot delta = RotUtils.getDelta(lr, needRotation);
                Rot speed = new Rot(
                        RandomUtils.nextFloat(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                        RandomUtils.nextFloat(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
                );

                RotUtils.limitDelta(delta, speed);

                switch (smoothMode.getMode()) {
                    case "Linear" -> {
                        delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / linearSmoothStrength.getValue()));
                        delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / linearSmoothStrength.getValue()));
                    }
                    case "AIModel" -> {
                        if (model.getModes() == null) smoothMode.setMode("Linear");
                        if (AIRotationSmooth.currentModelName != null) {
                            if (!AIRotationSmooth.currentModelName.equalsIgnoreCase(model.getMode())) {
                                AIRotationSmooth.changeModel(model.getMode());
                            }

                            Rot targetRot = lr.add(delta);

                            Rot aiRotation = AIRotationSmooth.compute(
                                    lr, targetRot, target,
                                    yawMultiplier.getValue(), pitchMultiplier.getValue(),
                                    correction.isToggled(), yawCorrectionSpeed.getValue(), pitchCorrectionSpeed.getValue()
                            );

                            delta = RotUtils.getDelta(lr, aiRotation);
                        }
                    }
                }

                RotUtils.limitDelta(delta, speed);
                if (gcd.isToggled()) delta = RotUtils.fixDelta(delta);
                lr = lr.add(delta);
                lr.setPitch(Math.clamp(lr.getPitch(), -90, 90));
                Rot.setServerRotation(lr);

                if (lockView.isToggled()) {
                    mc.thePlayer.rotationYaw = Rot.getServerRotation().getYaw();
                    mc.thePlayer.rotationPitch = Rot.getServerRotation().getPitch();
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
                case EntityArmorStand _ -> {
                    continue;
                }
                default -> {}
            }

            double value = Double.MAX_VALUE;

            switch (sortType.getMode()) {
                case "Distance" -> {
                    value = DistanceUtils.getDistance(entity);
                }
                case "FOV" -> value = RotUtils.getFovToEntity(entity);
                case "HurtTime" -> value = ent.hurtTime;
                case "Switch" -> {
                    if (DistanceUtils.getDistance(entity) > 3) {
                        value = DistanceUtils.getDistance(entity);
                    } else if (DistanceUtils.getDistance(entity) < 3) {
                        value = ent.hurtTime;
                    }
                }
            }

            if (value < bestValue) {
                bestValue = value;
                target = ent;
            }
        }

        return target;
    }

    public void updateModels() {
        model.getModes().clear();
        if (Client.INST.getModelsDirectory().listFiles() != null) {
            for (File modelFile : Client.INST.getModelsDirectory().listFiles()) {
                model.getModes().add(modelFile.getName().replaceAll(".params", ""));
            }
        }
    }
}