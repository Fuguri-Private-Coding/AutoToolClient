package fuguriprivatecoding.autotoolrecode.module.impl.misc;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.player.MotionEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.player.move.MoveUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

@ModuleInfo(name = "Fixes", category = Category.MISC, description = "Убирает задержки механики майнкрафта.")
public class Fixes extends Module {

    public MultiMode fixes = new MultiMode("Fixes", this)
        .add("ClickDelay", true)
        .add("SaveMoveKeys", true)
        .add("JumpDelay", true)
        .add("FastWorldLoading", true)
        .add("1.17-ItemUse")
        ;

    IntegerSetting jumpChance = new IntegerSetting("JumpChance", this, 0, 100, 100);

    boolean wasInGui = false;

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null && mc.theWorld == null) return;
        if (event instanceof MotionEvent e && e.getType() == MotionEvent.Type.PRE) {
            if (fixes.get("ClickDelay")) mc.leftClickCounter = -1;
            if (fixes.get("SaveMoveKeys")) {
                if (wasInGui && mc.currentScreen == null) MoveUtils.updateControls();
                wasInGui = mc.currentScreen == null;
            }

            if (fixes.get("JumpDelay") && Math.random() <= jumpChance.getValue() / 100f) {
                mc.thePlayer.jumpTicks = 0;
            }
        }

        if (fixes.get("1.17-ItemUse") && event instanceof PacketEvent e && e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            e.cancel();

            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.rotationYaw,
                mc.thePlayer.rotationPitch,
                mc.thePlayer.onGround
            ));

            mc.getNetHandler().getNetworkManager().sendPacketNoEvent(e.getPacket());
        }
    }
}
