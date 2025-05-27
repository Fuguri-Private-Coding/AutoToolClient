package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.Shadows;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(name = "NewScaffold", category = Category.PLAYER)
public class NewScaffold extends Module {

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

    final CheckBox autoThirdPerson = new CheckBox("ThirdPerson", this, true);

    final CheckBox render = new CheckBox("Render", this, true);
    final CheckBox fadeColor = new CheckBox("FadeColor", this, render::isToggled);
    final ColorSetting color1 = new ColorSetting("Color1", this, render::isToggled, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, () -> render.isToggled() && fadeColor.isToggled(), 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, () -> render.isToggled() && fadeColor.isToggled(),0.1f, 20, 1, 0.1f);

    final StopWatch stopWatch;
    BlockPos renderPos;
    int personFirst, delay = 0;
    Shadows shadows;

    public NewScaffold() {
        stopWatch = new StopWatch();
    }

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

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof TickEvent) {
            if (mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) renderPos = mc.objectMouseOver.getBlockPos();

        }

        if (event instanceof LegitClickTimingEvent) {
            int slot = findBlock();

            if (slot == -1) { return; }

            if (mc.thePlayer.inventory.currentItem != slot) {
                mc.thePlayer.inventory.currentItem = slot;
            }
        }

        if (event instanceof SprintEvent) {
            if (Math.abs(MathHelper.wrapDegree((float) Math.toDegrees(MoveUtils.getDirection(mc.thePlayer.rotationYaw))) - MathHelper.wrapDegree(Rot.getServerRotation().getYaw())) > 90 - 22.5) {
                mc.thePlayer.setSprinting(false);
            }
        }

        if (event instanceof MoveEvent moveEvent) {
            MoveUtils.moveFix(moveEvent, MoveUtils.getDirection(mc.thePlayer.rotationYaw, moveEvent.getForward(), moveEvent.getStrafe()));
        }

        if (event instanceof MoveFlyingEvent moveFlyingEvent) {
            moveFlyingEvent.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof JumpEvent jumpEvent) {
            jumpEvent.setYaw(Rot.getServerRotation().getYaw());
        }

        if (event instanceof MotionEvent motionEvent) {
            motionEvent.setYaw(Rot.getServerRotation().getYaw());
            motionEvent.setPitch(Rot.getServerRotation().getPitch());
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

        if (event instanceof Render3DEvent) {
            Color fadeColor;
            if (this.fadeColor.isToggled()) {
                fadeColor = ColorUtils.mixColors(color1.getColor(), color2.getColor(), fadeSpeed.getValue());
            } else {
                fadeColor = color1.getColor();
            }

            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.module.get("Scaffold")) {
                BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderPos, fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f,  0, 1));
            }
            RenderUtils.drawBlockESP(renderPos, fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f, 0, 1);
            ColorUtils.resetColor();
            RenderUtils.stop3D();
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
                mc.thePlayer.swingItem();
                stopWatch.reset();
                delay = 1000 / RandomUtils.nextInt(minCps.getValue(), maxCps.getValue());
            }
        }
    }

    void ragePlace() {
        if (mc.currentScreen != null) return;
        ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack == null) return;
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
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec)) {
                mc.thePlayer.swingItem();
            }
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
                    || !block.getBlock().isFullBlock()
                    || !block.getBlock().isOpaqueCube()
                    || block.getBlock().getMaterial().isReplaceable()
                    || block.getBlock().getMaterial().isLiquid()
            ) continue;
            bestSlot = i;
        }

        return bestSlot;
    }
}
