package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "ChestAura", category = Category.PLAYER)
public class ChestAura extends Module {

//    FloatSetting distance = new FloatSetting("Distance", this, 1f,6f,4.5f,0.1f);
//
//    public BlockPos chestPos;
//
//    List<BlockPos> whiteList = new CopyOnWriteArrayList<>();
//
//    int delayTicks;
//
//    @Override
//    public void onEvent(Event event) {
//
//        if (event instanceof TickEvent) {
//            if (!handleRotate()) {
//                reset();
//                return;
//            }
//
//            searchChestPosition();
//
//            rotate();
//        }
//
//        if (event instanceof WorldChangeEvent) {
//            whiteList = new CopyOnWriteArrayList<>();
//        }
//
//        if (event instanceof Render3DEvent && chestPos != null) {
//            RenderUtils.start3D();
//            RenderUtils.drawBlockESP(chestPos, new Color(0,0,0,1f));
//            ColorUtils.resetColor();
//            RenderUtils.stop3D();
//        }
//
//        if (chestPos != null) {
//            if (event instanceof MotionEvent e) {
//                e.setYaw(Rot.getServerRotation().getYaw());
//                e.setPitch(Rot.getServerRotation().getPitch());
//            }
//
//            if (event instanceof MoveEvent e) {
//                MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
//            }
//
//            if (event instanceof JumpEvent e) e.setYaw(Rot.getServerRotation().getYaw());
//            if (event instanceof UpdateBodyRotationEvent e) e.setYaw(Rot.getServerRotation().getYaw());
//            if (event instanceof MoveFlyingEvent e) e.setYaw(Rot.getServerRotation().getYaw());
//
//            if (event instanceof LookEvent e) {
//                e.setYaw(Rot.getServerRotation().getYaw());
//                e.setPitch(Rot.getServerRotation().getPitch());
//            }
//
//            if (event instanceof ChangeHeadRotationEvent e) {
//                e.setYaw(Rot.getServerRotation().getYaw());
//                e.setPitch(Rot.getServerRotation().getPitch());
//            }
//        }
//    }
//
//    private void reset() {
//        chestPos = null;
//    }
//
//    public void rotate() {
//        if (chestPos != null) {
//            Rot needRot = RotUtils.calculate(new Vector3d(chestPos.getX(), chestPos.getY(), chestPos.getZ()), getEnumFacing(chestPos));
//            Rot.setServerRotation(needRot.fix());
//
//            if (delayTicks > 0) delayTicks--;
//
//            if (mc.currentScreen == null && delayTicks == 0) {
//                if (mc.playerController.onPlayerRightClick(
//                    mc.thePlayer, mc.theWorld,
//                    mc.thePlayer.inventory.getItemStack(),
//                    chestPos,
//                    getEnumFacing(chestPos),
//                    RotUtils.getVectorForRotation(needRot)
//                )) {
//                    mc.thePlayer.swingItem();
//                    whiteList.add(chestPos);
//                    delayTicks = 5;
//                }
//            }
//        } else {
//            reset();
//        }
//    }
//
//    public static EnumFacing getEnumFacing(BlockPos pos) {
//        Vec3 eyesPos = mc.thePlayer.getPositionVector();
//
//        if (pos.getY() < eyesPos.yCoord) {
//            return EnumFacing.UP;
//        } else if (pos.getY() > eyesPos.yCoord) {
//            return EnumFacing.DOWN;
//        } else {
//            return mc.thePlayer.getHorizontalFacing().getOpposite();
//        }
//    }
//
//    public boolean handleRotate() {
//        return !Modules.getModule(Scaffold.class).isToggled() && TargetStorage.getTarget() == null && !whiteList.contains(chestPos);
//    }
//
//    private void searchChestPosition() {
//        chestPos = null;
//        double range = distance.getValue();
//
//        List<BlockPos> blockPosList = new ArrayList<>();
//
//        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
//            if (tileEntity instanceof TileEntityChest chest && DistanceUtils.getDistance(chest.getPos()) <= range && !whiteList.contains(chest.getPos())) {
//                blockPosList.add(chest.getPos());
//            }
//        }
//
//        blockPosList.sort(Comparator.comparingDouble(DistanceUtils::getDistance));
//
//        chestPos = blockPosList.getFirst();
//    }
}
