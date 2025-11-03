package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "AutoPlace", category = Category.PLAYER, description = "FastPlace на максималках.")
public class AutoPlace extends Module {

    CheckBox needHoldRight = new CheckBox("HoldRight", this);

    @Override
    public void onEvent(Event event) {
        if (Modules.getModule(Scaffold.class).isToggled()) return;
        if (mc.currentScreen != null) return;
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock))
                return;

            if (mc.objectMouseOver == null
                    || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || mc.objectMouseOver.sideHit == EnumFacing.UP
                    || mc.objectMouseOver.sideHit == EnumFacing.DOWN) return;

            if (!needHoldRight.isToggled() || Mouse.isButtonDown(1)) {
                if (mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock().getMaterial() != Material.air) {
                    mc.rightClickMouse();
                }
            }
        }
    }
}
