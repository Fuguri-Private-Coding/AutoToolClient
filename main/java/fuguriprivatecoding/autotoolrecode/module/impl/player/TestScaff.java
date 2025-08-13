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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import java.awt.*;
import java.util.*;
import java.util.List;

@ModuleInfo(name = "TestScaff", category = Category.PLAYER)
public class TestScaff extends Module {

    IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this, 1, 180, 30) {
        @Override
        public int getValue() {
            if (maxYawSpeed.value < value) { value = maxYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this, 1, 180, 30) {
        @Override
        public int getValue() {
            if (minYawSpeed.value > value) { value = minYawSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this, 1, 180, 15) {
        @Override
        public int getValue() {
            if (maxPitchSpeed.value < value) { value = maxPitchSpeed.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this, 1, 180, 15) {
        @Override
        public int getValue() {
            if (minPitchSpeed.value > value) { value = minPitchSpeed.value; }
            return super.getValue();
        }
    };

    FloatSetting smooth = new FloatSetting("Smooth", this, 1, 10, 2f, 0.1f) {};

    IntegerSetting minCps = new IntegerSetting("MinCps", this, 1, 40, 7) {
        @Override
        public int getValue() {
            if (maxCps.value < value) { value = maxCps.value; }
            return super.getValue();
        }
    };
    IntegerSetting maxCps = new IntegerSetting("MaxCps", this, 0, 40, 11) {
        @Override
        public int getValue() {
            if (minCps.value > value) { value = minCps.value; }
            return super.getValue();
        }
    };

    final CheckBox bypassServerPitch = new CheckBox("BypassServerPitch", this, true);
    FloatSetting serverPitch = new FloatSetting("ServerPitch", this, bypassServerPitch::isToggled , 70, 85, 77,0.1f);

    final CheckBox render = new CheckBox("Render", this, true);
    final CheckBox fadeColor = new CheckBox("FadeColor", this, render::isToggled);
    final ColorSetting color1 = new ColorSetting("Color1", this, render::isToggled, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, () -> render.isToggled() && fadeColor.isToggled(), 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, () -> render.isToggled() && fadeColor.isToggled(),0.1f, 20, 1, 0.1f);

    final StopWatch stopWatch;

    private MovingObjectPosition mouse;

    BlockPos blockPos;

    int delay = 0;

    BlockPos renderPos = null;
    Glow shadows;

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
    }

    public TestScaff() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);

        if (event instanceof TickEvent) {

        }

        if (event instanceof TickEvent) {
            MovingObjectPosition renderRayCast = RayCastUtils.rayCast(4.5, 4.5, Rot.getServerRotation());
            BlockPos analyzingBlock = renderRayCast.getBlockPos();
            if (analyzingBlock != null && renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) renderPos = analyzingBlock;
            updateBlockPos();
            rotate();
            legitPlace();
        }

        if (event instanceof Render3DEvent && renderPos != null && render.isToggled()) {
            Color fadeColor;
            if (this.fadeColor.isToggled()) {
                fadeColor = ColorUtils.fadeColor(color1.getColor(), color2.getColor(), fadeSpeed.getValue());
            } else {
                fadeColor = color1.getColor();
            }

            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.module.get("Scaffold")) {
                BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderPos, fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), 1f));
            }
            RenderUtils.drawBlockESP(renderPos, fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent e) {
            MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
        }

        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                mc.thePlayer.inventory.currentItem = slot;
            }
        }

        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rot.getServerRotation().getYaw());
            if (bypassServerPitch.isToggled()) motionEvent.setPitch(serverPitch.getValue()); else motionEvent.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof LookEvent lookEvent) {
            lookEvent.setYaw(Rot.getServerRotation().getYaw());
            lookEvent.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
            changeHeadRotationEvent.setYaw(Rot.getServerRotation().getYaw());
            changeHeadRotationEvent.setPitch(Rot.getServerRotation().getPitch());
        }

        if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
            UpdateBodyRotationEvent.setYaw(Rot.getServerRotation().getYaw());
        }
    }

    private void updateBlockPos() {
        BlockPos newBlockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        if (!mc.theWorld.isAirBlock(newBlockPos)) {
            blockPos = newBlockPos;
        }
    }

    void legitPlace() {
        if (mc.currentScreen != null) return;
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, 4.5, Rot.getServerRotation());

