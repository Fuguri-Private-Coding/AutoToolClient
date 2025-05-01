package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.managers.CombatManager;
import me.hackclient.module.impl.combat.killaura.rotation.impl.*;
import me.hackclient.utils.target.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.module.impl.visual.Animations;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;

@ModuleInfo(name = "KillAura", category = Category.COMBAT)
public class KillAura extends Module {

    final StopWatch stopWatch;

    private final IntaveRotation intaveRotation = new IntaveRotation();
    private final VanillaRotation vanillaRotation = new VanillaRotation();
    private final ChaGPTRotation chaGPTRotation = new ChaGPTRotation();

    final FloatSetting findDistance = new FloatSetting("FindDistance", this, 3.0f, 8.0f, 6.0f, 0.1f);

    final BooleanSetting players = new BooleanSetting("Players", this, true);
    final BooleanSetting animals = new BooleanSetting("Animals", this, false);
    final BooleanSetting mobs = new BooleanSetting("Mobs", this, false);

    final ModeSetting rotationMode = new ModeSetting(
            "RotationMode",
            this,
            "Intave",
            new String[] {
                    "Vanilla",
                    "Intave"
            }
    );

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

    final FloatSetting smooth = new FloatSetting("Smooth", this, 1f,10f,2f,0.1f);

    BooleanSetting randomizeWithAIModel = new BooleanSetting("RandomizeWithAIModel", this, true);
    BooleanSetting additionalCorrection = new BooleanSetting("AdditionalCorrection", this, randomizeWithAIModel::isToggled, true);

    public ModeSetting aiModel = new ModeSetting("AIModel", this,randomizeWithAIModel::isToggled, "louf", new String[] {"louf","test1", "test2"});

    final FloatSetting yawMultiplier = new FloatSetting("YawMultiplier", this,randomizeWithAIModel::isToggled, 0.1f,2f,1f,0.05f);
    final FloatSetting pitchMultiplier = new FloatSetting("PitchMultiplier", this,randomizeWithAIModel::isToggled, 0.1f,2f,1f,0.05f);

    final FloatSetting yawCorrectionSpeed = new FloatSetting("YawCorrectionSpeed", this,additionalCorrection::isToggled, 0f,10f,3f,0.1f);
    final FloatSetting pitchCorrectionSpeed = new FloatSetting("PitchCorrectionSpeed", this,additionalCorrection::isToggled, 0f,10f,1.5f,0.1f);

    final FloatSetting swingDistance = new FloatSetting("SwingDistance", this, 3.0f, 6.0f, 6.0f, 0.1f);

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

    final BooleanSetting fakeBlock = new BooleanSetting("FakeBlock", this, true);

    final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    final BooleanSetting silent = new BooleanSetting("Silent", this, moveFix::isToggled, true);
    final BooleanSetting jumpFix = new BooleanSetting("JumpFix", this, true);

    public KillAura() {
        stopWatch = new StopWatch();
    }

    CombatManager combatManager = Client.INSTANCE.getCombatManager();

    float randomizedCps = RandomUtils.nextFloat(minCPS.getValue(), maxCPS.getValue());
    float randomizeYawSpeed = RandomUtils.nextFloat(minYawSpeed.getValue(), maxYawSpeed.getValue());
    float randomizePitchSpeed = RandomUtils.nextFloat(minPitchSpeed.getValue(), maxPitchSpeed.getValue());

    @Override
    public void onDisable() {
        super.onDisable();
        Animations.setAnimate(false);
        combatManager.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) combatManager.setTarget(TargetFinder.findTarget(findDistance.getValue(), players.isToggled(), mobs.isToggled(), animals.isToggled()));

        if (event instanceof RunGameLoopEvent) {
            boolean haveTarget = combatManager.getTarget() != null;
            double distanceToTarget = haveTarget ? DistanceUtils.getDistance(combatManager.getTarget()) : swingDistance.getValue();
            boolean needSwing = haveTarget && distanceToTarget < swingDistance.getValue();

            Animations.setAnimate(haveTarget && needSwing && fakeBlock.isToggled());
            if (stopWatch.reachedMS() >= 1000 / randomizedCps && mc.currentScreen == null && haveTarget && needSwing) {
                Client.INSTANCE.getClickManager().addClick();
                stopWatch.reset();
                randomizedCps = RandomUtils.nextFloat(minCPS.getValue(), maxCPS.getValue());
            }
        }

        if (combatManager.getTarget() != null) {
            if (event instanceof MotionEvent motionEvent) {
                motionEvent.setYaw(Rotation.getServerRotation().getYaw());
                motionEvent.setPitch(Rotation.getServerRotation().getPitch());

                if (mc.currentScreen == null) {
                    KillAuraRotation rotation = switch (rotationMode.getMode()) {
                        case "Vanilla" -> vanillaRotation;
                        case "Intave" -> intaveRotation;
                        default -> throw new IllegalStateException("Unexpected value: " + rotationMode.getMode());
                    };

                    Rotation targetRot = rotation.compute(
                            Rotation.getServerRotation(),
                            combatManager.getTarget(),
                            randomizeYawSpeed, randomizePitchSpeed,
                            smooth.getValue(),
                            yawMultiplier.getValue(), pitchMultiplier.getValue()
                    );

                    if (randomizeWithAIModel.isToggled()) {
                        if (!chaGPTRotation.currentModelName.equalsIgnoreCase(aiModel.getMode())) chaGPTRotation.changeModel(aiModel.getMode());
                        targetRot = chaGPTRotation.compute(
                                Rotation.getServerRotation(),
                                targetRot,
                                combatManager.getTarget(),
                                yawMultiplier.getValue(), pitchMultiplier.getValue(),
                                additionalCorrection.isToggled(),
                                yawCorrectionSpeed.getValue(), pitchCorrectionSpeed.getValue()
                        );
                    }
                    Rotation.setServerRotation(targetRot);
                }
            }
            if (event instanceof LookEvent lookEvent) {
                lookEvent.setYaw(Rotation.getServerRotation().getYaw());
                lookEvent.setPitch(Rotation.getServerRotation().getPitch());
            }

            if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
                changeHeadRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
                changeHeadRotationEvent.setPitch(Rotation.getServerRotation().getPitch());
            }

            if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
                UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
            }

            if (moveFix.isToggled()) {
                if (event instanceof MoveFlyingEvent moveFlyingEvent) {
                    moveFlyingEvent.setYaw(Rotation.getServerRotation().getYaw());
                }
                if (event instanceof MoveEvent moveEvent && silent.isToggled()) {
                    MoveUtils.moveFix(moveEvent, Rotation.getServerRotation().getYaw());
                }

                if (event instanceof SprintEvent) {
                    if (Math.abs(MoveUtils.getDirection() - MoveUtils.getDirection(Rotation.getServerRotation().getYaw())) > 45) {
                        mc.thePlayer.setSprinting(false);
                    } else if (MoveUtils.canSprint()) {
                        mc.thePlayer.setSprinting(true);
                    }
                }

                if (event instanceof JumpEvent jumpEvent && jumpFix.isToggled()) {
                    jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return String.valueOf(combatManager.getTarget() == null ? "" : combatManager.getTarget().getName());
    }
}
