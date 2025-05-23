package fuguriprivatecoding.autotool.module.impl.player;

import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.MoveButtonEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.event.events.UpdateEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.IntegerSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "Phase", category = Category.PLAYER)
public class Phase extends Module {

    CheckBox sneak = new CheckBox("Sneak", this, true);
    CheckBox clipDown = new CheckBox("Clip", this, true);
    IntegerSetting packetCount = new IntegerSetting("Packet count", this, () -> clipDown.isToggled(), 1, 20, 1);

    private BlockPos currentBreakingBlock;
    private long lastBreakTime;
    private WorldSettings.GameType lastGameType;
    public boolean cliped;

    public void onEnable() {
        if (mc.thePlayer != null) {
            lastGameType = mc.theWorld.getWorldInfo().getGameType();
            setGameMode(WorldSettings.GameType.SURVIVAL);
        }
    }

    public void onDisable() {
        resetBreaking();
        setGameMode(lastGameType);
        lastGameType = null;
        cliped = false;
    }

    private void resetBreaking() {
        currentBreakingBlock = null;
    }

    private void setGameMode(WorldSettings.GameType gameType) {
        if (mc.thePlayer != null && mc.playerController != null && gameType != null) {
            mc.playerController.setGameType(gameType);
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof UpdateEvent && !cliped) {
            if (mc.thePlayer.noClip) {
                mc.thePlayer.motionY = 0.0;
            } else {
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.onGround = true;
            }
        }

        if (event instanceof MoveButtonEvent moveButtonEvent) {
            if (mc.thePlayer.noClip) moveButtonEvent.setJump(false);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()).isEmpty() && sneak.isToggled()) {
                moveButtonEvent.setSneak(true);
            }
        }

        if (event instanceof TickEvent && mc.objectMouseOver != null) {
            EntityPlayerSP player = mc.thePlayer;
            PlayerControllerMP controller = mc.playerController;
            BlockPos newBreakingBlock = mc.objectMouseOver.getBlockPos();

            if (newBreakingBlock == null) return;
            if (currentBreakingBlock != null && System.currentTimeMillis() - lastBreakTime > 500L) resetBreaking();
            if (!newBreakingBlock.equals(player.getPosition().down()) && (!Mouse.isButtonDown(0) || controller.curBlockDamageMP == 0)) return;
            if (!clipDown.isToggled()) return;
            if (newBreakingBlock.getY() >= player.posY) return;
            if (!newBreakingBlock.equals(currentBreakingBlock)) resetBreaking();

            currentBreakingBlock = newBreakingBlock;
            lastBreakTime = System.currentTimeMillis();
            EnumFacing sideHit = mc.objectMouseOver.sideHit;

            player.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentBreakingBlock, sideHit));
            for (int i = 0; i < packetCount.getValue(); i++) player.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
            if (controller.curBlockDamageMP > 0.77F) controller.curBlockDamageMP = 1.0f;
        }
    }
}