//        if (findBlock() == -1 || mouseOver == null || mouseOver.getBlockPos() == null || mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock().getMaterial() == Material.air) {
//            return;
//        }
//
//        if (stopWatch.reachedMS(delay)) {
//            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
//                mc.thePlayer.swingItem();
//                stopWatch.reset();
//                delay = 1000 / RandomUtils.nextInt(minCps.getValue(), maxCps.getValue());
//            }
//        }
        if (mouse != null && mouse.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && mouse.sideHit != EnumFacing.UP && mouse.sideHit != EnumFacing.DOWN) {
            if (mc.playerController.onPlayerRightClick(
                    mc.thePlayer,
                    mc.theWorld,
                    mc.thePlayer.inventory.getCurrentItem(),
                    mouse.getBlockPos(),
                    mouse.sideHit,
                    mouse.hitVec
            )) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
        }
    }

    void rotate() {
        if (mc.currentScreen != null) return;
//        boolean moveDiagonally = false;
//        float yaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw - 180), 45);
//
//        if (Math.round(yaw / 45f) % 2 != 0) {
//            moveDiagonally = true;
//        }
//
//        Rot rotation = null;
//
//        MovingObjectPosition leftRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rot(yaw + 45, getPitch(yaw)));
//        MovingObjectPosition rightRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rot(yaw - 45, getPitch(yaw)));
//
//        if (!moveDiagonally) {
//            if (leftRayCast != null && leftRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                rotation = new Rot(yaw + 45, getPitch(yaw));
//            } else if (rightRayCast != null && rightRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                rotation = new Rot(yaw - 45, getPitch(yaw));
//            }
//        } else {
//            rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(yaw));
//        }'

        float yaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), 45);

        if (yaw / 45 % 2 == 0) {
            yaw += 45;
        }

        Rot rotation = new Rot(MathHelper.wrapDegree(yaw), getPitch(MathHelper.wrapDegree(yaw)));

        Delta delta = RotUtils.getDelta(Rot.getServerRotation(), rotation);

        delta = delta.limit(
                RandomUtils.nextInt(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                RandomUtils.nextInt(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
        );

        delta = delta.divine(
                smooth.getValue(),
                smooth.getValue()
        );

        delta = RotUtils.fixDelta(delta);

        Rot.setServerRotation(new Rot(
                Rot.getServerRotation().getYaw() + delta.getYaw(),
                Math.clamp(Rot.getServerRotation().getPitch() + delta.getPitch(), -90, 90)
        ));
    }

    private float getPitch(float yaw) {
        Map<Float, MovingObjectPosition> positionHashMap = new HashMap<>();

        float step = 0.1f;
        for (float i = 45; i < 81; i += step) {
            MovingObjectPosition mouses = RayCastUtils.rayCast(3, 4.5, new Rot(yaw, i));
            if (mouses == null || mouses.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || positionHashMap.containsValue(mouses)
                    || mouses.sideHit == EnumFacing.UP
                    || mouses.sideHit == EnumFacing.DOWN) continue;
            positionHashMap.put(i, mouses);
        }

        if (positionHashMap.isEmpty()) {
            return 79;
        }

        List<Float> pitches = new ArrayList<>(positionHashMap.keySet());

        pitches.sort(Comparator.comparingDouble(pitch -> Math.abs(Rot.getServerRotation().getPitch()) - pitch));
        mouse = positionHashMap.get(pitches.getFirst());
        return pitches.getFirst();
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
