package fuguriprivatecoding.autotoolrecode.module.impl.move;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "FastLadder", category = Category.MOVE, description = "Позволяет быстро подниматься по лестнице.")
public class FastLadder extends Module {

    final FloatSetting motion = new FloatSetting("Motion", this, 0, 5, 0.6f, 0.1f);
    final CheckBox polar = new CheckBox("Polar", this, false);
    final CheckBox stopBoostAtEnd = new CheckBox("Stop boost at end", this, polar::isToggled, true);


    @Override
    public void onEvent(Event event) {
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE && mc.thePlayer.isOnLadder() && mc.gameSettings.keyBindJump.isKeyDown()) {
            if (polar.isToggled()) {
                e.setOnGround(true);

                if (stopBoostAtEnd.isToggled()) {
                    BlockPos headPos = new BlockPos(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                        mc.thePlayer.posZ
                    );

                    IBlockState blockState = mc.theWorld.getBlockState(headPos);

                    if (!(blockState.getBlock() instanceof BlockLadder))
                        return;
                }
            }

            mc.thePlayer.motionY = motion.getValue();
        }
    }
}
