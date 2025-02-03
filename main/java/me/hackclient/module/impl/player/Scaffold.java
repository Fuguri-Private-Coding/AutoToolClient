package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.math.MathUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.Arrays;
import java.util.Comparator;

@ModuleInfo(
        name = "Scaffold",
        category = Category.PLAYER
)
public class Scaffold extends Module {

//    IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this, 1, 180, 30);
//    IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this, 1, 180, 30);
//    IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this, 1, 180, 15);
//    IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this, 1, 180, 15);
//    FloatSetting smooth = new FloatSetting("Smooth", this, 1, 10, 2f, 0.1f);
//    BooleanSetting saveWalk = new BooleanSetting("SneakOnFirstBlock", this, true);
//    BooleanSetting placeOnlyHorizontal = new BooleanSetting("PlaceOnlyHorizontal", this, true);
//    BooleanSetting swingItem = new BooleanSetting("PlayerSwingItem", this, true);
//
//    BlockPos standing = null;
//    int lastSlot;
//    int placedBlocks;
//    long lastTime = 0L;
//    BlockPos blockPos = null;
//
//    final Rotation[] bestGodbrigdeRotations = new Rotation[] {
//            new Rotation(-45.0f, 75.5f),
//            new Rotation(135.0f, 75.5f),
//            new Rotation(-135.0f, 75.5f),
//            new Rotation(45.0f, 75.5f),
//    };
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        lastSlot = 0;
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        placedBlocks = 0;
//        mc.thePlayer.inventory.currentItem = lastSlot;
//        standing = null;
//    }
//
//    @Override
//    public void onEvent(Event event) {
//        super.onEvent(event);
//        if (event instanceof MoveButtonEvent moveButtonEvent && saveWalk.isToggled()) {
//            if (placedBlocks == 0 && mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ)) || mc.thePlayer.inventory.getCurrentItem() == null) {
//                moveButtonEvent.setSneak(true);
//            }
//        }
//        if (event instanceof Render3DEvent && standing != null) {
//            RenderUtils.start3D();
//            PixelReplacerUtils.addToDraw(() -> {
//                float a = 1f;
//                RenderUtils.renderHitBox(new AxisAlignedBB(standing.getX(), standing.getY(), standing.getZ(), standing.getX() + 0.5, standing.getY() + 0.5, standing.getZ() + 0.5));
//            });
//            RenderUtils.stop3D();
//        }
//        if (event instanceof TickEvent) {
//
//            BlockPos bp1 = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
//            if (!mc.theWorld.isAirBlock(bp1)) {
//                standing = bp1;
//            }
//
//            Rotation nearest = null;
//
//            float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), 45);
//            Rotation left  = new Rotation(MathHelper.wrapDegree(roundedYaw + 45f), 75.5f);
//            Rotation right = new Rotation(MathHelper.wrapDegree(roundedYaw - 45f), 75.5f);
//
//            MovingObjectPosition leftMouseOver = RayCastUtils.rayCast(4.5, left);
//            MovingObjectPosition rightMouseOver = RayCastUtils.rayCast(4.5, right);
//
//            if (leftMouseOver != null && leftMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                nearest = left;
//            } else if (rightMouseOver != null && rightMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                nearest = right;
//            }
//
//            if (nearest == null) { return; }
//
//            Delta delta = RotationUtils.getDelta(Rotation.getServerRotation(), nearest);
//
//            float randomizedYawSpeed = RandomUtils.nextFloat(minYawSpeed.getValue(), maxYawSpeed.getValue());
//            float randomizedPitchSpeed = RandomUtils.nextFloat(minPitchSpeed.getValue(), maxPitchSpeed.getValue());
//
//            delta.setYaw(MathHelper.clamp(delta.getYaw(), -randomizedYawSpeed, randomizedYawSpeed));
//            delta.setPitch(MathHelper.clamp(delta.getPitch() , -randomizedPitchSpeed, randomizedPitchSpeed));
//
//            delta.setYaw(delta.getYaw() / smooth.getValue());
//            delta.setPitch(delta.getPitch() / smooth.getValue());
//
//            delta = RotationUtils.fixDelta(delta);
//            Rotation.setServerRotation(
//                    new Rotation(
//                            Rotation.getServerRotation().getYaw() + delta.getYaw(),
//                            Rotation.getServerRotation().getPitch() + delta.getPitch()
//                    )
//            );
//        }
//        if (event instanceof LegitClickTimingEvent) {
//            if (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock)) {
//                int slot = findBlock();
//
//                if (slot == -1)
//                    return;
//
//                lastSlot = mc.thePlayer.inventory.currentItem;
//                mc.thePlayer.inventory.currentItem = slot;
//            }
//        }
//        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
//            moveFlyingEvent.setCanceled(true);
//            MoveUtils.silentMoveFix(moveFlyingEvent);
//
//            if (mc.currentScreen != null)
//                return;
//
//            ItemStack stack = mc.thePlayer.getHeldItem();
//
//            if (stack == null)
//                return;
//
//            if (!(stack.getItem() instanceof ItemBlock))
//                return;
//
//            MovingObjectPosition mouse = RayCastUtils.rayCast(4.5, Rotation.getServerRotation());
//
//            if (mouse == null
//                    || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
//                    || ((mouse.sideHit == EnumFacing.UP || mouse.sideHit == EnumFacing.DOWN)) && placeOnlyHorizontal.isToggled()) {
//                return;
//            }
//
//            BlockPos pos = mouse.getBlockPos();
//            if (blockPos == null || pos.getX() != blockPos.getX() || pos.getY() != blockPos.getY() || pos.getZ() != blockPos.getZ()) {
//                Block block = mc.theWorld.getBlockState(pos).getBlock();
//                if (block != null && block != Blocks.air && !(block instanceof BlockLiquid)) {
//                    if (mc.playerController.onPlayerRightClickNoPacket(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec))  {
//                        mc.rightClickMouse();
//                        if (swingItem.isToggled()) {
//                            mc.thePlayer.swingItem();
//                            mc.getItemRenderer().resetEquippedProgress();
//                        }
//                        blockPos = pos;
//                        lastTime = System.currentTimeMillis();
//                        ++placedBlocks;
//
//                        ClientUtils.chatLog("Clicked " + placedBlocks);
//
//                        if (placedBlocks % 7 == 0 && mc.thePlayer.onGround) {
//                            mc.thePlayer.movementInput.sneak = true;
//                        }
//                    }
//                }
//            }
//        }
//        if (event instanceof MotionEvent motionEvent) {
//            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
//            motionEvent.setPitch(Rotation.getServerRotation().getPitch());
//        }
//        if (event instanceof LookEvent lookEvent) {
//            lookEvent.setYaw(Rotation.getServerRotation().getYaw());
//            lookEvent.setPitch(Rotation.getServerRotation().getPitch());
//        }
//        if (event instanceof SprintEvent) {
//            if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
//                mc.thePlayer.setSprinting(false);
//            }
//        }
//        if (event instanceof JumpEvent jumpEvent) {
//            jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
//        }
//        if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
//            changeHeadRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
//            changeHeadRotationEvent.setPitch(Rotation.getServerRotation().getPitch());
//        }
//        if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
//            UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
//        }
//    }
//

    IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this, 1, 180, 30);
    IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this, 1, 180, 30);
    IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this, 1, 180, 15);
    IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this, 1, 180, 15);
    FloatSetting smooth = new FloatSetting("Smooth", this, 1, 10, 2f, 0.1f);

    IntegerSetting minCps = new IntegerSetting("MinCps", this, 0, 40, 7);
    IntegerSetting maxCps = new IntegerSetting("MaxCps", this, 0, 40, 11);

    final StopWatch stopWatch;
    static final float bestPitch = 75.5f;

    int lastSlot = -1;
    int delay = 0;
    BlockPos standingOn = null;

    @Override
    public void onDisable() {
        super.onDisable();
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
            lastSlot = -1;
        }
    }

    public Scaffold() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent) {
            place();
            rotate();
        }
        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setCanceled(true);
            MoveUtils.silentMoveFix(moveFlyingEvent);
        }
        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                lastSlot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = slot;
            }
        }
        if (event instanceof UpdateRenderingItem updateRenderingItem && lastSlot != -1) {
            updateRenderingItem.setStack(mc.thePlayer.inventory.mainInventory[lastSlot]);
        }
        if (event instanceof SprintEvent) {
            if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
                mc.thePlayer.setSprinting(false);
            }
        }
        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
        }
        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
            motionEvent.setPitch(Rotation.getServerRotation().getPitch());
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
    }

    void place() {
        BlockPos analyzingBlock = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
        if (!mc.theWorld.isAirBlock(analyzingBlock)) {
            standingOn = analyzingBlock;
        }

        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, Rotation.getServerRotation());

        if (mouseOver == null || mouseOver.getBlockPos() == null /*|| mouseOver.sideHit == EnumFacing.UP || mouseOver.sideHit == EnumFacing.DOWN*/) {
            return;
        }

        if (mc.playerController.onPlayerRightClickNoPacket(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
             if (stopWatch.reachedMS(delay)) {
                 stopWatch.reset();
                 mc.rightClickMouse();
                 delay = 1000 / RandomUtils.nextInt(minCps.getValue(), maxCps.getValue());
             }
        }
    }

    void rotate() {
        boolean moveDiagonally = false;
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), 45);

        if (Math.round(roundedYaw / 45f) % 2 != 0) {
            moveDiagonally = true;
        }

        Rotation rotation = null;

        if (!moveDiagonally) {
            MovingObjectPosition leftRayCast = RayCastUtils.rayCast(4.5, new Rotation(roundedYaw + 45, bestPitch));
            MovingObjectPosition rightRayCast = RayCastUtils.rayCast(4.5, new Rotation(roundedYaw - 45, bestPitch));

            if (leftRayCast != null && leftRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                rotation = new Rotation(MathHelper.wrapDegree(roundedYaw + 45), bestPitch);
            } else if (rightRayCast != null && rightRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                rotation = new Rotation(MathHelper.wrapDegree(roundedYaw - 45), bestPitch);
            }
        } else {
            rotation = new Rotation(roundedYaw, bestPitch);
        }

        if (rotation == null) {
            return;
        }

        Delta delta = RotationUtils.getDelta(Rotation.getServerRotation(), rotation);

        delta = delta.limit(
                RandomUtils.nextInt(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                RandomUtils.nextInt(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
        );

        delta = delta.divine(
                smooth.getValue(),
                smooth.getValue()
        );

        delta = RotationUtils.fixDelta(delta);

        Rotation.setServerRotation(new Rotation(
                Rotation.getServerRotation().getYaw() + delta.getYaw(),
                Rotation.getServerRotation().getPitch() + delta.getPitch()
        ));
    }

    int findBlock() {
        int bestSlot = -1;
//        int bestCount = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemBlock) /*|| item.stackSize <= bestCount*/) continue;
            bestSlot = i;
//            bestCount = item.stackSize;
        }

        return bestSlot;
    }
}