package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.inventory.PlayerUtil;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Delta;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;

import java.awt.*;

@ModuleInfo(name = "Fucker", category = Category.PLAYER)
public class Fucker extends Module {

    FloatSetting breakDistance = new FloatSetting("BreakDistance", this, 1f,6f,4.5f,0.1f);

    CheckBox whiteListOwnBed = new CheckBox("WhiteListOwnBed", this);

    public BlockPos bedPos;
    public boolean rotate = false;
    private int breakTicks;
    private int delayTicks;
    private Vec3 home;

    @Override
    public void onEnable() {
        super.onEnable();
        rotate = false;
        bedPos = null;

        breakTicks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset(true);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof TeleportEvent e) {
            if (whiteListOwnBed.isToggled()) {
                final double distance = mc.thePlayer.getDistance(e.getX(), e.getY(), e.getZ());

                if (distance > 40) {
                    home = new Vec3(e.getX(), e.getY(), e.getZ());
                }
            }
        }

        if (event instanceof TickEvent) {
            if (!handleRotate()) {
                reset(true);
                return;
            }

            searchBedPosition();

            destroy();
        }

        if (event instanceof Render3DEvent && bedPos != null) {
            RenderUtils.start3D();
            RenderUtils.drawBlockESP(bedPos, new Color(1,0,0,1f * breakTicks / 10));
            ColorUtils.resetColor();
            RenderUtils.stop3D();
        }

        if (bedPos != null && rotate) {
            if (event instanceof MotionEvent e) {
                e.setYaw(Rot.getServerRotation().getYaw());
                e.setPitch(Rot.getServerRotation().getPitch());
            }

            if (event instanceof MoveEvent e) {
                MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
            }

            if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
            if (event instanceof UpdateBodyRotationEvent e) e.setYaw(Rot.getServerRotation().getYaw());
            if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());

            if (event instanceof LookEvent e) {
                e.setYaw(Rot.getServerRotation().getYaw());
                e.setPitch(Rot.getServerRotation().getPitch());
            }

            if (event instanceof ChangeHeadRotationEvent e) {
                e.setYaw(Rot.getServerRotation().getYaw());
                e.setPitch(Rot.getServerRotation().getPitch());
            }
        }
    }

    private void searchBedPosition() {
        if (home != null && mc.thePlayer.getDistanceSq(home.xCoord, home.yCoord, home.zCoord) < 35 * 35 && whiteListOwnBed.isToggled()) {
            return;
        }
        bedPos = null;
        double range = breakDistance.getValue();
        for (double x = mc.thePlayer.posX - range; x <= mc.thePlayer.posX + range; x++) {
            for (double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - range; y <= mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + range; y++) {
                for (double z = mc.thePlayer.posZ - range; z <= mc.thePlayer.posZ + range; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed && (mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD || mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)) {
                        bedPos = pos;
                        break;
                    }
                }
            }
        }
    }

    private void mine(BlockPos blockPos) {
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        if (blockState.getBlock() instanceof BlockAir) {
            return;
        }

        float totalBreakTicks = getBreakTicks(bedPos, mc.thePlayer.inventory.currentItem);
        if (breakTicks == 0) {
            rotate = true;
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, bedPos, EnumFacing.UP));
        } else if (breakTicks >= totalBreakTicks) {
            rotate = true;
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bedPos, EnumFacing.UP));

            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, 1);

            reset(false);
            return;
        } else {
            rotate = true;
            mc.thePlayer.swingItem();
        }

        breakTicks += 1;

        int currentProgress = (int) (((double) breakTicks / totalBreakTicks) * 100);
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, currentProgress / 10);
    }

    private void reset(boolean resetRotate) {
        if (bedPos != null) mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, -1);
        breakTicks = 0;
        delayTicks = 5;
        bedPos = null;
        rotate = !resetRotate;
    }

    public boolean handleRotate() {
        return !Client.INST.getModules().getModule(Scaffold.class).isToggled() && Client.INST.getTargetStorage().getTarget() == null;
    }

    public void destroy() {
        if (bedPos != null) {
            if (rotate) {
                Rot needRot = RotUtils.getRotationToBlock(bedPos, getEnumFacing(bedPos));

                Delta delta = RotUtils.getDelta(Rot.getServerRotation(), needRot);

                delta = RotUtils.fixDelta(delta);

                Rot rot = new Rot(
                    Rot.getServerRotation().getYaw() + delta.getYaw(),
                    Rot.getServerRotation().getPitch() + delta.getPitch()
                );

                Rot.setServerRotation(rot);
            }
            rotate = false;

            mine(bedPos);
        } else {
            reset(true);
        }
    }

    public static EnumFacing getEnumFacing(BlockPos pos) {
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (pos.getY() > eyesPos.yCoord) {
            if (PlayerUtil.isReplaceable(pos.add(0, -1, 0))) {
                return EnumFacing.DOWN;
            } else {
                return mc.thePlayer.getHorizontalFacing().getOpposite();
            }
        }

        if (!PlayerUtil.isReplaceable(pos.add(0, 1, 0))) {
            return mc.thePlayer.getHorizontalFacing().getOpposite();
        }

        return EnumFacing.UP;
    }

    private float getBreakTicks(BlockPos bp, int tool) {
        int oldHeld = mc.thePlayer.inventory.currentItem;

        mc.thePlayer.inventory.currentItem = tool;
        IBlockState bs = mc.theWorld.getBlockState(bp);
        float ticks = 1f / bs.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, bp);

        mc.thePlayer.inventory.currentItem = oldHeld;
        return ticks;
    }
}
