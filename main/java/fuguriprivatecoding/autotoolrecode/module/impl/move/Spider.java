package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.BlockBBEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "Spider", category = Category.MOVE, description = "Позволяет взбиратся по стенам как POV('Паук')")
public class Spider extends Module {

    Mode mode = new Mode("Mode", this)
        .addModes("Polar")
        .setMode("Polar")
        ;

    public final CheckBox fast = new CheckBox("Fast", this, true);

    public final IntegerSetting mouseButton = new IntegerSetting("Mouse button to go up faster", this, 0, 5, 1);

    @Override
    public void onEvent(Event event) {
        switch (mode.getMode()) {
            case "Polar" -> {
                if (event instanceof MotionEvent) {
                    boolean isInsideBlock = insideBlock();
                    if (mc.thePlayer.isCollidedHorizontally && !isInsideBlock) {
                        double yaw = MoveUtils.direction();
                        mc.thePlayer.setPosition(mc.thePlayer.posX + (-MathHelper.sin((float)yaw)) * 0.05, mc.thePlayer.posY, mc.thePlayer.posZ + MathHelper.cos((float)yaw) * 0.05);
                        MoveUtils.stopMotion();
                        MoveUtils.keyBindStop();
                    }

                    Mouse.poll();
                    if (Mouse.isButtonDown(mouseButton.getValue()) && isInsideBlock) {
                        double fastSpeed = (mc.thePlayer.onGround ? (mc.thePlayer.motionY += 0.65999) : (mc.thePlayer.motionY -= 0.005));
                        double slowSpeed = (mc.thePlayer.onGround ? (mc.thePlayer.motionY += 0.64456) : (mc.thePlayer.motionY -= 0.005));
                        mc.thePlayer.motionY = fast.isToggled() ? fastSpeed : slowSpeed;
                    }
                }

                if (event instanceof BlockBBEvent e) {
                    if (insideBlock()) {
                        BlockPos playerPos = mc.thePlayer.getPosition();
                        if (e.getBlockPos().getY() > playerPos.getY()) {
                            e.setBoundingBox(null);
                        }
                    }
                }
            }
        }
    }

    private boolean insideBlock(AxisAlignedBB bb) {
        int x = MathHelper.floor_double(bb.minX);
        while (x < MathHelper.floor_double(bb.maxX) + 1) {
            int y = MathHelper.floor_double(bb.minY);
            while (y < MathHelper.floor_double(bb.maxY) + 1) {
                int z = MathHelper.floor_double(bb.minZ);
                while (z < MathHelper.floor_double(bb.maxZ) + 1) {
                    AxisAlignedBB boundingBox;
                    Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null && !(block instanceof BlockAir) && (boundingBox = block.getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z)))) != null && bb.intersectsWith(boundingBox)) {
                        return true;
                    }
                    ++z;
                }
                ++y;
            }
            ++x;
        }
        return false;
    }

    private boolean insideBlock() {
        if (mc.thePlayer.ticksExisted < 5) return false;
        return this.insideBlock(mc.thePlayer.getEntityBoundingBox());
    }
}
