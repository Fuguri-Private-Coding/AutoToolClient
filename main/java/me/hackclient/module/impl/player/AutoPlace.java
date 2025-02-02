package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.DrawBlockHighlightEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoPlace", category = Category.PLAYER, toggled = true)
public class AutoPlace extends Module {

    long lastTime = 0L;
    int delay = 0;
    BlockPos blockPos = null;

    FloatSetting frameDelay = new FloatSetting("FrameDelay", this, 0f, 10f, 0f, 1f);
    BooleanSetting needHoldRight = new BooleanSetting("HoldRight", this, true);
    BooleanSetting swingItem = new BooleanSetting("PlayerSwingItem", this, true);

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

            if (delay < frameDelay.getValue()) {
                ++delay;
                return;
            }

            BlockPos pos = mouse.getBlockPos();
            if (blockPos == null || pos.getX() != blockPos.getX() || pos.getY() != blockPos.getY() || pos.getZ() != blockPos.getZ()) {
                Block b = mc.theWorld.getBlockState(pos).getBlock();
                if (b != null && b != Blocks.air && !(b instanceof BlockLiquid)) {
                    if (!needHoldRight.isToggled() || Mouse.isButtonDown(1)) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25)  {
                            mc.rightClickMouse();
                            if (swingItem.isToggled()) {
                                mc.thePlayer.swingItem();
                                mc.getItemRenderer().resetEquippedProgress();
                                mc.rightClickMouse();
                            }
                            blockPos = pos;
                            lastTime = System.currentTimeMillis();
                            delay = 0;
                        }
                    }
                }
            }
        }
    }
}
