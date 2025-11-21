package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(name = "Fucker", category = Category.PLAYER)
public class Fucker extends Module {

    FloatSetting breakDistance = new FloatSetting("BreakDistance", this, 1f,6f,4.5f,0.1f);
    DoubleSlider destroyDelayTicks = new DoubleSlider("DestroyDelayTicks", this, 0,5,5,1f);

    CheckBox whiteListOwnBed = new CheckBox("WhiteListOwnBed", this);

    public BlockPos bedPos;
    private int breakTicks;
    private int delayTicks;
    private Vec3 home;

    @Override
    public void onEnable() {
        super.onEnable();
        bedPos = null;


        breakTicks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }

    @Override
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
                reset();
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

        if (bedPos != null) {
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
        double minDistance = Double.MAX_VALUE;
        BlockPos closestBed = null;

        for (double x = mc.thePlayer.posX - range; x <= mc.thePlayer.posX + range; x++) {
            for (double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - range; y <= mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + range; y++) {
                for (double z = mc.thePlayer.posZ - range; z <= mc.thePlayer.posZ + range; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed &&
                        (mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD ||
                            mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)) {

                        double distance = DistanceUtils.getDistance(pos);

                        if (distance < minDistance) {
                            minDistance = distance;
                            closestBed = pos;
                        }
                    }
                }
            }
        }

        if (closestBed != null) {
            bedPos = closestBed;
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
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, bedPos, EnumFacing.UP));
        } else if (breakTicks >= totalBreakTicks) {
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bedPos, EnumFacing.UP));

            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, 1);

            reset();
            return;
        } else {
            mc.thePlayer.swingItem();
        }

        breakTicks += 1;

        int currentProgress = (int) (((double) breakTicks / totalBreakTicks) * 100);
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, currentProgress / 10);
    }

    private void reset() {
        if (bedPos != null) mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, -1);
        breakTicks = 0;
        delayTicks = destroyDelayTicks.getRandomizedIntValue();
        bedPos = null;
    }

    public boolean handleRotate() {
        return !Modules.getModule(Scaffold.class).isToggled() && TargetStorage.getTarget() == null;
    }

    public void destroy() {
        if (bedPos != null) {
            Rot needRot = RotUtils.calculate(new Vector3d(bedPos.getX(), bedPos.getY(), bedPos.getZ()), getEnumFacing(bedPos));
            Rot.setServerRotation(needRot.fix());

            mine(bedPos);
        } else {
            reset();
        }
    }

    public static EnumFacing getEnumFacing(BlockPos pos) {
        Vec3 eyesPos = mc.thePlayer.getPositionVector();

        if (pos.getY() < eyesPos.yCoord) {
            return EnumFacing.UP;
        } else if (pos.getY() > eyesPos.yCoord) {
            return EnumFacing.DOWN;
        } else {
            return mc.thePlayer.getHorizontalFacing().getOpposite();
        }
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
