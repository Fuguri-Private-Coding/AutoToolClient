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
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.block.*;
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

    CheckBox noSwing = new CheckBox("No Swing", this);
    CheckBox serverSwing = new CheckBox("Server Swing", this, noSwing::isToggled, true);

    final CheckBox speedTelly = new CheckBox("Speed Telly", this, tellyVisible, true);
    IntegerSetting minTellyTicks = new IntegerSetting("Min Telly Ticks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 2, 12, 5) {
        @Override
        public int getValue() {
            if (maxTellyTicks.value < value) { value = maxTellyTicks.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxTellyTicks = new IntegerSetting("Max Telly Ticks", this, () -> tellyVisible.getAsBoolean() && !speedTelly.isToggled(), 2, 12, 5) {
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
    final CheckBox fadeColor = new CheckBox("Fade Color", this, render::isToggled);
    final ColorSetting color1 = new ColorSetting("Color1", this, render::isToggled, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, () -> render.isToggled() && fadeColor.isToggled(), 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("Fade Speed", this, () -> render.isToggled() && fadeColor.isToggled(),0.1f, 20, 1, 0.1f);

    final StopWatch stopWatch;
    private MovingObjectPosition mouse;

    double lastDelta = 0;
    float lastPitch = 80;
    float[] lastRotation;
    int jumpTicks;
    Glow shadows;

    @Override
    public void onDisable() {
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
    }

    public Scaffold() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);

        if (event instanceof TickEvent) {
            if (!mc.thePlayer.onGround) {
                jumpTicks++;
            } else {
                jumpTicks = 0;
            }
            rotate();
            legitPlace();
        }

        if (event instanceof Render3DEvent && mouse.getBlockPos() != null && render.isToggled()) {
            Color fadeColor = this.fadeColor.isToggled() ? ColorUtils.fadeColor(color1.getColor(), color2.getColor(), fadeSpeed.getValue()) : color1.getColor();

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
                    BlockPos pos = getBlockPos(edgeOffset.getValue());
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
                if (bypassServerPitch.isToggled() && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
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
        if (jumpTicks > 11) {
            return true;
        }

        if (jumpTicks > 6) {
            return mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5f, mc.thePlayer.posZ)) &&
                    mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.2f, mc.thePlayer.posZ)) &&
                    mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0f, mc.thePlayer.posZ)) &&
                    mc.thePlayer.jumpTicks == 0;
        }

        if (mc.thePlayer.hurtResistantTime > 0) {
            return mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.2f, mc.thePlayer.posZ));
        }

        return false;
    }

    private BlockPos getBlockPos(float edgeOffset) {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY - 0.5;
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
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, 4.5, Rot.getServerRotation());

        if (mouse.sideHit == mouseOver.sideHit
                && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && getSameYValue(mouse) && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), mouse.getBlockPos(), mouse.sideHit, mouse.hitVec)) {
                if (noSwing.isToggled()) {
                    if (serverSwing.isToggled()) mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                } else {
                    mc.thePlayer.swingItem();
                }
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

        Rot rotation;

        if (rotMode.getMode().equalsIgnoreCase("TellyBridge")) {
            if (getTellyValue()) {
                rotation = new Rot(MathHelper.wrapDegree(yaw + 180), 80);
            } else {
                rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(MathHelper.wrapDegree(yaw)));
            }
        } else {
            if (getSafeValue()) {
                float[] rot = getBestRotation();
                rotation = new Rot(rot[0], rot[1]);
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
            delta = delta.limit(
                    RandomUtils.nextInt(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                    RandomUtils.nextInt(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
            );
        }

        delta = RotUtils.fixDelta(delta);

        lastDelta = delta.hypot();

        Rot.setServerRotation(new Rot(
                Rot.getServerRotation().getYaw() + delta.getYaw(),
                Math.clamp(Rot.getServerRotation().getPitch() + delta.getPitch(), -90, 90)
        ));
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

        float maxPitch = rotMode.getMode().equalsIgnoreCase("TellyBridge") ? 85 : 81;

        float step = 0.8f;
        for (float i = 45; i < maxPitch; i += step) {
            MovingObjectPosition mouses = RayCastUtils.rayCast(3, 4.5, new Rot(yaw, i));
            if (mouses == null || mouses.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || positionHashMap.containsValue(mouses)
                    || mouses.sideHit == EnumFacing.DOWN) continue;
            positionHashMap.put(i, mouses);
        }

        if (positionHashMap.isEmpty()) {
            return lastPitch;
        }

        List<Float> pitches = new ArrayList<>(positionHashMap.keySet());

        pitches.sort(Comparator.comparingDouble(pitch -> Math.abs(Rot.getServerRotation().getPitch()) - pitch));
        if (!getSafeValue() && rotMode.getMode().equalsIgnoreCase("GodBridge")) {
            mouse = positionHashMap.get(pitches.getFirst());
        }

        if (rotMode.getMode().equalsIgnoreCase("TellyBridge")) mouse = positionHashMap.get(pitches.getFirst());
        lastPitch = pitches.getFirst();
        return pitches.getFirst();
    }

    private float[] getBestRotation() {
        float step = 2f;
        List<RotationData> validRotations = new ArrayList<>();

        for (float yaw = -180; yaw < 180; yaw += step) {
            float pitch = getPitch(yaw);
            MovingObjectPosition hit = RayCastUtils.rayCast(3, 4.5, new Rot(yaw, pitch));

            if (hit == null
                    || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || hit.sideHit == EnumFacing.DOWN
                    || hit.sideHit == EnumFacing.UP) {
                continue;
            }

            validRotations.add(new RotationData(
                    MathHelper.wrapDegree(yaw),
                    pitch,
                    hit.hitVec,
                    hit
            ));
        }

        if (validRotations.isEmpty()) {
            return lastRotation;
        }

        RotationData closest = findClosestRotation(validRotations);
        lastRotation = new float[]{closest.yaw, closest.pitch};
        mouse = closest.mouse;
        return lastRotation;
    }

    private RotationData findClosestRotation(List<RotationData> rotations) {
        RotationData closest = null;
        double minDistance = Double.MAX_VALUE;

        Vec3 playerPos = mc.thePlayer.getPositionVector(); // Здесь нужно получить позицию игрока

        for (RotationData data : rotations) {
            double dist = playerPos.distanceTo(data.hitPos);
            if (dist < minDistance) {
                minDistance = dist;
                closest = data;
            }
        }

        return closest;
    }

    private static class RotationData {
        public final float yaw;
        public final float pitch;
        public final Vec3 hitPos;
        public final MovingObjectPosition mouse;

        public RotationData(float yaw, float pitch, Vec3 hitPos, MovingObjectPosition mouse) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.hitPos = hitPos;
            this.mouse = mouse;
        }
    }

    public int findBlock() {
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemBlock block)
                    || block.getBlock() instanceof BlockSand
                    || block.getBlock() instanceof BlockGravel
                    || block.getBlock() instanceof BlockSoulSand
                    || block.getBlock() instanceof BlockTNT
                    || block.getBlock() instanceof BlockWeb
                    || block.getBlock() instanceof BlockFence
                    || block.getBlock() instanceof BlockFenceGate
                    || block.getBlock() instanceof BlockWall
                    || block.getBlock() instanceof BlockPane
                    || block.getBlock() instanceof BlockStairs
                    || block.getBlock() instanceof BlockSlab
                    || block.getBlock() instanceof BlockCarpet
                    || block.getBlock() instanceof BlockSnow
                    || block.getBlock() instanceof BlockLilyPad
                    || block.getBlock() instanceof BlockFire
                    || block.getBlock() instanceof BlockRedstoneWire
                    || block.getBlock() instanceof BlockTorch
                    || block.getBlock() instanceof BlockLadder
                    || block.getBlock() instanceof BlockFurnace
                    || block.getBlock() instanceof BlockCactus
                    || block.getBlock() instanceof BlockAnvil
                    || block.getBlock() instanceof BlockDoor
                    || block.getBlock() instanceof BlockEndPortal
                    || block.getBlock() instanceof BlockEndPortalFrame
                    || block.getBlock() instanceof BlockEnchantmentTable
                    || block.getBlock() instanceof BlockChest
                    || block.getBlock() instanceof BlockEnderChest
                    || block.getBlock() instanceof BlockWorkbench
                    || block.getBlock() instanceof BlockPressurePlate
                    || block.getBlock() instanceof BlockTrapDoor
                    || block.getBlock() instanceof BlockDropper
                    || block.getBlock() instanceof BlockNote
                    || item.stackSize == 0
            ) continue;
            bestSlot = i;
        }

        return bestSlot;
    }
}
