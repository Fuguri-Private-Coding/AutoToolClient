package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Blur;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.awt.*;

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

    final Mode clickMode = new Mode("ClickMode", this)
            .addModes("AutoPlace", "Legit")
            .setMode("AutoPlace");

    final CheckBox swingItem = new CheckBox("ServerSwingItem", this, true);

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
    final FloatSetting serverPitch = new FloatSetting("ServerPitch", this, bypassServerPitch::isToggled , 70, 85, 77,0.1f);
    final FloatSetting bestPitchNoDiagonal = new FloatSetting("FrontPitch", this, 70, 85, 77,0.1f);
    final FloatSetting diagonalPitch = new FloatSetting("DiagonalPitch", this, 70, 85, 77,0.1f);
    final CheckBox alwaysSprint = new CheckBox("AlwaysSprint", this, true);
    final CheckBox ninjaBridge = new CheckBox("NinjaBridge", this, true);

    final CheckBox sneakIfRotate = new CheckBox("SneakIfRotate", this, true);
    final CheckBox sneakIfNoBlocks = new CheckBox("SneakIfNoBlocks", this, true);

    final CheckBox autoThirdPerson = new CheckBox("ThirdPerson", this, true);

    final CheckBox render = new CheckBox("Render", this, true);
    final CheckBox fadeColor = new CheckBox("FadeColor", this, render::isToggled);
    final ColorSetting color1 = new ColorSetting("Color1", this, render::isToggled, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, () -> render.isToggled() && fadeColor.isToggled(), 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, () -> render.isToggled() && fadeColor.isToggled(),0.1f, 20, 1, 0.1f);

    final StopWatch stopWatch;

    float bestPitch = 77f;

    int delay = 0;

    BlockPos renderPos = null;
    long lastTime = 0L;
    Shadows shadows;
    Blur blur;

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
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        switch (clickMode.getMode()) {
            case "AutoPlace" -> {
                if (event instanceof DrawBlockHighlightEvent) {
                    ragePlace();
                }
                if (event instanceof TickEvent && !mc.thePlayer.onGround) {
                    legitPlace();
                }
            }
            case "Legit" -> {
                if (event instanceof TickEvent) {
                    legitPlace();
                }
            }
        }

        if (event instanceof TickEvent) {
            MovingObjectPosition renderRayCast = RayCastUtils.rayCast(4.5, 4.5, Rot.getServerRotation());
            BlockPos analyzingBlock = renderRayCast.getBlockPos();
            if (analyzingBlock != null && renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) renderPos = analyzingBlock;
            rotate();
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
            if (blur.isToggled() && blur.module.get("Scaffold")) {
                GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderPos, fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), 1f));
            }
            RenderUtils.drawBlockESP(renderPos, fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
            RenderUtils.stop3D();
        }

        if (event instanceof MoveEvent moveEvent) {
            MoveUtils.moveFix(moveEvent, MoveUtils.getDirection(mc.thePlayer.rotationYaw, moveEvent.getForward(), moveEvent.getStrafe()));
            if (sneakIfRotate.isToggled() && lastDelta > 0) moveEvent.setSneak(true);
            if (sneakIfNoBlocks.isToggled() && findBlock() == -1) moveEvent.setSneak(true);
            if (ninjaBridge.isToggled() && mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX,mc.thePlayer.posY - 0.01, mc.thePlayer.posZ))) moveEvent.setSneak(true);
        }

        if (event instanceof SprintEvent && alwaysSprint.isToggled()) {
            if (MoveUtils.isMoving() && mc.thePlayer.onGround && !mc.thePlayer.movementInput.jump && lastDelta == 0 && !mc.thePlayer.movementInput.sneak) {
                mc.thePlayer.setSprinting(true);
            }
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

    void legitPlace() {
        if (mc.currentScreen != null) return;
        MovingObjectPosition mouseOver = RayCastUtils.rayCast(4.5, 4.5, Rot.getServerRotation());

        if (findBlock() == -1 || mouseOver == null || mouseOver.getBlockPos() == null || mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock().getMaterial() == Material.air) {
            return;
        }

        if (stopWatch.reachedMS(delay)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
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
        if (findBlock() == -1) return;

        MovingObjectPosition mouse = mc.objectMouseOver;

        if (mouse == null
                || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || mouse.sideHit == EnumFacing.UP
                || mouse.sideHit == EnumFacing.DOWN
        ) return;

        BlockPos pos = mouse.getBlockPos();
        if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() != Material.air) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25) {
                if (swingItem.isToggled()) {
                    mc.thePlayer.swingItem();
                }
            }
        }
    }

    double lastDelta = 0;

    void rotate() {
        if (mc.currentScreen != null) return;
        boolean moveDiagonally = false;
        float roundedYaw = (float) MathUtils.round(MathHelper.wrapDegree(mc.thePlayer.rotationYaw + 180), 45);

        if (Math.round(roundedYaw / 45f) % 2 != 0) {
            moveDiagonally = true;
        }

        Rot rotation = null;

       if (!moveDiagonally) {
           MovingObjectPosition leftRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rot(roundedYaw + 45, bestPitch));
           MovingObjectPosition rightRayCast = RayCastUtils.rayCast(4.5, 4.5, new Rot(roundedYaw - 45, bestPitch));

           bestPitch = bestPitchNoDiagonal.getValue();

           if (leftRayCast != null && leftRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
               rotation = new Rot(MathHelper.wrapDegree(roundedYaw + 45), bestPitch);
           } else if (rightRayCast != null && rightRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
               rotation = new Rot(MathHelper.wrapDegree(roundedYaw - 45), bestPitch);
           }
       } else {
            bestPitch = diagonalPitch.getValue();
            rotation = new Rot(roundedYaw, bestPitch);
       }

       if (rotation == null) {
           return;
       }

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

        lastDelta = delta.hypot();

        Rot.setServerRotation(new Rot(
                Rot.getServerRotation().getYaw() + delta.getYaw(),
                Math.clamp(Rot.getServerRotation().getPitch() + delta.getPitch(), -90, 90)
        ));
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