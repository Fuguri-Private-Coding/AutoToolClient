package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.DrawBlockHighlightEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSettings;
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

    private long lastTime = 0L;
    private int click = 0;
    private MovingObjectPosition movingObjectPosition = null;
    private BlockPos blockPos = null;

     FloatSettings frameDelay = new FloatSettings("FrameDelay", this, 0f, 10f, 0f, 1f);
     BooleanSetting needHoldRight = new BooleanSetting("HoldRight", this, true);

    @Override
    public void onEnable() {
        super.onEnable();
        if (needHoldRight.isToggled() && Mouse.isButtonDown(1) && !mc.thePlayer.capabilities.isFlying) {
            ItemStack i = mc.thePlayer.getHeldItem();
        }
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
                ItemStack i = mc.thePlayer.getHeldItem();
                if (i != null && i.getItem() instanceof ItemBlock) {
                    MovingObjectPosition mouse = mc.objectMouseOver;
                    if (mouse != null && mouse.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mouse.sideHit != EnumFacing.UP && mouse.sideHit != EnumFacing.DOWN) {
                        if (movingObjectPosition != null && (double) click < frameDelay.getValue()) {
                            ++click;
                        } else {
                            movingObjectPosition = mouse;
                            BlockPos pos = mouse.getBlockPos();
                            if (blockPos == null || pos.getX() != blockPos.getX() || pos.getY() != blockPos.getY() || pos.getZ() != blockPos.getZ()) {
                                Block b = mc.theWorld.getBlockState(pos).getBlock();
                                if (b != null && b != Blocks.air && !(b instanceof BlockLiquid)) {
                                    if (!needHoldRight.isToggled() || Mouse.isButtonDown(1)) {
                                        long time = System.currentTimeMillis();
                                        if (time - lastTime >= 25L) {
                                            lastTime = time;
                                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, i, pos, mouse.sideHit, mouse.hitVec)) {
                                                mc.rightClickMouse();
                                                mc.thePlayer.swingItem();
                                                mc.getItemRenderer().resetEquippedProgress();
                                                mc.rightClickMouse();
                                                blockPos = pos;
                                                click = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
