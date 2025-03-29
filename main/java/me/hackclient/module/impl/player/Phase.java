package me.hackclient.module.impl.player;

import me.hackclient.event.Event;
import me.hackclient.event.events.*;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;

@ModuleInfo(
        name = "Phase",
        category = Category.PLAYER
)
public class Phase extends Module {

    BooleanSetting sneak = new BooleanSetting("Sneak", this, true);
    BooleanSetting clipDown = new BooleanSetting("Clip", this, true);
    IntegerSetting packetCount = new IntegerSetting("Packet count", this, () -> clipDown.isToggled(), 1, 20, 1);

    private BlockPos currentBreakingBlock;
    private long lastBreakTime;
    private WorldSettings.GameType lastGameType;
    public boolean clip;

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
        clip = false;
    }

    private void resetBreaking() {
        currentBreakingBlock = null;
    }

    private void setGameMode(WorldSettings.GameType gameType) {
        if (mc.thePlayer != null && mc.playerController != null) {
            mc.playerController.setGameType(gameType);
        }
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof UpdateEvent && !clip) {
            if (mc.thePlayer.noClip) {
                mc.thePlayer.motionY = 0.0;
            } else {
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.onGround = true;
            }
        }

        if (event instanceof MoveButtonEvent moveButtonEvent) {
            if (mc.thePlayer.noClip) {
                moveButtonEvent.setJump(false);
            }
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()).isEmpty() && sneak.isToggled()) {
                moveButtonEvent.setSneak(true);
            }
        }

        if (event instanceof TickEvent) {
            EntityPlayerSP player = mc.thePlayer;
            PlayerControllerMP controller = mc.playerController;
            BlockPos newBreakingBlock = mc.objectMouseOver.getBlockPos();
            if (newBreakingBlock == null) return;

            if (currentBreakingBlock != null && System.currentTimeMillis() - lastBreakTime > 500L) resetBreaking();

            if (!mc.thePlayer.isSwingInProgress) return;

            if (!clipDown.isToggled()) return;

            if (newBreakingBlock.getY() >= player.posY) return;

            if (!newBreakingBlock.equals(currentBreakingBlock)) resetBreaking();

            currentBreakingBlock = newBreakingBlock;
            lastBreakTime = System.currentTimeMillis();
            EnumFacing sideHit = mc.objectMouseOver.sideHit;
            player.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentBreakingBlock, sideHit));
            for (int i = 0; i < packetCount.getValue(); i++) {
                player.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY, player.posZ, true));
            }
            if (controller.curBlockDamageMP > 0.77F) {
                controller.curBlockDamageMP = 1.0f;
            }
        }
    }
}