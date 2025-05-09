package me.hackclient.module.impl.player;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.visual.Shadows;
import me.hackclient.settings.impl.*;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.math.MathUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.move.MoveUtils;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.rotation.Delta;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name = "Scaffold", category = Category.PLAYER)
public class Scaffold extends Module {

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

    final ModeSetting clickMode = new ModeSetting("ClickMode", this)
            .addModes("AutoPlace", "Legit")
            .setMode("AutoPlace");

    final BooleanSetting swingItem = new BooleanSetting("ServerSwingItem", this, true);

    IntegerSetting minCps = new IntegerSetting("MinCps", this, () -> clickMode.getMode().equals("Legit"), 1, 40, 7) {
        @Override
        public int getValue() {
            if (maxCps.value < value) { value = maxCps.value; }
            return super.getValue();
        }
    };

    IntegerSetting maxCps = new IntegerSetting("MaxCps", this, () -> clickMode.getMode().equals("Legit"), 0, 40, 11) {
        @Override
        public int getValue() {
            if (minCps.value > value) { value = minCps.value; }
            return super.getValue();
        }
    };

    final BooleanSetting bypassServerPitch = new BooleanSetting("BypassServerPitch", this, true);
    FloatSetting serverPitch = new FloatSetting("ServerPitch", this, bypassServerPitch::isToggled , 70, 85, 77,0.1f);
    FloatSetting bestPitchNoDiagonal = new FloatSetting("FrontPitch", this, 70, 85, 77,0.1f);
    FloatSetting diagonalPitch = new FloatSetting("DiagonalPitch", this, 70, 85, 77,0.1f);
    final BooleanSetting alwaysSprint = new BooleanSetting("AlwaysSprint", this, true);

    final BooleanSetting sneakIfRotate = new BooleanSetting("SneakIfRotate", this, true);
    final BooleanSetting sneakIfNoBlocks = new BooleanSetting("SneakIfNoBlocks", this, true);

    final BooleanSetting autoThirdPerson = new BooleanSetting("ThirdPerson", this, true);

    final BooleanSetting render = new BooleanSetting("Render", this, true);
    final ColorSetting color = new ColorSetting("Color", this, render::isToggled, 1,1,1,1);

    final StopWatch stopWatch;

    float bestPitch = 77f;

    int delay = 0;

    BlockPos renderPos = null;
    long lastTime = 0L;
    Shadows shadows;

    int personFirst;

