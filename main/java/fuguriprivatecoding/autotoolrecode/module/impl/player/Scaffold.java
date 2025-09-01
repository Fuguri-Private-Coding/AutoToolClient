package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Glow;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
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
import net.minecraft.block.state.IBlockState;
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

    IntegerSetting minYawSpeed = new IntegerSetting("Min Yaw Speed", this, 1, 180, 30) {
        @Override
        public int getValue() {
            if (maxYawSpeed.value < value) { value = maxYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxYawSpeed = new IntegerSetting("Max Yaw Speed", this, 1, 180, 30) {
        @Override
        public int getValue() {
            if (minYawSpeed.value > value) { value = minYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting minPitchSpeed = new IntegerSetting("Min Pitch Speed", this, 1, 180, 15) {
        @Override
        public int getValue() {
            if (maxPitchSpeed.value < value) { value = maxPitchSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxPitchSpeed = new IntegerSetting("Max Pitch Speed", this, 1, 180, 15) {
        @Override
        public int getValue() {
            if (minPitchSpeed.value > value) { value = minPitchSpeed.value; }
            return super.getValue();
        }
    };

    Mode rotMode = new Mode("Rotation Mode", this)
            .addModes("TellyBridge", "GodBridge")
            .setMode("GodBridge")
            ;

    BooleanSupplier tellyVisible = () -> rotMode.getMode().equalsIgnoreCase("TellyBridge");
    BooleanSupplier godVisible = () -> rotMode.getMode().equalsIgnoreCase("GodBridge");

    IntegerSetting minClutchYawSpeed = new IntegerSetting("Min Clutch Yaw Speed", this, godVisible, 1, 180, 30) {
        @Override
        public int getValue() {
            if (maxClutchYawSpeed.value < value) { value = maxClutchYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxClutchYawSpeed = new IntegerSetting("Max Clutch Yaw Speed", this, godVisible, 1, 180, 30) {
        @Override
        public int getValue() {
            if (minClutchYawSpeed.value > value) { value = minClutchYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting minClutchPitchSpeed = new IntegerSetting("Min Clutch Pitch Speed", this, godVisible, 1, 180, 15) {
        @Override
        public int getValue() {
            if (maxClutchPitchSpeed.value < value) { value = maxClutchPitchSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxClutchPitchSpeed = new IntegerSetting("Max Clutch Pitch Speed", this, godVisible, 1, 180, 15) {
        @Override
        public int getValue() {
            if (minClutchPitchSpeed.value > value) { value = minClutchPitchSpeed.value; }
            return super.getValue();
        }
    };

    FloatSetting minPitchCorrectionSearch = new FloatSetting("Min Pitch Correction Search", this, godVisible, 0, 90, 81, 0.1f) {
        @Override
        public float getValue() {
            if (maxPitchCorrectionSearch.value < value) { value = maxPitchCorrectionSearch.value; }
            return super.getValue();
        }
    };
    FloatSetting maxPitchCorrectionSearch = new FloatSetting("Max Pitch Correction Search", this, godVisible, 0, 90, 81, 0.1f) {
        @Override
        public float getValue() {
            if (minPitchCorrectionSearch.value > value) { value = minPitchCorrectionSearch.value; }
            return super.getValue();
        }
    };

    FloatSetting stepPitchCorrection = new FloatSetting("Step Pitch Correction", this, godVisible, 0.3f, 10, 0.8f, 0.01f);

    Mode pitchSelection = new Mode("Pitch Selection", this)
            .addModes("Highest", "Nearest", "Lowest", "Mid")
            .setMode("Nearest")
            ;

    CheckBox noSwing = new CheckBox("No Swing", this);
    CheckBox serverSwing = new CheckBox("Server Swing", this, noSwing::isToggled, true);

    final CheckBox speedTelly = new CheckBox("Speed Telly", this, tellyVisible, true);
    IntegerSetting minTellyTicks = new IntegerSetting("Min Telly Ticks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 4, 12, 5) {
        @Override
        public int getValue() {
            if (maxTellyTicks.value < value) { value = maxTellyTicks.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxTellyTicks = new IntegerSetting("Max Telly Ticks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 4, 12, 5) {
        @Override
        public int getValue() {
            if (minTellyTicks.value > value) { value = minTellyTicks.value; }
            return super.getValue();
        }
    };

    final CheckBox ninjaBridge = new CheckBox("Ninja Bridge", this, godVisible, true);
    final FloatSetting edgeOffset = new FloatSetting("Edge Offset", this, () -> godVisible.getAsBoolean() && ninjaBridge.isToggled(), 0f,0.1f,0.05f, 0.01f);

    final CheckBox sneakIfRotate = new CheckBox("Sneak If Rotate", this, godVisible, true);
    final CheckBox sneakIfNoBlocks = new CheckBox("Sneak If Zero Blocks", this, godVisible, true);

    final CheckBox sameY = new CheckBox("SameY", this, true);

    final CheckBox bypassServerPitch = new CheckBox("Bypass Server Pitch", this, godVisible, true);
    FloatSetting serverPitch = new FloatSetting("Server Pitch", this, () -> godVisible.getAsBoolean() && bypassServerPitch.isToggled(), 70, 85, 77,0.1f);

    final CheckBox render = new CheckBox("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this);

    private final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
            Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
            Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
            Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
            Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
            Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
            Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs, Blocks.stone_stairs, Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
            Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
            Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
            Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
            Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate);

    private MovingObjectPosition mouse;

    Rot rotation;

    double lastDelta = 0;
    float lastPitch = 80;
    Rot lastRotation;

    int jumpTicks, blocksLeft;

    Glow shadows;

    @Override
    public void onDisable() {
        resetValues();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);

        if (event instanceof TickEvent) {
            updateValues();
            rotate();
            legitPlace();
        }

        if (event instanceof Render3DEvent && findBlocks() != null && render.isToggled()) {
            Color fadeColor = color.isFade() ? ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed()) : color.getColor();

            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.module.get("Scaffold")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(mouse.getBlockPos(), fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), 1f));
            RenderUtils.drawBlockESP(findBlocks(), fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
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
        return mc.thePlayer.hurtResistantTime > 0 || jumpTicks > 12;
    }

    private void updateValues() {
        if (!mc.thePlayer.onGround) {
            jumpTicks++;
        } else {
            jumpTicks = 0;
        }
    }

    private void resetValues() {
        jumpTicks = 0;
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
        float yawStep = rotMode.getMode().equalsIgnoreCase("TellyBridge") ? 0.1f : 45f;

        float yaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), yawStep);

        if (yaw / yawStep % 2 == 0) {
            yaw += yawStep;
        }

        if (rotMode.getMode().equalsIgnoreCase("TellyBridge")) {
            if (getTellyValue()) {
                rotation = new Rot(MathHelper.wrapDegree(yaw + 180), 80);
            } else {
                rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(MathHelper.wrapDegree(yaw)));
            }
        } else {
            if (getSafeValue()) {
                rotation = getBestRotation();
            } else {
                rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(MathHelper.wrapDegree(yaw)));
            }
        }

        Delta delta = RotUtils.getDelta(Rot.getServerRotation(), rotation);

        if (rotMode.getMode().equalsIgnoreCase("TellyBridge")) {
            if (getTellyValue()) {
                delta = delta.limit(
                        RandomUtils.nextInt(180, 180),
                        RandomUtils.nextInt(180, 180)
                );
            } else {
                delta = delta.limit(
                        RandomUtils.nextInt(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                        RandomUtils.nextInt(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
                );
            }
        } else {
            if (getSafeValue()) {
                delta = delta.limit(
                        RandomUtils.nextInt(minClutchYawSpeed.getValue(), maxClutchYawSpeed.getValue()),
                        RandomUtils.nextInt(minClutchPitchSpeed.getValue(), maxClutchPitchSpeed.getValue())
                );
            } else {
                delta = delta.limit(
                        RandomUtils.nextInt(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                        RandomUtils.nextInt(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
                );
            }
        }

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
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                return mc.thePlayer.onGround || mc.thePlayer.jumpTicks > RandomUtils.nextInt(minTellyTicks.getValue(), maxTellyTicks.getValue());
            }
            return !mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1f, mc.thePlayer.posZ));
        }
        return false;
    }

    private float getPitch(float yaw) {
        Map<Float, MovingObjectPosition> positionHashMap = new HashMap<>();

        float maxPitch = rotMode.getMode().equalsIgnoreCase("TellyBridge") ? 85 : maxPitchCorrectionSearch.getValue();
        float minPitch = rotMode.getMode().equalsIgnoreCase("TellyBridge") ? 45 : minPitchCorrectionSearch.getValue();

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

        if (rotMode.getMode().equalsIgnoreCase("TellyBridge")) mouse = positionHashMap.get(pitches.getFirst());
        if (pitchSelection.getMode().equalsIgnoreCase("Nearest")) {
            lastPitch = pitches.getFirst();
        }
        return pitches.getFirst();
    }

    public BlockPos findBlocks() {

        List<BlockPos> blockPosList = new ArrayList<>();

        for (int y = 4; y >= -4; --y) {
            for (int x = -4; x <= 4; ++x) {
                for (int z = -4; z <= 4; ++z) {
                    BlockPos pos = new BlockPos(
                            mc.thePlayer.posX + x,
                            mc.thePlayer.posY + y,
                            mc.thePlayer.posZ + z
                    );
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    if (state.getBlock() instanceof BlockAir) continue;

                    blockPosList.add(pos);
                }
            }
        }

        Vec3 playerPos = mc.thePlayer.getPositionVector();

//        blockPosList.sort();

        return blockPosList.getFirst();

    }

    private Rot getBestRotation() {
        float step = 2f;
        List<RotationData> validRotations = new ArrayList<>();

        for (float yaw = -180; yaw < 180; yaw += step) {
            float pitch = getPitch(yaw);
            MovingObjectPosition hit = RayCastUtils.rayCast(4.5, 4.5f, new Rot(yaw, pitch));

            if (hit == null
                    || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || hit.sideHit == EnumFacing.DOWN || hit.sideHit == EnumFacing.UP) {
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
            double pitchDiff = Math.abs(Rot.getServerRotation().getPitch() - data.rotation.getPitch());

            return yawDiff + pitchDiff + DistanceUtils.getDistance(data.hitPos);
        }));

        validRotations.sort(Comparator.comparingDouble(data -> {
            return DistanceUtils.getDistance(data.hitPos);
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
