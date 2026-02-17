package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Player;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.scaffold.RotationData;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.ItemUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.input.Mouse;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Scaffold", category = Category.PLAYER, description = "Позволяет ХАЛЯЛЬНО строится.")
public class Scaffold extends Module {

    private final Mode rotMode = new Mode("RotationMode", this)
            .addModes("TellyBridge", "Normal")
            .setMode("Normal");

    private final BooleanSupplier tellyVisible = () -> rotMode.is("TellyBridge");
    private final BooleanSupplier normalVisible = () -> rotMode.is("Normal");

    private final DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0,180,90,1);
    private final DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0,180,90,1);

    private final DoubleSlider yawClutchSpeed = new DoubleSlider("YawClutchSpeed", this, normalVisible, 0,180,90,1);
    private final DoubleSlider pitchClutchSpeed = new DoubleSlider("PitchClutchSpeed", this, normalVisible, 0,180,90,1);

    private final DoubleSlider pitchCorrectionSearchRange = new DoubleSlider("PitchCorrectionSearchRange", this, 0,90,90,0.1f);
    private final FloatSetting pitchCorrectionMinStep = new FloatSetting("PitchCorrectionMinStep", this, 0.3f, 10, 1f, 0.01f);

    private final CheckBox sortYawOffset = new CheckBox("SortYawOffset", this);
    private final FloatSetting yawOffset = new FloatSetting("YawOffset", this, () -> sortYawOffset.isToggled() || normalVisible.getAsBoolean(), 0, 90, 45, 0.1f);

    private final MultiMode removeSwing = new MultiMode("RemoveSwing", this)
        .addModes("On Client", "On Server")
        ;

    private final Mode sprintMode = new Mode("SprintMode", this)
        .addModes("None", "Legit", "JumpSprint", "AllDirection")
        .setMode("Legit")
        ;

    private final CheckBox rotateWithMovement = new CheckBox("RotateWithMovement", this);
    private final CheckBox strictYaw = new CheckBox("StrictYaw", this);

    private final CheckBox clutch = new CheckBox("Clutch", this, normalVisible);

    private final FloatSetting minDistanceToClutch = new FloatSetting("MinDistanceToClutch", this, () -> normalVisible.getAsBoolean() && clutch.isToggled(), 3, 6, 3.7f, 0.1f);

    private final CheckBox speedTelly = new CheckBox("SpeedTelly", this, tellyVisible, true);
    private final DoubleSlider airTicks = new DoubleSlider("AirTicks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 0,10,3,1);

    private final CheckBox flick = new CheckBox("Flick", this, tellyVisible, false);

    private final CheckBox sameY = new CheckBox("SameY", this, true);

    private final MultiMode sneakIf = new MultiMode("SneakIf", this, normalVisible)
        .addModes("Rotate", "ZeroBlocks", "NinjaBridge");
    
    private final FloatSetting minDeltaToSneak = new FloatSetting("MinDeltaToSneak", this, () -> sneakIf.get("Rotate") && normalVisible.getAsBoolean(), 0, 10, 2, 0.1f);
    private final CheckBox sneakIfRotateWithClutch = new CheckBox("SneakIfRotateWithClutch", this, () -> sneakIf.get("Rotate") && normalVisible.getAsBoolean());
    private final CheckBox sneakIfNinjaBridgeWithClutch = new CheckBox("SneakIfNinjaBridgeWithClutch", this, () -> sneakIf.get("NinjaBridge") && normalVisible.getAsBoolean());

    private final FloatSetting edgeOffset = new FloatSetting("EdgeOffset", this, () -> normalVisible.getAsBoolean() && sneakIf.get("NinjaBridge"), -0.1f,0.1f,0.05f, 0.01f);

    private final DoubleSlider cps = new DoubleSlider("CPS", this, 0, 80, 20, 1);

    private final CheckBox render = new CheckBox("Render", this, true);
    private final ColorSetting color = new ColorSetting("Color", this);

    private final CheckBox glow = new CheckBox("Glow", this);
    private final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    private Rot rotation, lastRotation, lastDelta = Rot.ZERO;

    private BlockPos targetBlock;

    int clicks = 0;

    private final StopWatch clickTimer = new StopWatch();
    private long clickDelay;

    @Override
    public void onDisable() {
        resetValues();
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer.isUsingItem()) {
            mc.thePlayer.clearItemInUse();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {

            rotate();
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = ItemUtils.findBlockInHotBar();

            if (slot != -1) {
                if (mc.thePlayer.inventory.currentItem != slot) {
                    mc.thePlayer.inventory.currentItem = slot;
                }
            }

            legitPlace();
        }

        if (event instanceof RunGameLoopEvent) {
            targetBlock = PlayerUtils.getPossibleBlockPos();
            if (clickTimer.reachedMS(clickDelay)) {
                clicks++;
                clickDelay = Math.round(1000f / cps.getRandomizedIntValue());
                clickTimer.reset();
            }
        }

        if (event instanceof Render3DEvent && targetBlock != null && render.isToggled()) {
            RenderUtils.start3D();

            if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(targetBlock, glowColor.getFadedFloatColor()));
            RenderUtils.drawBlockESP(targetBlock, color.getFadedFloatColor());

            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            float yaw = CameraRot.INST.getYaw();
            float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw), 45);

            float needYaw = strictYaw.isToggled() ? roundedYaw : yaw;

            MoveUtils.moveFix(e, MoveUtils.getDirection(needYaw, e.getForward(), e.getStrafe()));

            if (rotMode.is("Normal")) {
                if (sneakIf.get("Rotate") && lastDelta.hypot() > minDeltaToSneak.getValue()) {
                    if (isClutch() && !sneakIfRotateWithClutch.isToggled()) {
                        return;
                    }

                    e.setSneak(true);
                }

                if (sneakIf.get("ZeroBlocks") && ItemUtils.findBlockInHotBar() == -1) e.setSneak(true);

                if (sneakIf.get("NinjaBridge")) {
                    if (isClutch() && !sneakIfNinjaBridgeWithClutch.isToggled()) {
                        return;
                    }

                    BlockPos pos = MoveUtils.getDirectionalBlockPos(edgeOffset.getValue(), 0.7f);
                    if (mc.theWorld.isAirBlock(pos)) e.setSneak(true);
                }
            }
        }

        if (event instanceof SprintEvent e) {
            switch (sprintMode.getMode()) {
                case "AllDirection" -> {
                    if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isSneaking() && MoveUtils.isMoving()) {
                        e.setSprinting(true);
                    }
                }

                case "JumpSprint" -> e.setSprinting(mc.gameSettings.keyBindJump.isKeyDown() && MoveUtils.isMoving());
                case "None" -> e.setSprinting(false);
            }
        }

        if (event instanceof JumpEvent e) {
            float yaw = rotateWithMovement.isToggled() ? MoveUtils.getDir() : CameraRot.INST.getYaw();
            float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw), 45);

            float needYaw = strictYaw.isToggled() ? roundedYaw : yaw;

            e.setYaw(!sprintMode.is("JumpSprint") ? mc.thePlayer.rotationYaw : needYaw);
        }

        if (event instanceof ClickEvent e) {
            if (e.getButton() == ClickEvent.Button.RIGHT || e.getButton() == ClickEvent.Button.LEFT) {
                e.cancel();
            }
        }
    }

    private void resetValues() {
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
        CameraRot.INST.setWillChange(false);

        targetBlock = null;
    }

    void legitPlace() {
        RayTrace mouseOver = RayCastUtils.rayCast(6, 4.5f, mc.thePlayer.getRotation());

        int iters = clicks;
        clicks = 0;

        if (mouseOver.typeOfHit == RayTrace.RayType.BLOCK
            && isSameY(mouseOver)
            && targetBlock.equals(mouseOver.getBlockPos())
            && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {

            for (int i = 0; i < iters; i++) {
                mc.rightClickMouse(false);

                if (!removeSwing.get("On Client")) {
                    mc.thePlayer.swingItemNoPacket();
                }

                if (!removeSwing.get("On Server")) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }
        }
    }

    void rotate() {
        float yaw = rotateWithMovement.isToggled() ? MoveUtils.getDir() : CameraRot.INST.getYaw();
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw + 180), 45);

        boolean isOnRightSide = Math.floor(mc.thePlayer.posX + Math.cos(Math.toRadians(roundedYaw)) * 0.5) != Math.floor(mc.thePlayer.posX) ||
            Math.floor(mc.thePlayer.posZ + Math.sin(Math.toRadians(roundedYaw)) * 0.5) != Math.floor(mc.thePlayer.posZ);

        float needYaw = strictYaw.isToggled() ? roundedYaw - 180 : yaw;

        boolean isDiagonally = MoveUtils.isMoveDiagonally(needYaw);
        float offset = isDiagonally ? 0 : isOnRightSide ? yawOffset.getValue() : -yawOffset.getValue();

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly()) {
                    rotation = new Rot(needYaw, lastRotation.getPitch());
                } else {
                    rotation = getBestRotation(needYaw, offset, sortYawOffset.isToggled());
                }
            }

            case "Normal" -> {
                float rotYaw = MathHelper.wrapDegree(needYaw + 180 + offset);
                float rotPitch = getPitch(rotYaw, true);

                rotation = new Rot(rotYaw, rotPitch);
                RayTrace hit = RayCastUtils.rayCast(rotation, 4.5f);

                if ((hit.typeOfHit != RayTrace.RayType.BLOCK || isClutch()) && clutch.isToggled()) {
                    rotation = getBestRotation(0, 0, false);
                }
            }
        }

        if (!MoveUtils.isMoving() && !isClutch() && mc.gameSettings.keyBindJump.isKeyDown()
            && mc.thePlayer.motionX == 0 && mc.thePlayer.motionZ == 0) {
            rotation.setPitch(90);
        }

        Rot delta = RotUtils.getDelta(mc.thePlayer.getRotation(), rotation);
        Rot speed = getDeltaSpeed();

        delta = delta.limit(speed);
        delta = RotUtils.fixDelta(delta);

        lastDelta = delta.copy();

        CameraRot.INST.setUnlocked(true);
        mc.thePlayer.moveRotation(delta);
    }

    boolean isClutch() {
        return (Player.isClutch() || DistanceUtils.getDistance(targetBlock) > minDistanceToClutch.getValue()) && clutch.isToggled();
    }

    boolean isSameY(RayTrace mouse) {
        if (sameY.isToggled()) {
            if (Mouse.isButtonDown(1)) return mouse.sideHit != EnumFacing.DOWN;
            return mouse.sideHit != EnumFacing.DOWN && mouse.sideHit != EnumFacing.UP;
        }
        return mouse.sideHit != EnumFacing.DOWN;
    }

    boolean isTelly() {
        if (MoveUtils.isMoving()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                int needAirTicks = (speedTelly.isToggled() ? 0 : airTicks.getRandomizedIntValue());
                return mc.thePlayer.onGround || Player.airTicks < needAirTicks;
            } else {
                BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1f, mc.thePlayer.posZ);
                return !mc.theWorld.isAirBlock(pos) && flick.isToggled();
            }
        }
        return false;
    }

    Rot getDeltaSpeed() {
        Rot speed = Rot.ZERO;

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly()) {
                    speed.setYaw(180);
                    speed.setPitch(180);
                } else {
                    speed.setYaw(yawSpeed.getRandomizedIntValue());
                    speed.setPitch(pitchSpeed.getRandomizedIntValue());
                }
            }

            case "Normal" -> {
                if (isClutch()) {
                    speed.setYaw(yawClutchSpeed.getRandomizedIntValue());
                    speed.setPitch(pitchClutchSpeed.getRandomizedIntValue());
                } else {
                    speed.setYaw(yawSpeed.getRandomizedIntValue());
                    speed.setPitch(pitchSpeed.getRandomizedIntValue());
                }
            }
        }

        return speed;
    }

    private float getPitch(float yaw, boolean handleMouse) {
        List<RotationData> dataList = new ArrayList<>();

        float minPitch = (float) pitchCorrectionSearchRange.getMinValue();
        float maxPitch = (float) pitchCorrectionSearchRange.getMaxValue();
        float step = pitchCorrectionMinStep.getValue();

        for (float pitch = minPitch; pitch <= maxPitch; pitch += step) {
            Rot rot = new Rot(yaw, pitch);

            RayTrace hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotationData data = new RotationData(rot, hit);
                if (isOverBlock(hit, targetBlock)) dataList.add(data);
            }
        }

        if (dataList.isEmpty()) return lastRotation.getPitch();

        dataList.sort(Comparator.comparingDouble(data -> Math.abs(mc.thePlayer.rotationPitch) - data.rotation().getPitch()));

        RotationData rotationData = dataList.getFirst();
        if (handleMouse) lastRotation = rotationData.rotation();

        return rotationData.rotation().getPitch();
    }

    private Rot getBestRotation(float yawOffset, float offset, boolean sortOffset) {
        float step = 5;
        List<RotationData> validRotations = new ArrayList<>();

        float finalOffsetYaw = yawOffset - 180 + offset;

        for (float possibleYaw = 0; possibleYaw < 360; possibleYaw += step) {
            float yaw = MathHelper.wrapDegree(possibleYaw);
            float pitch = getPitch(yaw, false);

            Rot rot = new Rot(yaw, pitch);

            RayTrace hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotationData data = new RotationData(rot, hit);
                if (isOverBlock(hit, targetBlock)) validRotations.add(data);
            }
        }

        if (validRotations.isEmpty()) {
            if (sortOffset) lastRotation = new Rot(finalOffsetYaw, lastRotation.getPitch());
            return lastRotation;
        }

        validRotations.sort(Comparator.comparingDouble(data -> {
            double sortYawOffset = sortOffset ? finalOffsetYaw : mc.thePlayer.rotationYaw;
            double sortDistance = sortOffset ? 0 : DistanceUtils.getDistance(data.hit().hitVec) * 50;

            double yawDiff = MathHelper.wrapDegree(sortYawOffset - data.rotation().getYaw());
            double pitchDiff = Math.abs(mc.thePlayer.rotationPitch - data.rotation().getPitch());

            return Math.hypot(yawDiff, pitchDiff) + sortDistance;
        }));

        RotationData rotationData = validRotations.getFirst();

        lastRotation = rotationData.rotation();
        return lastRotation;
    }

    private boolean isOverBlock(RayTrace hit, BlockPos targetBlock) {
        return hit.typeOfHit == RayTrace.RayType.BLOCK && hit.hitVec.yCoord < mc.thePlayer.posY && hit.getBlockPos().equals(targetBlock) && hit.sideHit != EnumFacing.DOWN;
    }
}