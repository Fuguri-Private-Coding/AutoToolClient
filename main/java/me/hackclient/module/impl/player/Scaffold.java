package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.Comparator;

@ModuleInfo(
        name = "Scaffold",
        category = Category.PLAYER
)
public class Scaffold extends Module {

    IntegerSetting yawSpeed = new IntegerSetting("YawSpeed", this, 1, 180, 30);
    IntegerSetting pitchSpeed = new IntegerSetting("PitchSpeed", this, 1, 180, 15);
    FloatSetting smoothes = new FloatSetting("Smooth", this, 1, 10, 2f, 0.1f);
    BooleanSetting saveWalk = new BooleanSetting("SneakOnFirstBlock", this, true);

    int placedBlocks;
    long lastTime = 0L;
    BlockPos blockPos = null;

    final Rotation[] bestGodbrigdeRotations = new Rotation[] {
            new Rotation(-45.0f, 77.0f),
            new Rotation(135.0f, 77.0f),
            new Rotation(-135.0f, 77.0f),
            new Rotation(45.0f, 77.0f)
    };

    @Override
    public void onDisable() {
        super.onDisable();
        placedBlocks = 0;
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof MoveButtonEvent moveButtonEvent && saveWalk.isToggled()) {
            if (placedBlocks == 0 && mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ))) {
                moveButtonEvent.setSneak(true);
            }
        }
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.currentScreen != null)
                return;

            ItemStack stack = mc.thePlayer.getHeldItem();

            if (stack == null)
                return;

            if (!(stack.getItem() instanceof ItemBlock))
                return;

            MovingObjectPosition mouse = mc.objectMouseOver;

            if (mouse == null
                    || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || mouse.sideHit == EnumFacing.UP
                    || mouse.sideHit == EnumFacing.DOWN) {
                return;
            }

            BlockPos pos = mouse.getBlockPos();
            if (blockPos == null || pos.getX() != blockPos.getX() || pos.getY() != blockPos.getY() || pos.getZ() != blockPos.getZ()) {
                Block b = mc.theWorld.getBlockState(pos).getBlock();
                if (b != null && b != Blocks.air && !(b instanceof BlockLiquid)) {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25)  {
                        mc.rightClickMouse();
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                        mc.rightClickMouse();
                        blockPos = pos;
                        lastTime = System.currentTimeMillis();
                        placedBlocks++;
                    }
                }
            }
        }
        if (event instanceof TickEvent) {
            Rotation playerRotation = new Rotation(
                    MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180),
                    mc.thePlayer.rotationPitch
            );
            Rotation nearest = Arrays.stream(bestGodbrigdeRotations).min(Comparator.comparing(rotation -> RotationUtils.getDelta(playerRotation, rotation).hypot())).orElse(null);
            if (nearest == null) { return; }

            Delta delta = RotationUtils.getDelta(Rotation.getServerRotation(), nearest);

            delta.setYaw(MathHelper.clamp(delta.getYaw(), -yawSpeed.getValue(), yawSpeed.getValue()));
            delta.setPitch(MathHelper.clamp(delta.getPitch() , -pitchSpeed.getValue(), pitchSpeed.getValue()));

            delta.setYaw(delta.getYaw() / smoothes.getValue());
            delta.setPitch(delta.getPitch() / smoothes.getValue());

            delta = RotationUtils.fixDelta(delta);
            Rotation.setServerRotation(
                    new Rotation(
                            Rotation.getServerRotation().getYaw() + delta.getYaw(),
                            Rotation.getServerRotation().getPitch() + delta.getPitch()
                    )
            );
        }
        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
            motionEvent.setPitch(Rotation.getServerRotation().getPitch());
        }
        if (event instanceof LookEvent lookEvent) {
            lookEvent.setYaw(Rotation.getServerRotation().getYaw());
            lookEvent.setPitch(Rotation.getServerRotation().getPitch());
        }
        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setCanceled(true);
            MoveUtils.silentMoveFix(moveFlyingEvent);
        }
        if (event instanceof SprintEvent) {
            if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
                mc.thePlayer.setSprinting(false);
            }
        }
        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
        }
        if (event instanceof ChangeHeadRotationEvent changeHeadRotationEvent) {
            changeHeadRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
            changeHeadRotationEvent.setPitch(Rotation.getServerRotation().getPitch());
        }
        if (event instanceof UpdateBodyRotationEvent UpdateBodyRotationEvent) {
            UpdateBodyRotationEvent.setYaw(Rotation.getServerRotation().getYaw());
        }
    }
}
