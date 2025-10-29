package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.managers.PlayerManager;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.scaffold.EnumFacingOffset;
import fuguriprivatecoding.autotoolrecode.module.impl.player.scaffold.RotationData;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.inventory.PlayerUtil;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtil;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.joml.Vector2f;
import org.lwjgl.input.Mouse;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Scaffold", category = Category.PLAYER)
public class Scaffold extends Module {

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0,180,90,1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0,180,90,1);

    Mode rotMode = new Mode("RotationMode", this)
            .addModes("TellyBridge", "GodBridge", "Normal")
            .setMode("GodBridge");

    BooleanSupplier tellyVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge");
    BooleanSupplier godVisible = () -> rotMode.getMode().equalsIgnoreCase("GodBridge");
    BooleanSupplier tellyGodVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge") || rotMode.getMode().equalsIgnoreCase("GodBridge");

    BooleanSupplier tellyGodNormalVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge") || rotMode.getMode().equalsIgnoreCase("GodBridge") || rotMode.getMode().equalsIgnoreCase("Normal");

    DoubleSlider yawClutchSpeed = new DoubleSlider("YawClutchSpeed", this, godVisible, 0,180,90,1);
    DoubleSlider pitchClutchSpeed = new DoubleSlider("PitchClutchSpeed", this, godVisible, 0,180,90,1);

    DoubleSlider yawForwardTellySpeed = new DoubleSlider("YawForwardTellySpeed", this, tellyVisible, 0,180,90,1);
    DoubleSlider pitchForwardTellySpeed = new DoubleSlider("PitchForwardTellySpeed", this, tellyVisible, 0,180,90,1);

    DoubleSlider pitchCorrectionSearch = new DoubleSlider("PitchCorrectionSearch", this, tellyGodNormalVisible, 0,90,90,0.1f);

    FloatSetting stepPitchCorrection = new FloatSetting("StepPitchCorrection", this, tellyGodNormalVisible, 0.3f, 10, 0.8f, 0.01f);

    IntegerSetting sortingDistanceStrength = new IntegerSetting("SortingDistanceStrength", this, tellyGodNormalVisible, 0,100,2);

    MultiMode removeSwing = new MultiMode("RemoveSwing", this)
        .addModes("On Client", "On Server")
        ;

    Mode sprintMode = new Mode("SprintMode", this)
        .addModes("None", "Legit", "AllDirection")
        .setMode("Legit")
        ;

    final CheckBox rotateWithMovement = new CheckBox("RotateWithMovement", this, tellyGodVisible);

    final CheckBox speedTelly = new CheckBox("SpeedTelly", this, tellyVisible, true);
    DoubleSlider tellyTicks = new DoubleSlider("TellyTicks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 0,12,5,1);

    final CheckBox sameY = new CheckBox("SameY", this, true);

    MultiMode sneakIf = new MultiMode("SneakIf", this, godVisible)
        .addModes("Clutching", "Rotate", "Zero Blocks", "Ninja Bridge");

    final FloatSetting edgeOffset = new FloatSetting("EdgeOffset", this, () -> godVisible.getAsBoolean() && sneakIf.get("Ninja Bridge"), 0f,0.1f,0.05f, 0.01f);

    final CheckBox render = new CheckBox("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    private final List<Block> blacklistedBlocks = Arrays.asList(
            Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
            Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
            Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
            Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
            Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
            Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
            Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs,
            Blocks.stone_stairs, Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
            Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
            Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
            Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
            Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate
    );

    private MovingObjectPosition mouse;

    Rot rotation, lastRotation;

    RotationData closest;

    private Vec3 targetBlock;

    private BlockPos blockFace;
    private EnumFacingOffset enumFacing;

    double lastDelta = 0;
    float lastPitch = 80;

    int blocksLeft;

    @Override
    public void onDisable() {
        resetValues();
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer.isUsingItem()) {
            mc.thePlayer.setItemInUse(mc.thePlayer.inventory.getCurrentItem(), 0);
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
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

        if (event instanceof Render3DEvent && blockFace != null && render.isToggled()) {
            RenderUtils.start3D();

            if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawBlockESP(blockFace, glowColor.getFadedFloatColor()));
            RenderUtils.drawBlockESP(blockFace, color.getFadedFloatColor());

            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));

            switch (rotMode.getMode()) {
                case "GodBridge" -> {
                    if (sneakIf.get("Rotate") && lastDelta > 5) {
                        if (getSafeValue() && !sneakIf.get("Clutching")) {
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
                    if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.thePlayer.isSneaking())
                        mc.thePlayer.setSprinting(true);
                }
                case "None" -> mc.thePlayer.setSprinting(false);
            }
        }

        if (event instanceof MotionEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());

            switch (rotMode.getMode()) {
                case "TellyBridge" -> {
                    if (speedTelly.isToggled()) mc.thePlayer.jumpTicks = 0;
                    e.setPitch(Rot.getServerRotation().getPitch());
                }

                default -> e.setPitch(Rot.getServerRotation().getPitch());
            }
        }

        if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof UpdateBodyRotationEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof ClickEvent e && e.getButton() == ClickEvent.Button.RIGHT) e.cancel();

        if (event instanceof LookEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof ChangeHeadRotationEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }
    }

    boolean getSafeValue() {
        return mc.thePlayer.hurtResistantTime > 0 || PlayerManager.airTicks > 12;
    }

    private void resetValues() {
        blocksLeft = 0;
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;

        mouse = null;
    }

    void legitPlace() {
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(6, 4.5f, Rot.getServerRotation());

        if (mouse.sideHit == mouseOver.sideHit &&
                mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && getSameYValue(mouse)
                && mouse.getBlockPos().getX() == mouseOver.getBlockPos().getX() && mouse.getBlockPos().getZ() == mouseOver.getBlockPos().getZ()
            && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {

            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
                if (!removeSwing.get("On Client")) {
                    mc.thePlayer.swingItemNoPacket();
                }

                if (!removeSwing.get("On Server")) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }

            blocksLeft++;
        }
    }

    boolean getSameYValue(MovingObjectPosition mouse) {
        if (sameY.isToggled()) {
            if (Mouse.isButtonDown(1)) return mouse.sideHit != EnumFacing.DOWN;
            return mouse.sideHit != EnumFacing.DOWN && mouse.sideHit != EnumFacing.UP;
        }
        return mouse.sideHit != EnumFacing.DOWN;
    }

    void rotate() {
        float yaw = rotateWithMovement.isToggled() ? MoveUtils.getDir() : mc.thePlayer.rotationYaw;
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(yaw + 180), 45);

        boolean isOnRightSide = Math.floor(mc.thePlayer.posX + Math.cos(Math.toRadians(roundedYaw)) * 0.6) != Math.floor(mc.thePlayer.posX) ||
            Math.floor(mc.thePlayer.posZ + Math.sin(Math.toRadians(roundedYaw)) * 0.3) != Math.floor(mc.thePlayer.posZ);

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (getTellyValue()) {
                    rotation = new Rot(yaw, lastRotation.getPitch());
                } else {
                    rotation = getBestRotation();
                }
            }

            case "GodBridge" -> {
                if (getSafeValue()) {
                    rotation = getBestRotation();
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

            case "Normal" -> rotation = getBestRotation();
        }

        Delta delta = RotUtils.getDelta(Rot.getServerRotation(), rotation);

        delta = getDeltaSpeed(delta);

        delta = RotUtils.fixDelta(delta);

        lastDelta = delta.hypot();

        Rot rot = new Rot(
                Rot.getServerRotation().getYaw() + delta.getYaw(),
                Rot.getServerRotation().getPitch() + delta.getPitch()
        );

        Rot.setServerRotation(rot);
    }

    boolean getTellyValue() {
        if (MoveUtils.isMoving()) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) return mc.thePlayer.onGround || mc.thePlayer.jumpTicks > tellyTicks.getRandomizedIntValue();
            return !mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1f, mc.thePlayer.posZ));
        }
        return false;
    }

    Delta getDeltaSpeed(Delta delta) {
        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (getTellyValue()) {
                    return delta.limit(yawForwardTellySpeed.getRandomizedIntValue(), pitchForwardTellySpeed.getRandomizedIntValue());
                } else {
                    return delta.limit(yawSpeed.getRandomizedIntValue(), pitchSpeed.getRandomizedIntValue());
                }
            }

            case "GodBridge" -> {
                if (getSafeValue()) {
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
        Map<Float, MovingObjectPosition> positionHashMap = new HashMap<>();

        float minPitch = (float) pitchCorrectionSearch.getMinValue();
        float maxPitch = (float) pitchCorrectionSearch.getMaxValue();

        float step = stepPitchCorrection.getValue();
        for (float i = minPitch; i < maxPitch; i += step) {
            MovingObjectPosition mouses = RayCastUtils.rayCast(4.5, 4.5f, new Rot(yaw, i));
            if (mouses == null || mouses.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || positionHashMap.containsValue(mouses)
                    || mouses.sideHit == EnumFacing.DOWN
            ) continue;
            positionHashMap.put(i, mouses);
        }

        if (positionHashMap.isEmpty()) {
            return lastPitch;
        }

        List<Float> pitches = new ArrayList<>(positionHashMap.keySet());

        pitches.sort(Comparator.comparingDouble(pitch -> Math.abs(Rot.getServerRotation().getPitch()) - pitch));

        if (handleMouse) {
            mouse = positionHashMap.get(pitches.getFirst());
            lastPitch = pitches.getFirst();
        }

        return pitches.getFirst();
    }

    private Rot getBestRotation() {
        float step = 5;
        List<RotationData> validRotations = new ArrayList<>();

        for (float i = 0; i < 360; i += step) {
            float yaw = MathHelper.wrapDegree(i);
            float pitch = getPitch(yaw, false);
            MovingObjectPosition hit = RayCastUtils.rayCast(4.5, 4.5f, new Rot(yaw, pitch));

            if (hit == null
                || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || hit.sideHit == EnumFacing.DOWN
                || hit.hitVec.yCoord >= mc.thePlayer.posY
            ) {
                continue;
            }

            validRotations.add(new RotationData(
                new Rot(yaw, pitch),
                hit.hitVec,
                hit
            ));
        }

        if (validRotations.isEmpty()) {
            return lastRotation;
        }

        validRotations.sort(Comparator.comparingDouble(data -> {
            double yawDiff = MathHelper.wrapDegree(Rot.getServerRotation().getYaw() - data.rotation().getYaw());
            double pitchDiff = Rot.getServerRotation().getPitch() - data.rotation().getPitch();

            return Math.hypot(yawDiff, pitchDiff) + DistanceUtils.getDistance(data.hitPos()) * (sortingDistanceStrength.getValue() * 10);
        }));

        closest = validRotations.getFirst();

        lastRotation = closest.rotation();
        mouse = closest.mouse();
        return lastRotation;
    }

    public int getBlockCount() {
        int blockCount = 0;

        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;

            final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (!(is.getItem() instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) is.getItem()).getBlock()))) {
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

            if (blacklistedBlocks.contains(block)) continue;

            bestSlot = i;
        }

        return bestSlot;
    }
}
