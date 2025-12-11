package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
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
import net.minecraft.block.*;
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

    Mode rotMode = new Mode("RotationMode", this)
            .addModes("TellyBridge", "GodBridge", "Normal")
            .setMode("GodBridge");

    BooleanSupplier tellyVisible = () -> rotMode.is("TellyBridge");
    BooleanSupplier godVisible = () -> rotMode.is("GodBridge");
    BooleanSupplier godNormalVisible = () -> rotMode.is("GodBridge") || rotMode.is("Normal");
    BooleanSupplier tellyNormalVisible = () -> rotMode.is("TellyBridge") || rotMode.is("Normal");

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0,180,90,1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0,180,90,1);

    DoubleSlider yawClutchSpeed = new DoubleSlider("YawClutchSpeed", this, godVisible, 0,180,90,1);
    DoubleSlider pitchClutchSpeed = new DoubleSlider("PitchClutchSpeed", this, godVisible, 0,180,90,1);

    DoubleSlider pitchCorrectionSearchRange = new DoubleSlider("PitchCorrectionSearchRange", this, 0,90,90,0.1f);
    FloatSetting pitchCorrectionMinStep = new FloatSetting("PitchCorrectionMinStep", this, 0.3f, 10, 1f, 0.01f);

    CheckBox sortYawOffset = new CheckBox("SortYawOffset", this, tellyNormalVisible);
    FloatSetting yawOffset = new FloatSetting("YawOffset", this, () -> tellyNormalVisible.getAsBoolean() && sortYawOffset.isToggled(), 0, 90, 45, 0.1f);

    MultiMode removeSwing = new MultiMode("RemoveSwing", this)
        .addModes("On Client", "On Server")
        ;

    Mode sprintMode = new Mode("SprintMode", this)
        .addModes("None", "Legit", "JumpSprint", "AllDirection")
        .setMode("Legit")
        ;

    final CheckBox rotateWithMovement = new CheckBox("RotateWithMovement", this);
    final CheckBox strictYaw = new CheckBox("StrictYaw", this, tellyNormalVisible);

    final CheckBox speedTelly = new CheckBox("SpeedTelly", this, tellyVisible, true);
    DoubleSlider airTicks = new DoubleSlider("AirTicks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 0,10,3,1);

    final CheckBox sameY = new CheckBox("SameY", this, true);

    MultiMode sneakIf = new MultiMode("SneakIf", this, godNormalVisible)
        .addModes("Clutching", "Rotate", "Zero Blocks", "Ninja Bridge");

    final FloatSetting edgeOffset = new FloatSetting("EdgeOffset", this, () -> godNormalVisible.getAsBoolean() && sneakIf.get("Ninja Bridge"), 0f,0.1f,0.05f, 0.01f);

    final CheckBox render = new CheckBox("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    Rot rotation, lastRotation = Rot.ZERO;

    BlockPos targetBlock;

    double lastDelta = 0;

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

            rotate();
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot != -1) {
                if (mc.thePlayer.inventory.currentItem != slot) {
                    mc.thePlayer.inventory.currentItem = slot;
                }
            }

            legitPlace();
        }

        if (event instanceof Render3DEvent && targetBlock != null && render.isToggled()) {
            RenderUtils.start3D();

            if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(targetBlock, glowColor.getFadedFloatColor()));
            RenderUtils.drawBlockESP(targetBlock, color.getFadedFloatColor());

            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(CameraRot.INST.getYaw(), e.getForward(), e.getStrafe()));

            switch (rotMode.getMode()) {
                case "GodBridge", "Normal" -> {
                    if (sneakIf.get("Rotate") && lastDelta > 5) {
                        if (isClutch() && !sneakIf.get("Clutching")) {
                            return;
                        }

                        e.setSneak(true);
                    }

                    if (sneakIf.get("Zero Blocks") && findBlock() == -1) e.setSneak(true);

                    if (sneakIf.get("Ninja Bridge")) {
                        BlockPos pos = MoveUtils.getDirectionalBlockPos(edgeOffset.getValue(), 0.7f);
                        if (mc.theWorld.isAirBlock(pos)) e.setSneak(true);
                    }
                }
            }
        }

        if (event instanceof SprintEvent) {
            switch (sprintMode.getMode()) {
                case "AllDirection" -> {
                    if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isSneaking() && MoveUtils.isMoving()) {
                        mc.thePlayer.setSprinting(true);
                    }
                }

                case "JumpSprint" -> mc.thePlayer.setSprinting(mc.gameSettings.keyBindJump.isKeyDown() && MoveUtils.isMoving());
                case "None" -> mc.thePlayer.setSprinting(false);
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
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(6, 4.5f, mc.thePlayer.getRotation());

        if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
            && isSameY(mouseOver)
            && targetBlock.equals(mouseOver.getBlockPos())
            && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {

            mc.rightClickMouse(false);

            if (!removeSwing.get("On Client")) {
                mc.thePlayer.swingItemNoPacket();
            }

            if (!removeSwing.get("On Server")) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
        }
    }

    void rotate() {
        float yaw = rotateWithMovement.isToggled() ? MoveUtils.getDir() : CameraRot.INST.getYaw();
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw + 180), 45);

        boolean isOnRightSide = Math.floor(mc.thePlayer.posX + Math.cos(Math.toRadians(roundedYaw)) * 0.6) != Math.floor(mc.thePlayer.posX) ||
            Math.floor(mc.thePlayer.posZ + Math.sin(Math.toRadians(roundedYaw)) * 0.3) != Math.floor(mc.thePlayer.posZ);

        float needYaw = strictYaw.isToggled() ? roundedYaw - 180 : yaw;

        boolean isDiagonally = MoveUtils.isMoveDiagonally(needYaw);

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly()) {
                    rotation = new Rot(needYaw, lastRotation.getPitch());
                } else {
                    float offset = isDiagonally ? 0 : isOnRightSide ? yawOffset.getValue() : -yawOffset.getValue();

                    rotation = getBestRotation(needYaw, offset, sortYawOffset.isToggled());
                }
            }

            case "GodBridge" -> {
                if (isClutch()) {
                    rotation = getBestRotation(yaw, 0, false);
                } else {
                    MovingObjectPosition rightRayCast = RayCastUtils.rayCast(3,4.5, new Rot(roundedYaw + 45, getPitch(roundedYaw + 45, false)));
                    MovingObjectPosition leftRayCast = RayCastUtils.rayCast(3,4.5, new Rot(roundedYaw - 45, getPitch(roundedYaw - 45, false)));

                    boolean isLeftRayCastSide = leftRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK || rightRayCast.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
                    boolean isRightRayCastSide = rightRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK || leftRayCast.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;

                    boolean moveDiagonally = MoveUtils.isMoveDiagonally(roundedYaw);

                    float finallyYaw = MathHelper.wrapDegree(moveDiagonally ? roundedYaw : (roundedYaw + (isRightRayCastSide ? (isOnRightSide && !isLeftRayCastSide ? 45 : !isOnRightSide && isLeftRayCastSide ? -45 : 45) : -45)));

                    rotation = new Rot(finallyYaw, getPitch(finallyYaw, true));
                }
            }

            case "Normal" -> {
                float offset = isDiagonally ? 0 : isOnRightSide ? yawOffset.getValue() : -yawOffset.getValue();

                rotation = getBestRotation(needYaw, offset, sortYawOffset.isToggled());
            }
        }

        Rot delta = RotUtils.getDelta(mc.thePlayer.getRotation(), rotation);

        delta = getDeltaSpeed(delta);
        delta = RotUtils.fixDelta(delta);

        lastDelta = delta.hypot();

        CameraRot.INST.setUnlocked(true);
        mc.thePlayer.moveRotation(delta);
    }

    boolean isClutch() {
        return mc.thePlayer.hurtResistantTime > 0 || Player.airTicks > 12;
    }

    boolean isSameY(MovingObjectPosition mouse) {
        if (sameY.isToggled()) {
            if (Mouse.isButtonDown(1)) return mouse.sideHit != EnumFacing.DOWN;
            return mouse.sideHit != EnumFacing.DOWN && mouse.sideHit != EnumFacing.UP;
        }
        return mouse.sideHit != EnumFacing.DOWN;
    }

    boolean isTelly() {
        if (MoveUtils.isMoving()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) return mc.thePlayer.onGround || Player.airTicks < (speedTelly.isToggled() ? 0 : airTicks.getRandomizedIntValue());
            return !mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1f, mc.thePlayer.posZ));
        }
        return false;
    }

    Rot getDeltaSpeed(Rot delta) {
        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (isTelly()) {
                    return delta.limit(180, 180);
                } else {
                    return delta.limit(yawSpeed.getRandomizedIntValue(), pitchSpeed.getRandomizedIntValue());
                }
            }

            case "GodBridge" -> {
                if (isClutch()) {
                    return delta.limit(yawClutchSpeed.getRandomizedIntValue(), pitchClutchSpeed.getRandomizedIntValue());
                } else {
                    return delta.limit(yawSpeed.getRandomizedIntValue(), pitchSpeed.getRandomizedIntValue());
                }
            }

            case "Normal" -> {
                return delta.limit(yawSpeed.getRandomizedIntValue(), pitchSpeed.getRandomizedIntValue());
            }
        }

        return delta;
    }

    private float getPitch(float yaw, boolean handleMouse) {
        List<RotationData> dataList = new ArrayList<>();

        float minPitch = (float) pitchCorrectionSearchRange.getMinValue();
        float maxPitch = (float) pitchCorrectionSearchRange.getMaxValue();
        float step = pitchCorrectionMinStep.getValue();

        for (float pitch = minPitch; pitch <= maxPitch; pitch += step) {
            Rot rot = new Rot(yaw, pitch);

            MovingObjectPosition hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotationData data = new RotationData(rot, hit);

                if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    hit.hitVec.yCoord <= mc.thePlayer.posY &&
                    hit.getBlockPos().equals(targetBlock) &&
                    hit.sideHit != EnumFacing.DOWN
                ) dataList.add(data);
            }
        }

        if (dataList.isEmpty()) return lastRotation.getPitch();

        dataList.sort(Comparator.comparingDouble(data -> {
            float pitchDiff = Math.abs(mc.thePlayer.rotationPitch) - data.rotation().getPitch();

            return pitchDiff;
        }));

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

            MovingObjectPosition hit = RayCastUtils.rayCast(4.5, 4.5f, rot);

            if (hit != null) {
                RotationData data = new RotationData(rot, hit);

                if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                    hit.sideHit != EnumFacing.DOWN &&
                    hit.hitVec.yCoord <= mc.thePlayer.posY &&
                    hit.getBlockPos().equals(targetBlock)
                ) validRotations.add(data);
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

    public int getBlockCount() {
        int blockCount = 0;

        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;

            final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            final Block block = ((ItemBlock) is.getItem()).getBlock();

            if (!(is.getItem() instanceof ItemBlock && ItemUtils.blackListedBlock(block))) {
                continue;
            }

            blockCount += is.stackSize;
        }

        return blockCount;
    }

    public int findBlock() {
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];

            if (item == null || !(item.getItem() instanceof ItemBlock itemBlock) || item.stackSize == 0) continue;

            Block block = itemBlock.getBlock();

            if (ItemUtils.blackListedBlock(block)) continue;

            bestSlot = i;
        }

        return bestSlot;
    }
}
