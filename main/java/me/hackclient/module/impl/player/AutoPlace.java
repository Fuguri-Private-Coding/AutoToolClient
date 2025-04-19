package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.DrawBlockHighlightEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoPlace", category = Category.PLAYER, toggled = true)
public class AutoPlace extends Module {

    BooleanSetting needHoldRight = new BooleanSetting("HoldRight", this, true);
    BooleanSetting swingItem = new BooleanSetting("PlayerSwingItem", this, true);

    long lastTime = 0L;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.currentScreen != null) return;

            ItemStack stack = mc.thePlayer.getHeldItem();

            if (stack == null) return;

            if (!(stack.getItem() instanceof ItemBlock)) return;

            MovingObjectPosition mouse = mc.objectMouseOver;

            if (mouse == null
                    || mouse.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || mouse.sideHit == EnumFacing.UP
                    || mouse.sideHit == EnumFacing.DOWN) return;

            
            BlockPos pos = mouse.getBlockPos();
            if (!needHoldRight.isToggled() || Mouse.isButtonDown(1)) {
                if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() != Material.air) {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, pos, mouse.sideHit, mouse.hitVec) && System.currentTimeMillis() - lastTime >= 25) {
                        mc.rightClickMouse();
                        if (swingItem.isToggled()) {
                            mc.thePlayer.swingItem();

                        }
                    }
                }
            }
        }
    }
}
