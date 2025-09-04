package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.managers.PlayerManager;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.input.Mouse;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Scaffold", category = Category.PLAYER)
public class Scaffold extends Module {

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, 0,180,90,1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, 0,180,90,1);

    Mode rotMode = new Mode("Rotation Mode", this)
            .addModes("TellyBridge", "GodBridge", "Normal")
            .setMode("GodBridge");

    BooleanSupplier tellyVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge");
    BooleanSupplier godVisible = () -> rotMode.getMode().equalsIgnoreCase("GodBridge");

    BooleanSupplier tellyGodVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge") || rotMode.getMode().equalsIgnoreCase("GodBridge");
    BooleanSupplier tellyGodNormalVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge") || rotMode.getMode().equalsIgnoreCase("GodBridge") || rotMode.getMode().equalsIgnoreCase("Normal");

    DoubleSlider yawClutchSpeed = new DoubleSlider("Yaw Clutch Speed", this, godVisible, 0,180,90,1);
    DoubleSlider pitchClutchSpeed = new DoubleSlider("Pitch Clutch Speed", this, godVisible, 0,180,90,1);

    DoubleSlider yawForwardTellySpeed = new DoubleSlider("Yaw Forward Telly Speed", this, tellyVisible, 0,180,90,1);
    DoubleSlider pitchForwardTellySpeed = new DoubleSlider("Pitch Forward Telly Speed", this, tellyVisible, 0,180,90,1);

    DoubleSlider pitchCorrectionSearch = new DoubleSlider("Pitch Correction Search", this, tellyGodNormalVisible, 0,90,90,0.1f);

    FloatSetting stepPitchCorrection = new FloatSetting("Step Pitch Correction", this, tellyGodNormalVisible, 0.3f, 10, 0.8f, 0.01f);

    IntegerSetting sortingDistanceStrength = new IntegerSetting("Sorting Distance Strength", this, tellyGodNormalVisible, 0,100,2);

    Mode pitchSelection = new Mode("Pitch Selection", this)
            .addModes("Highest", "Nearest", "Lowest", "Mid")
            .setMode("Nearest")
            ;

    CheckBox noSwing = new CheckBox("No Swing", this);
    CheckBox serverSwing = new CheckBox("Server Swing", this, noSwing::isToggled, true);

    final CheckBox speedTelly = new CheckBox("Speed Telly", this, tellyVisible, true);
    DoubleSlider tellyTicks = new DoubleSlider("Telly Ticks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 0,12,5,1);

    final CheckBox ninjaBridge = new CheckBox("Ninja Bridge", this, godVisible, true);
    final FloatSetting edgeOffset = new FloatSetting("Edge Offset", this, () -> godVisible.getAsBoolean() && ninjaBridge.isToggled(), 0f,0.1f,0.05f, 0.01f);

    final CheckBox sneakIfRotate = new CheckBox("Sneak If Rotate", this, godVisible, true);
    final CheckBox sneakIfNoBlocks = new CheckBox("Sneak If Zero Blocks", this, godVisible, true);

    final CheckBox sameY = new CheckBox("SameY", this, true);

    final CheckBox bypassServerPitch = new CheckBox("Bypass Server Pitch", this, godVisible, true);
    FloatSetting serverPitch = new FloatSetting("Server Pitch", this, () -> godVisible.getAsBoolean() && bypassServerPitch.isToggled(), 70, 85, 77,0.1f);

    final CheckBox render = new CheckBox("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this);

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

    double lastDelta = 0;
    float lastPitch = 80;

    int blocksLeft;

    Glow shadows;

    @Override
    public void onDisable() {
        resetValues();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);

        if (event instanceof TickEvent) {
            rotate();
            legitPlace();
        }

        if (event instanceof Render3DEvent && mouse.getBlockPos() != null && render.isToggled()) {
            Color fadeColor = color.isFade() ? ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed()) : color.getColor();

            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.module.get("Scaffold")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(mouse.getBlockPos(), fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), 1f));
            RenderUtils.drawBlockESP(mouse.getBlockPos(), fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));

            if (rotMode.getMode().equalsIgnoreCase("GodBridge")) {
                if (sneakIfRotate.isToggled() && lastDelta > 5) e.setSneak(true);
                if (sneakIfNoBlocks.isToggled() && findBlock() == -1) e.setSneak(true);

                if (ninjaBridge.isToggled()) {
                    BlockPos pos = getDirectionalBlockPos(edgeOffset.getValue());
                    if (mc.theWorld.isAirBlock(pos)) e.setSneak(true);
                }
            }
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                mc.thePlayer.inventory.currentItem = slot;
            }
        }

        if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof UpdateBodyRotationEvent e) e.setYaw(Rot.getServerRotation().getYaw());
        if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());

        if (event instanceof MotionEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());

            if (rotMode.getMode().equalsIgnoreCase("GodBridge")) {
                if (bypassServerPitch.isToggled() && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !getSafeValue() && Rot.getServerRotation().getPitch() > 70 && Rot.getServerRotation().getPitch() < 85) {
                    e.setPitch(serverPitch.getValue());
                } else {
                    e.setPitch(Rot.getServerRotation().getPitch());
                }
            } else {
                if (speedTelly.isToggled()) mc.thePlayer.jumpTicks = 0;
                e.setPitch(Rot.getServerRotation().getPitch());
            }
        }

        if (event instanceof LookEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof ChangeHeadRotationEvent e) {
            e.setYaw(Rot.getServerRotation().getYaw());
            e.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof ClickEvent e) {
            if (e.getButton() == ClickEvent.Button.RIGHT) e.cancel();
        }
    }

    boolean getSafeValue() {
        return mc.thePlayer.hurtResistantTime > 0 || PlayerManager.airTicks > 12;
    }

    private void resetValues() {
        blocksLeft = 0;
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
    }

    private BlockPos getDirectionalBlockPos(float edgeOffset) {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY - 0.7;
        double z = mc.thePlayer.posZ;

        boolean movingX = Math.abs(mc.thePlayer.motionX) > 0.1;
        boolean movingZ = Math.abs(mc.thePlayer.motionZ) > 0.1;

        if (movingX || movingZ) {
            if (Math.abs(mc.thePlayer.motionX) > Math.abs(mc.thePlayer.motionZ)) {
                x += (mc.thePlayer.motionX > 0) ? -edgeOffset : edgeOffset;
            } else {
                z += (mc.thePlayer.motionZ > 0) ? -edgeOffset : edgeOffset;
            }
        }

        return new BlockPos(x, y, z);
    }

    void legitPlace() {
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(6, 4.5f, Rot.getServerRotation());

        if (mouse.sideHit == mouseOver.sideHit
                && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && getSameYValue(mouse) && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), mouseOver.getBlockPos(), mouse.sideHit, mouse.hitVec)) {
                if (noSwing.isToggled()) {
                    if (serverSwing.isToggled()) mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                } else {
                    mc.thePlayer.swingItem();
                }
                blocksLeft++;
            }
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
        float yawStep = 45f;

        float yaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), yawStep);

        if (yaw / yawStep % 2 == 0) {
            yaw += yawStep;
        }

        switch (rotMode.getMode()) {
            case "TellyBridge" -> {
                if (getTellyValue()) {
                    rotation = new Rot(MoveUtils.getDir(), 90);
                } else {
                    rotation = getBestRotation();
                }
            }

            case "GodBridge" -> {
                if (getSafeValue()) {
                    rotation = getBestRotation();
                } else {
                    rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(MathHelper.wrapDegree(yaw)));
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
                Math.clamp(Rot.getServerRotation().getPitch() + delta.getPitch(), -90, 90)
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

    private float getPitch(float yaw) {
        Map<Float, MovingObjectPosition> positionHashMap = new HashMap<>();

        float maxPitch = (float) pitchCorrectionSearch.getMaxValue();
        float minPitch = (float) pitchCorrectionSearch.getMinValue();

        float step = stepPitchCorrection.getValue();
        for (float i = minPitch; i < maxPitch; i += step) {
            MovingObjectPosition mouses = RayCastUtils.rayCast(4.5, 4.5f, new Rot(yaw, i));
            if (mouses == null || mouses.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || positionHashMap.containsValue(mouses)
                    || mouses.sideHit == EnumFacing.DOWN) continue;
            positionHashMap.put(i, mouses);
        }

        if (positionHashMap.isEmpty()) {
            return switch (pitchSelection.getMode()) {
                case "Nearest" -> lastPitch;
                case "Lowest" -> 80;
                case "Highest" -> 76;
                case "Mid" -> 78;
                default -> Rot.getServerRotation().getPitch();
            };
        }

        List<Float> pitches = new ArrayList<>(positionHashMap.keySet());

        pitches.sort(Comparator.comparingDouble(pitch -> Math.abs(Rot.getServerRotation().getPitch()) - pitch));
        if (!getSafeValue() && rotMode.getMode().equalsIgnoreCase("GodBridge")) {
            mouse = positionHashMap.get(pitches.getFirst());
        }

        if (pitchSelection.getMode().equalsIgnoreCase("Nearest")) {
            lastPitch = pitches.getFirst();
        }
        return pitches.getFirst();
    }

    private Rot getBestRotation() {
        float step = 2f;
        List<RotationData> validRotations = new ArrayList<>();

        for (float yaw = -180; yaw < 180; yaw += step) {
            float pitch = getPitch(yaw);
            MovingObjectPosition hit = RayCastUtils.rayCast(4.5, 4.5f, new Rot(yaw, pitch));

            if (hit == null
                    || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || hit.sideHit == EnumFacing.DOWN) {
                continue;
            }

            validRotations.add(new RotationData(
                    new Rot(MathHelper.wrapDegree(yaw), pitch),
                    hit.hitVec,
                    hit
            ));
        }

        if (validRotations.isEmpty()) {
            return lastRotation;
        }

        validRotations.sort(Comparator.comparingDouble(data -> {
            double yawDiff = MathHelper.wrapDegree(Rot.getServerRotation().getYaw() - data.rotation.getYaw());
            double pitchDiff = Rot.getServerRotation().getPitch() - data.rotation.getPitch();

            return Math.hypot(yawDiff, pitchDiff) + mc.thePlayer.getPositionVector().distanceTo(data.hitPos) * (sortingDistanceStrength.getValue() * 10);
        }));

        closest = validRotations.getFirst();

        lastRotation = closest.rotation;
        mouse = closest.mouse;
        return lastRotation;
    }

    RotationData closest;

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

    private record RotationData(Rot rotation, Vec3 hitPos, MovingObjectPosition mouse) {}

}
