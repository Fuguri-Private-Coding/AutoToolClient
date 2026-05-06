package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.RunGameLoopEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.*;
import fuguriprivatecoding.autotoolrecode.event.events.render.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.handle.Player;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.scaffold.RotData;
import fuguriprivatecoding.autotoolrecode.module.impl.player.scaffold.ScaffoldType;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.ItemUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.PlayerUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
import fuguriprivatecoding.autotoolrecode.utils.value.Constants;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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

    private final CheckBox sortYawOffset = new CheckBox("SortYawOffset", this, tellyVisible);
    private final FloatSetting yawOffset = new FloatSetting("YawOffset", this, () -> sortYawOffset.isToggled() || normalVisible.getAsBoolean(), 0, 90, 45, 0.1f);

    private final MultiMode removeSwing = new MultiMode("RemoveSwing", this)
        .addModes("On Client", "On Server")
        ;

    private final Mode sprintMode = new Mode("SprintMode", this)
        .addModes("None", "Legit", "JumpSprint", "AllDirection")
        .setMode("Legit")
        ;

    private final CheckBox sprintJumpOnGround = new CheckBox("SprintJumpOnGround", this, () -> sprintMode.is("JumpSprint"));

    private final IntegerSetting sprintTicksAfterGround = new IntegerSetting("SpringTicksAfterGround", this, () -> sprintMode.is("JumpSprint") && sprintJumpOnGround.isToggled(), 1, 10, 1);

    private final CheckBox startAtEdge = new CheckBox("StartAtEdge", this, normalVisible, false);
    private final FloatSetting startEdgeOffset = new FloatSetting("StartEdgeOffset", this, () -> normalVisible.getAsBoolean() && startAtEdge.isToggled(), -0.1f,0.1f,0.05f, 0.01f);

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

    private Rot rotation = Constants.ROT_ZERO, lastRotation = Constants.ROT_ZERO, lastDelta = Constants.ROT_ZERO;

    private BlockPos targetBlock = new BlockPos(0, 0, 0);

    private ScaffoldType type = ScaffoldType.NONACTIVE;

    private int clicks = 0, currentAirTicks = 0;

    private final StopWatch clickTimer = new StopWatch();
    private long clickDelay = 1L;

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
            targetBlock = PlayerUtils.getPossibleBlockPos();
            switch (type) {
                case ACTIVE -> rotate();
                case NONACTIVE -> updateScaffoldType();
            }
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = ItemUtils.findBlockInHotBar();

            if (mc.thePlayer.inventory.currentItem != slot && slot != -1) {
                mc.thePlayer.inventory.currentItem = slot;
            }

            if (type == ScaffoldType.ACTIVE) click();
        }

        if (event instanceof RunGameLoopEvent) {
            if (type == ScaffoldType.ACTIVE) updateClicks();
        }

        if (event instanceof DrawBlockHighlightEvent e) {
            e.cancel();
        }

        if (event instanceof Render3DEvent && targetBlock != null && render.isToggled()) {
            RenderUtils.start3D();

            if (glow.isToggled()) {
                BloomUtils.startWrite();
                RenderUtils.drawBlockESP(targetBlock, glowColor.getFadedFloatColor());
                BloomUtils.stopWrite();
            }
            RenderUtils.drawBlockESP(targetBlock, color.getFadedFloatColor());

            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(getMoveYaw(strictYaw.isToggled()), e.getForward(), e.getStrafe()));
        }

        if (event instanceof MoveButtonEvent e) {
            if (rotMode.is("Normal")) {
                if (sneakIf.get("Rotate") && lastDelta.length() > minDeltaToSneak.getValue()) {
                    if (!isClutch() || sneakIfRotateWithClutch.isToggled()) {
                        e.setSneak(true);
                    }
                }

                if (sneakIf.get("ZeroBlocks") && ItemUtils.findBlockInHotBar() == -1) e.setSneak(true);

                if (sneakIf.get("NinjaBridge")) {
                    if (!isClutch() || sneakIfNinjaBridgeWithClutch.isToggled()) {
                        BlockPos pos = MoveUtils.getDirectionalBlockPos(edgeOffset.getValue(), 0.5f);

                        if (mc.theWorld.isAirBlock(pos) && !shouldNinePitch()) {
                            e.setSneak(true);
                        }
                    }
                }
            }
        }

        if (event instanceof SprintEvent e && type == ScaffoldType.ACTIVE) {
            switch (sprintMode.getMode()) {
                case "AllDirection" -> {
                    if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isSneaking() && MoveUtils.isMoving()) {
                        e.setSprinting(true);
                    }
                }

                case "JumpSprint" -> {
                    boolean sprint = mc.gameSettings.keyBindJump.isKeyDown() && MoveUtils.isMoving();
                    e.setSprinting(sprintJumpOnGround.isToggled() ? sprint && Player.airTicks <= sprintTicksAfterGround.getValue() : sprint);
                }
                case "None" -> e.setSprinting(false);
            }
        }

        if (event instanceof JumpEvent e && type == ScaffoldType.ACTIVE) e.setYaw(getJumpYaw(strictYaw.isToggled(), rotateWithMovement.isToggled()));
        if (event instanceof ClickEvent e && (e.getButton() == ClickEvent.Button.RIGHT || e.getButton() == ClickEvent.Button.LEFT)) e.cancel();
    }

    private void click() {
        RayTrace hit = mc.rayTrace;

        int iters = clicks;
        clicks = 0;

        boolean oneClick = true;

        ItemStack heldStack = mc.thePlayer.getHeldItem();

        if (heldStack != null && heldStack.getItem() instanceof ItemBlock itemblock) {
            if (!itemblock.canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit, mc.thePlayer, heldStack)) {
                oneClick = false;
            }
        }

        if (hit.typeOfHit == RayTrace.RayType.BLOCK
            && (shouldNinePitch() || (isSameY(hit, sameY.isToggled()) && targetBlock.equals(hit.getBlockPos())))
            && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock && oneClick) {

            for (int i = 0; i < iters; i++) {
                mc.rightClickMouse(false);

                if (!removeSwing.get("On Client")) mc.thePlayer.swingItemNoPacket();
                if (!removeSwing.get("On Server")) mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
        }
    }

    private void rotate() {
        float yaw = rotateWithMovement.isToggled()
            ? MoveUtils.getDir()
            : CameraRot.INST.getYaw();

        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw + 180), 45);

        boolean isOnRightSide = MoveUtils.isOnRightSide(targetBlock, roundedYaw);

        float needYaw = strictYaw.isToggled() ? roundedYaw - 180 : yaw;

        boolean isDiagonally = MoveUtils.isMoveDiagonally(needYaw);
        float offset = isDiagonally ? 0 : isOnRightSide ? yawOffset.getValue() : -yawOffset.getValue();

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly(speedTelly.isToggled(), currentAirTicks)) {
                    currentAirTicks = airTicks.getRandomizedIntValue();
                    rotation = new Rot(needYaw, lastRotation.getPitch());
                } else {
                    rotation = getBestRotation(needYaw, offset, sortYawOffset.isToggled(), 5);
                }
            }

            case "Normal" -> {
                float rotYaw = MathHelper.wrapDegree(needYaw + 180 + offset);
                float rotPitch = getPitch(rotYaw, true);

                rotation = new Rot(rotYaw, rotPitch);
                RayTrace hit = RayCastUtils.rayCast(3f, 4.5f, rotation);

                if ((hit.typeOfHit != RayTrace.RayType.BLOCK || isClutch()) && clutch.isToggled()) {
                    rotation = getBestRotation(0, 0, false, 2);
                }
            }
        }

        if (shouldNinePitch()) rotation.setPitch(90);

        Rot delta = mc.thePlayer.getRotation().deltaTo(rotation);
        Rot speed = getDeltaSpeed();

        delta = delta.limit(speed);
        delta = delta.fix();

        lastDelta = delta.copy();

        CameraRot.INST.setUnlocked(true);
        mc.thePlayer.moveRotation(delta);
    }

    private float getMoveYaw(boolean strictYaw) {
        float yaw = CameraRot.INST.getYaw();
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw), 45);

        return strictYaw ? roundedYaw : yaw;
    }

    private float getJumpYaw(boolean strictYaw, boolean rotateWithMovement) {
        float yaw = rotateWithMovement ? MoveUtils.getDir() : CameraRot.INST.getYaw();
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw), 45);

        float needYaw = strictYaw ? roundedYaw : yaw;

        return !sprintMode.is("JumpSprint") && !sprintMode.is("NotFullyJumpSprint") ? mc.thePlayer.rotationYaw : needYaw;
    }

    private void updateClicks() {
        if (clickTimer.reachedMS(clickDelay)) {
            clicks++;
            clickDelay = Math.round(1000f / cps.getRandomizedIntValue());
            clickTimer.reset();
        }
    }

    private boolean shouldNinePitch() {
        return !MoveUtils.isMoving() && mc.gameSettings.keyBindJump.isKeyDown()
            && mc.thePlayer.motionX == 0 && mc.thePlayer.motionZ == 0;
    }

    private boolean isClutch() {
        return (Player.isClutch() || DistanceUtils.getDistance(targetBlock) > minDistanceToClutch.getValue()) && clutch.isToggled();
    }

    private boolean isSameY(RayTrace mouse, boolean sameY) {
        if (sameY) {
            if (Mouse.isButtonDown(1)) return mouse.sideHit != EnumFacing.DOWN;
            return mouse.sideHit != EnumFacing.DOWN && mouse.sideHit != EnumFacing.UP;
        }
        return mouse.sideHit != EnumFacing.DOWN;
    }

    private boolean isTelly(boolean speedTelly, int airTicks) {
        if (MoveUtils.isMoving()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                int needAirTicks = (speedTelly ? 0 : airTicks);
                return mc.thePlayer.onGround || Player.airTicks < needAirTicks;
            } else if (flick.isToggled()) {
                BlockPos pos = new BlockPos(mc.thePlayer.getPositionVector());
                return !mc.theWorld.isAirBlock(pos.add(0.0, -0.1, 0.0));
            }
        }
        return false;
    }

    private void updateScaffoldType() {
        if (rotMode.is("Normal") && startAtEdge.isToggled()) {
            if (type != ScaffoldType.ACTIVE) {
                BlockPos pos = MoveUtils.getDirectionalBlockPos(startEdgeOffset.getValue(), 0.7f);
                if (mc.theWorld.isAirBlock(pos)) type = ScaffoldType.ACTIVE;
            }
        } else {
            type = ScaffoldType.ACTIVE;
        }
    }

    private Rot getDeltaSpeed() {
        Rot speed = Constants.ROT_ZERO;

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly(speedTelly.isToggled(), currentAirTicks)) {
                    speed = Constants.ROT_MAX;
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
        List<RotData> dataList = new ArrayList<>();

        float minPitch = (float) pitchCorrectionSearchRange.getMinValue();
        float maxPitch = (float) pitchCorrectionSearchRange.getMaxValue();
        float step = pitchCorrectionMinStep.getValue();

        for (float pitch = minPitch; pitch <= maxPitch; pitch += step) {
            Rot rot = new Rot(yaw, pitch);

            RayTrace hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotData data = new RotData(rot, hit);
                if (isOverBlock(hit, targetBlock)) dataList.add(data);
            }
        }

        if (dataList.isEmpty()) return lastRotation.getPitch();

        dataList.sort(Comparator.comparingDouble(data -> Math.abs(mc.thePlayer.rotationPitch) - data.rotation().getPitch()));

        RotData rotData = dataList.getFirst();
        if (handleMouse) lastRotation = rotData.rotation();

        return rotData.rotation().getPitch();
    }

    private Rot getBestRotation(float yawOffset, float offset, boolean sortOffset, float step) {
        List<RotData> validRotations = new ArrayList<>();

        float finalOffsetYaw = yawOffset - 180 + offset;

        for (float possibleYaw = 0; possibleYaw < 360; possibleYaw += step) {
            float yaw = MathHelper.wrapDegree(possibleYaw);
            float pitch = getPitch(yaw, false);

            Rot rot = new Rot(yaw, pitch);

            RayTrace hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotData data = new RotData(rot, hit);
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

        RotData rotData = validRotations.getFirst();

        lastRotation = rotData.rotation();
        return lastRotation;
    }

    private boolean isOverBlock(RayTrace hit, BlockPos targetBlock) {
        return hit.typeOfHit == RayTrace.RayType.BLOCK && hit.hitVec.yCoord < mc.thePlayer.posY && hit.getBlockPos().equals(targetBlock) && hit.sideHit != EnumFacing.DOWN;
    }

    private void resetValues() {
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
        CameraRot.INST.setWillChange(false);

        targetBlock = null;
        lastDelta = Constants.ROT_ZERO;
        clickDelay = 1L;
        clickTimer.reset();
        type = ScaffoldType.NONACTIVE;
    }
}