    @Override
    public void onEnable() {
        super.onEnable();
        if (autoThirdPerson.isToggled()) {
            personFirst = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.thePlayer.inventory.currentItem = mc.thePlayer.inventory.fakeCurrentItem;
        if (autoThirdPerson.isToggled()) {
            mc.gameSettings.thirdPersonView = personFirst;
        }
    }

    public Scaffold() {
        stopWatch = new StopWatch();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        switch (clickMode.getMode()) {
            case "AutoPlace" -> {
                if (event instanceof DrawBlockHighlightEvent && mc.currentScreen == null) {
                    ragePlace();
                }
            }
            case "Legit" -> {
                if (event instanceof TickEvent && mc.currentScreen == null) {
                    legitPlace();
                }
            }
        }

        if (event instanceof TickEvent && mc.currentScreen == null) {
            MovingObjectPosition renderRayCast = RayCastUtils.rayCast(4.5, 4.5, Rotation.getServerRotation());
            BlockPos analyzingBlock = renderRayCast.getBlockPos();
            if (analyzingBlock != null && renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) renderPos = analyzingBlock;
            rotate();
        }

        if (event instanceof Render3DEvent && renderPos != null && render.isToggled()) {
            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.module.get("Scaffold")) {
                BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderPos, color.getRed(), color.getGreen(), color.getBlue(), 1f, 1.0F, 0));
            }
            RenderUtils.drawBlockESP(renderPos, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1.0F, 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent moveEvent) {
            MoveUtils.moveFix(moveEvent, MoveUtils.getDirection(mc.thePlayer.rotationYaw, moveEvent.getForward(), moveEvent.getStrafe()));
            if (sneakIfRotate.isToggled() && lastDelta > 0) moveEvent.setSneak(true);
            if (sneakIfNoBlocks.isToggled() && findBlock() == -1) moveEvent.setSneak(true);
        }

        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setYaw(Rotation.getServerRotation().getYaw());
        }

        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                mc.thePlayer.inventory.currentItem = slot;
            }
        }

        if (event instanceof SprintEvent) {
            if (alwaysSprint.isToggled()) {
                if (MoveUtils.isMoving()) mc.thePlayer.setSprinting(true);
            } else {
                if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rotation.getServerRotation().getYaw())) > 90 - 22.5) {
                    mc.thePlayer.setSprinting(false);
                }
            }
        }

        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rotation.getServerRotation().getYaw());
        }

        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rotation.getServerRotation().getYaw());
            if (bypassServerPitch.isToggled()) motionEvent.setPitch(serverPitch.getValue()); else motionEvent.setPitch(Rotation.getServerRotation().getPitch());
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
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, 4.5, Rotation.getServerRotation());

        if (mouseOver == null || mouseOver.getBlockPos() == null || mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock().getMaterial() == Material.air) {
            return;
        }

        if (stopWatch.reachedMS(delay)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
                mc.rightClickMouse();
                if (swingItem.isToggled()) {
                    mc.thePlayer.swingItem();
                }
                stopWatch.reset();
                delay = 1000 / RandomUtils.nextInt(minCps.getValue(), maxCps.getValue());
            }
        }
    }

    void ragePlace() {
        if (mc.currentScreen != null) return;
        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack == null) return;
        if (sneakIfRotate.isToggled() && lastDelta > 0) return;
        if (!(stack.getItem() instanceof ItemBlock)) return;

        MovingObjectPosition mouse = mc.objectMouseOver;

        if (mouse == null
                || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || mouse.sideHit == EnumFacing.UP
                || mouse.sideHit == EnumFacing.DOWN
        ) return;

        BlockPos pos = mouse.getBlockPos();
        if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() != Material.air) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25) {
                mc.rightClickMouse();
                if (swingItem.isToggled()) {
                    mc.thePlayer.swingItem();
                }
            }
        }
    }

    double lastDelta = 0;

    void rotate() {
        boolean moveDiagonally = false;
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), 45);

        if (Math.round(roundedYaw / 45f) % 2 != 0) {
            moveDiagonally = true;
        }

        Rotation rotation = null;

        if (!moveDiagonally) {
            MovingObjectPosition leftRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rotation(roundedYaw + 45, bestPitch));
            MovingObjectPosition rightRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rotation(roundedYaw - 45, bestPitch));

            bestPitch = bestPitchNoDiagonal.getValue();

            if (leftRayCast != null && leftRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                rotation = new Rotation(MathHelper.wrapDegree(roundedYaw + 45), bestPitch);
            } else if (rightRayCast != null && rightRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                rotation = new Rotation(MathHelper.wrapDegree(roundedYaw - 45), bestPitch);
            }
        } else {
            bestPitch = diagonalPitch.getValue();
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

        lastDelta = delta.hypot();

        Rotation.setServerRotation(new Rotation(
                Rotation.getServerRotation().getYaw() + delta.getYaw(),
                Math.clamp(Rotation.getServerRotation().getPitch() + delta.getPitch(), -90, 90)
        ));
    }

    public int findBlock() {
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[i];
            if (item == null || !(item.getItem() instanceof ItemBlock block) || block.getBlock() instanceof BlockSand || block.getBlock() instanceof BlockSoulSand || block.getBlock() instanceof BlockTNT) continue;
            bestSlot = i;
        }

        return bestSlot;
    }
}