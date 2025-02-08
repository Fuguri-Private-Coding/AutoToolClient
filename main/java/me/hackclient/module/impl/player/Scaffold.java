package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
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
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@ModuleInfo(
        name = "Scaffold",
        category = Category.PLAYER
)
public class Scaffold extends Module {

    IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this, 1, 180, 30);
    IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this, 1, 180, 30);
    IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this, 1, 180, 15);
    IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this, 1, 180, 15);
    FloatSetting smooth = new FloatSetting("Smooth", this, 1, 10, 2f, 0.1f) {};

    final ModeSetting clickMode = new ModeSetting(
            "ClickMode",
            this,
            "AutoPlace",
            new String[] {
                    "AutoPlace",
                    "Legit"
            });

    final BooleanSetting swingItem = new BooleanSetting("SwingItem", this,() -> clickMode.getMode().equals("AutoPlace"), true);

    IntegerSetting minCps = new IntegerSetting("MinCps", this,() -> clickMode.getMode().equals("Legit"), 0, 40, 7);
    IntegerSetting maxCps = new IntegerSetting("MaxCps", this,() -> clickMode.getMode().equals("Legit"), 0, 40, 11);

    final BooleanSetting debug = new BooleanSetting("Debug", this, true);

    final StopWatch stopWatch;
    static final float bestPitch = 75.5f;

    int delay = 0;
    BlockPos standingOn = null;
    long lastTime = 0L;

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
    }

    public Scaffold() {
        stopWatch = new StopWatch();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof TickEvent && mc.currentScreen == null) {
            switch (clickMode.getMode()) {
                case "AutoPlace" -> ragePlace(event);
                case "Legit" -> legitPlace();
            }
            rotate();
        }
        if (event instanceof Render3DEvent && standingOn != null) {
            RenderUtils.start3D();
            GlStateManager.disableDepth();
            GL11.glPointSize(10f);
            GL11.glBegin(GL11.GL_POINTS);
            GL11.glVertex3d(standingOn.getX() - mc.getRenderManager().viewerPosX + 0.5, standingOn.getY() - mc.getRenderManager().viewerPosY + 0.5, standingOn.getZ() - mc.getRenderManager().viewerPosZ + 0.5);
            GL11.glEnd();
            GL11.glPointSize(1f);

            GlStateManager.enableDepth();
            RenderUtils.stop3D();
        }
        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setCanceled(true);
            MoveUtils.silentMoveFix(moveFlyingEvent);
        }
        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                mc.thePlayer.inventory.currentItem = slot;
            }
        }
        if (event instanceof PacketEvent packetEvent && packetEvent.getPacket() instanceof C08PacketPlayerBlockPlacement && debug.isToggled()) {
            ClientUtils.chatLog("C08PacketBlockPlacement");
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

    void legitPlace() {
        BlockPos analyzingBlock = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
        if (!mc.theWorld.isAirBlock(analyzingBlock)) {
            standingOn = analyzingBlock;
        }

        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, Rotation.getServerRotation());

        if (mouseOver == null || mouseOver.getBlockPos() == null || mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock().getMaterial() == Material.air) {
            return;
        }

        if (stopWatch.reachedMS(delay)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());

                stopWatch.reset();
                delay = 1000 / RandomUtils.nextInt(minCps.getValue(), maxCps.getValue());
            }
        }
    }

    void ragePlace(Event event) {
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.currentScreen != null) return;

            ItemStack stack = mc.thePlayer.getHeldItem();

            if (stack == null) return;

            if (!(stack.getItem() instanceof ItemBlock)) return;

            MovingObjectPosition mouse = mc.objectMouseOver;

            if (mouse == null
                    || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || mouse.sideHit == EnumFacing.UP
                    || mouse.sideHit == EnumFacing.DOWN) return;

            BlockPos pos = mouse.getBlockPos();
            if (standingOn == null || pos.getX() != standingOn.getX() || pos.getY() != standingOn.getY() || pos.getZ() != standingOn.getZ()) {
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                if (block != null && block != Blocks.air && !(block instanceof BlockLiquid)) {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25) {
                        mc.rightClickMouse();
                        if (swingItem.isToggled()) {
                            mc.thePlayer.swingItem();
                            mc.getItemRenderer().resetEquippedProgress();
                            mc.rightClickMouse();
                        }
                        standingOn = pos;
                        lastTime = System.currentTimeMillis();
                    }
                }
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

        if (mc.thePlayer.hurtResistantTime > 0) {
            for (float i = 0; i < 180; i += 0.1f) {

            }
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
        int bestSize = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemBlock block) || item.stackSize <= bestSize ||  block.getBlock() instanceof BlockSand || block.getBlock() instanceof BlockSoulSand) continue;
            bestSlot = i;
            bestSize = item.stackSize;
        }

        return bestSlot;
    }
}