package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Mouse;

@ModuleInfo(name = "Phase", category = Category.PLAYER, description = "Позволяет ходить через стены.")
public class Phase extends Module {

    CheckBox sneak = new CheckBox("Sneak", this, true);

    CheckBox intaveClip = new CheckBox("IntaveClip", this, true);
    IntegerSetting packetCount = new IntegerSetting("PacketCount", this, () -> intaveClip.isToggled(), 1, 20, 1);

    CheckBox fasterXZ = new CheckBox("FasterXZ", this, false);
    CheckBox moveUpDown = new CheckBox("MoveUpDown", this);
    IntegerSetting yMoveSpeed = new IntegerSetting("MoveUpDownSpeed", this, () -> moveUpDown.isToggled(), 0, 100, 6);

    private BlockPos currentBreakingBlock;
    private long lastBreakTime;
    private WorldSettings.GameType lastGameType;

    public void onEnable() {
        if (mc.thePlayer != null) {
            lastGameType = mc.playerController.getCurrentGameType();
            setGameMode(WorldSettings.GameType.SURVIVAL);
        }
    }

    public void onDisable() {
        resetBreaking();
        setGameMode(lastGameType);
        lastGameType = null;
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
        if (event instanceof UpdateEvent) {
            double ySpeed = yMoveSpeed.getValue() / 100.0;
            if (mc.thePlayer.noClip) {

                if (moveUpDown.isToggled()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.thePlayer.motionY = ySpeed;
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.thePlayer.motionY = -ySpeed;
                    } else {
                        mc.thePlayer.motionY = 0.0;
                    }
                }

                if (fasterXZ.isToggled() && !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()).isEmpty()) {
                    double forward = mc.thePlayer.movementInput.moveForward;
                    double strafe = mc.thePlayer.movementInput.moveStrafe;
                    float yaw = mc.thePlayer.rotationYaw;
                    if (forward != 0.0 || strafe != 0.0) {
                        double mx = -Math.sin(Math.toRadians(yaw)) * forward + Math.cos(Math.toRadians(yaw)) * strafe;
                        double mz = Math.cos(Math.toRadians(yaw)) * forward + Math.sin(Math.toRadians(yaw)) * strafe;
                        double motion = Math.sqrt(mx * mx + mz * mz);
                        if (motion > 0) {
                            mx = (mx / motion) * 0.7 * 0.2873;
                            mz = (mz / motion) * 0.7 * 0.2873;
                            mc.thePlayer.motionX = mx;
                            mc.thePlayer.motionZ = mz;
                        } else {
                            mc.thePlayer.motionX = 0.0;
                            mc.thePlayer.motionZ = 0.0;
                        }
                    } else {
                        mc.thePlayer.motionX = 0.0;
                        mc.thePlayer.motionZ = 0.0;
                    }
                }
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
            BlockPos newBreakingBlock = mc.objectMouseOver.getBlockPos();
            if (newBreakingBlock == null || !Mouse.isButtonDown(0) || !intaveClip.isToggled() || newBreakingBlock.getY() >= mc.thePlayer.posY) return;
            if (currentBreakingBlock != null && System.currentTimeMillis() - lastBreakTime > 500L || !newBreakingBlock.equals(currentBreakingBlock)) resetBreaking();

            currentBreakingBlock = newBreakingBlock;
            lastBreakTime = System.currentTimeMillis();

            EnumFacing sideHit = mc.objectMouseOver.sideHit;
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentBreakingBlock, sideHit));
            for (int i = 0; i < packetCount.getValue(); ++i) mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

            if (mc.playerController.curBlockDamageMP > 0.77f) {
                mc.playerController.curBlockDamageMP = 1.0f;
            }
        }
    }
}