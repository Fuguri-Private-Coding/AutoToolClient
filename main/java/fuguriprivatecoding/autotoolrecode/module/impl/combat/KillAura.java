package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "KillAura", category = Category.COMBAT, description = "Автоматически целится и бьет противника.")
public class KillAura extends Module {

    final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3, 8, 6, 0.1f);
    final FloatSetting rotateDistance = new FloatSetting("RotateDistance", this, 3, 8, 6, 0.1f);
    final FloatSetting clickDistance = new FloatSetting("ClickDistance", this, 3, 8, 6f, 0.1f);

    final MultiMode targets = new MultiMode("Targets", this)
        .addModes("Players", "Mobs", "Animals", "Villagers");

    final Mode sortType = new Mode("SortType", this)
        .addModes("Distance", "FOV", "HurtTime", "Switch")
        .setMode("FOV");

    final Mode hitVec = new Mode("HitVec", this)
        .addModes("Best", "Nearest", "Head", "Body")
        .setMode("Best");

    final BooleanSupplier hitBoxSizeVisible = () -> hitVec.getMode().equalsIgnoreCase("Best") || hitVec.getMode().equalsIgnoreCase("Nearest");
    final IntegerSetting horizontalHitBoxSize = new IntegerSetting("HorizontalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);
    final IntegerSetting verticalHitBoxSize = new IntegerSetting("VerticalHitBoxSize", this, hitBoxSizeVisible, 1, 100, 100);

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0, 180, 90, 1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0, 180, 90, 1);

    final CheckBox gcd = new CheckBox("GCD (FIX)", this);

    final CheckBox teleportPredictFix = new CheckBox("Teleport Predict Fix", this);

    final Mode smoothMode = new Mode("SmoothMode", this)
        .addModes("Linear")
        .setMode("Linear");

    BooleanSupplier linearVisible = () -> smoothMode.getMode().equalsIgnoreCase("Linear");

    DoubleSlider mixYawDelta = new DoubleSlider("Mix Yaw Delta", this, 0, 1, 1, 0.01f);
    DoubleSlider mixPitchDelta = new DoubleSlider("Mix Pitch Delta", this, 0, 1, 1, 0.01f);

    CheckBox basicRandomize = new CheckBox("Basic Randomize", this, linearVisible, false);
    FloatSetting randomizeStrength = new FloatSetting("Randomize Strength", this, () -> basicRandomize.isToggled() && linearVisible.getAsBoolean(), 0, 20, 5, 0.1f);

    final FloatSetting linearSmoothStrength = new FloatSetting(
        "LinearSmoothStrength", this,
        () -> smoothMode.getMode().equalsIgnoreCase("Linear"),
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
        Client.INST.getTargetStorage().setTarget(null);
    }

    @EventTarget
    public void onEvent(Event event) {
        TargetStorage targetStorage = Client.INST.getTargetStorage();
        if (event instanceof TickEvent) targetStorage.setTarget(findNewTarget());
        if (targetStorage.getTarget() == null) return;
        EntityLivingBase target = targetStorage.getTarget();
        if (Client.INST.getModules().getModule(Scaffold.class).isToggled()) return;

        if (event instanceof RunGameLoopEvent && DistanceUtils.getDistance(target) < clickDistance.getValue()) {
            if (TimerRange.balance == 0) {
                if (clickTimer.reachedMS(delay)) {
                    clickTimer.reset();
                    Client.INST.getClicks().addClick();
                    delay = Math.round(1000f / CPS.getRandomizedIntValue());
                }
            }
        }

        if (DistanceUtils.getDistance(target) < rotateDistance.getValue()) {
            Rot lr = Rot.getServerRotation();

            if (event instanceof TickEvent) {
                AxisAlignedBB box = getHitBox(target);
                Rot needRotation = TimerRange.teleporting && teleportPredictFix.isToggled() ?
                    RotUtils.getBestRotation(box.expand(0.1f,0f,0.1f)) :
                    switch (hitVec.getMode()) {
                    case "Best" -> RotUtils.getBestRotation(box.expand(0.1f,0.1f,0.1f));
                    case "Nearest" -> RotUtils.getNearestRotations(lr, box);
                    case "Head" -> RotUtils.getRotationToPoint(target.getPositionEyes(1f));
                    case "Body" -> RotUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.getEyeHeight() / 2f, target.posZ));
                    default -> throw new IllegalStateException("Unexpected value: " + hitVec.getMode());
                };

                if (needRotation == null) return;

                Rot delta = RotUtils.getDelta(lr, needRotation);

                Rot speed = new Rot(
                    TimerRange.teleporting && teleportPredictFix.isToggled() ? 180 : yawSpeed.getRandomizedIntValue(),
                    TimerRange.teleporting && teleportPredictFix.isToggled() ? 180 : pitchSpeed.getRandomizedIntValue()
                );

                RotUtils.limitDelta(delta, speed);

                switch (smoothMode.getMode()) {
                    case "Linear" -> {
                        if (TimerRange.teleporting && teleportPredictFix.isToggled()) break;

                        if (basicRandomize.isToggled()) {
                            Rot rot = new Rot(
                                needRotation.getYaw() - RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue()),
                                needRotation.getPitch() - RandomUtils.nextFloat(-randomizeStrength.getValue(), randomizeStrength.getValue())
                            );

                            delta = RotUtils.getDelta(lr, rot);
                        }

                        delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / linearSmoothStrength.getValue()));
                        delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / linearSmoothStrength.getValue()));
                    }
                }

                RotUtils.limitDelta(delta, speed);

                if (!TimerRange.teleporting) {
                    delta.setYaw(MathHelper.lerp((float) mixYawDelta.getRandomizedDoubleValue(), lastDelta.getYaw(), delta.getYaw()));
                    delta.setPitch(MathHelper.lerp((float) mixPitchDelta.getRandomizedDoubleValue(), lastDelta.getPitch(), delta.getPitch()));
                }

                lastDelta = new Rot(delta.getYaw(), delta.getPitch());

                if (gcd.isToggled()) delta = RotUtils.fixDelta(delta);
                lr = lr.add(delta);
                lr.setPitch(Math.clamp(lr.getPitch(), -90, 90));
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

    private AxisAlignedBB getHitBox(EntityLivingBase target) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double horizontalPercent = (TimerRange.teleporting && teleportPredictFix.isToggled() ? 100 : horizontalHitBoxSize.getValue()) / 200d;
        double verticalPercent = (TimerRange.teleporting && teleportPredictFix.isToggled() ? 100 : verticalHitBoxSize.getValue()) / 200d;

        Vec3 center = new Vec3(
            (box.maxX + box.minX) / 2,
            (box.maxY + box.minY) / 2,
            (box.maxZ + box.minZ) / 2
        );

        box = new AxisAlignedBB(
            center.xCoord - box.getLengthX() * horizontalPercent,
            center.yCoord - box.getLengthY() * verticalPercent,
            center.zCoord - box.getLengthZ() * horizontalPercent,
            center.xCoord + box.getLengthX() * horizontalPercent,
            center.yCoord + box.getLengthY() * verticalPercent,
            center.zCoord + box.getLengthZ() * horizontalPercent
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
                default -> {
                }
            }

            double value = Double.MAX_VALUE;

            switch (sortType.getMode()) {
                case "Distance" -> value = DistanceUtils.getDistance(entity);
                case "FOV" -> value = RotUtils.getFovToEntity(entity);
                case "HurtTime" -> value = DistanceUtils.getDistance(entity) + ent.hurtTime;
                case "Switch" -> {
                    if (DistanceUtils.getDistance(entity) > 3) {
                        value = DistanceUtils.getDistance(entity);
                    } else if (DistanceUtils.getDistance(entity) <= 3) {
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
}