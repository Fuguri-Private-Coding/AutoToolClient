package me.hackclient.module.impl.combat;

import me.hackclient.Client;
import me.hackclient.combatmanager.CombatManager;
import me.hackclient.combatmanager.TargetFinder;
import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.combat.killaura.rotation.KillAuraRotation;
import me.hackclient.module.impl.combat.killaura.rotation.impl.IntaveRotation;
import me.hackclient.module.impl.combat.killaura.rotation.impl.TestRotation;
import me.hackclient.module.impl.combat.killaura.rotation.impl.VanillaRotation;
import me.hackclient.module.impl.visual.Animations;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.util.MathHelper;

@ModuleInfo(
        name = "KillAura",
        category = Category.COMBAT
)
public class KillAura extends Module {

    final StopWatch stopWatch;

    // Настройки поиска противника
    final FloatSetting distance = new FloatSetting("Distance", this, 3.0f, 6.0f, 6.0f, 0.1f) {};
    final BooleanSetting players = new BooleanSetting("Players", this, true);
    final BooleanSetting animals = new BooleanSetting("Animals", this, false);
    final BooleanSetting mobs = new BooleanSetting("Mobs", this, false);

    // Настройки ротации
    final ModeSetting rotationMode = new ModeSetting(
            "RotationMode",
            this,
            "Intave",
            new String[] {
                    "Vanilla",
                    "Test",
                    "Intave"
            }
    );


    // Клики
    final IntegerSetting minCPS = new IntegerSetting("MinCPS", this, 0, 20, 17);
    final IntegerSetting maxCPS = new IntegerSetting("MaxCPS", this, 0, 20, 17);


    // Ротация
    final IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 0, 180, 90);
    final IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 0, 180, 30);

    // Мувмент
    final BooleanSetting moveFix = new BooleanSetting("MoveFix", this, true);
    final BooleanSetting silent = new BooleanSetting("Silent", this, moveFix::isToggled, true);
    final BooleanSetting jumpFix = new BooleanSetting("JumpFix", this, true);

    public KillAura() {
        stopWatch = new StopWatch();
    }

    CombatManager combatManager = Client.INSTANCE.getCombatManager();

    @Override
    public void onDisable() {
        super.onDisable();
        Animations.setAnimate(false);
        combatManager.setTarget(null);
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent) {
            combatManager.setTarget(TargetFinder.findTarget(distance.getValue(), players.isToggled(), mobs.isToggled(), animals.isToggled()));
        }
        Animations.setAnimate(combatManager.getTarget() != null);
        if (combatManager.getTarget() == null) { return; }
        if (event instanceof RunGameLoopEvent && mc.currentScreen == null) {
            int randomizeCPS = RandomUtils.nextInt(minCPS.getValue(), maxCPS.getValue());
            if (stopWatch.reachedMS() >= 1000 / randomizeCPS) {
                Client.INSTANCE.getClickManager().addClick();
                stopWatch.reset();
            }
        }
        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
            motionEvent.setPitch(Rotation.getServerRotation().getPitch());

            KillAuraRotation rotation = switch (rotationMode.getMode()) {
                case "Test" -> new TestRotation();
                case "Vanilla" -> new VanillaRotation();
                case "Intave" -> new IntaveRotation();
                default -> throw new IllegalStateException("Unexpected value: " + rotationMode.getMode());
            };

            if (mc.currentScreen == null) {
                Rotation.setServerRotation(rotation.compute(
                        Rotation.getServerRotation(),
                        combatManager.getTarget(),
                        yawSpeed.getValue() + RandomUtils.nextFloat(0, 10), pitchSpeed.getValue() + RandomUtils.nextFloat(0, 10)
                ));
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
                if (silent.isToggled()) {
                    moveFlyingEvent.setCanceled(true);
                    MoveUtils.silentMoveFix(moveFlyingEvent);
                } else {
                    moveFlyingEvent.setYaw(Rotation.getServerRotation().getYaw());
                }
            }
            if (event instanceof SprintEvent && silent.isToggled()) {
                if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
                    mc.thePlayer.setSprinting(false);
                }
            }
            if (event instanceof JumpEvent jumpEvent && jumpFix.isToggled()) {
                jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
            }
        }
    }
}
