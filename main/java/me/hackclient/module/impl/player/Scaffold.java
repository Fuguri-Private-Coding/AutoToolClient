package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.DrawBlockHighlightEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

@ModuleInfo(
        name = "Scaffold",
        category = Category.PLAYER
)
public class Scaffold extends Module {

    long lastTime = 0L;
    BlockPos blockPos = null;

    final Rotation[] bestGodbrigdeRotations = new Rotation[] {
            new Rotation(-45.0f, 77.0f),
            new Rotation(135.0f, 77.0f),
            new Rotation(-135.0f, 77.0f),
            new Rotation(45.0f, 77.0f)
    };

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
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
                    }
                }
            }
        }
//        Rotation nearest = Arrays.stream(bestGodbrigdeRotations).sorted(rotation ->).findFirst();
    }
}
