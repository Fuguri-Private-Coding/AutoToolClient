package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "Phase", category = Category.PLAYER)
public class Phase extends Module {

    CheckBox sneak = new CheckBox("Sneak", this, true);
    CheckBox clipDown = new CheckBox("Clip", this, true);
    IntegerSetting packetCount = new IntegerSetting("Packet count", this, () -> clipDown.isToggled(), 1, 20, 1);

    CheckBox findHoles = new CheckBox("FindHoles", this, false);

    BooleanSupplier visible = () -> findHoles.isToggled();

    IntegerSetting range = new IntegerSetting("Range", this, visible, 5, 100, 1);
    CheckBox includeUp = new CheckBox("IncludeUp", this, true);

    private transient final List<BlockPos> holes = new CopyOnWriteArrayList<>();
    private transient boolean finding;
    private transient long blocksChecked = 0;

    private BlockPos currentBreakingBlock;
    private long lastBreakTime;
    private WorldSettings.GameType lastGameType;

    public void onEnable() {
        if (mc.thePlayer != null) {
            lastGameType = mc.theWorld.getWorldInfo().getGameType();
            setGameMode(WorldSettings.GameType.SURVIVAL);
            findHoles();
        }
    }

    public void onDisable() {
        resetBreaking();
        setGameMode(lastGameType);
        lastGameType = null;
        holes.clear();
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
            if (!Mouse.isButtonDown(0)) return;
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

        if (findHoles.isToggled()) {
            if (event instanceof Render2DEvent) {
                ScaledResolution scaledResolution = new ScaledResolution(mc);

                double progress = ((double) blocksChecked / Math.pow(range.getValue(), 3)) * 10d;
                String progressStr = String.format("%.2f", progress);

                String findingText = finding ? "(Finding holes) " : "";
                String progressText = finding ? "checked " + blocksChecked + "/" + (long) Math.pow(range.getValue(), 3) + " (" + progressStr + "%) " : "";
                String foundHolesText = "found " + holes.size();

                String text = findingText + progressText + foundHolesText;

                mc.fontRendererObj.drawString(text, scaledResolution.getScaledWidth() / 2f, scaledResolution.getScaledHeight() / 2f, -1, true);
            }

            if (event instanceof Render3DEvent) {
                RenderUtils.start3D();
                for (BlockPos hole : holes) {
                    RenderUtils.drawBlockESP(hole, 0, 0.9f, 0, 0.15f);
                }
                RenderUtils.stop3D();
            }
        }
    }

    private void findHoles() {
        if (!findHoles.isToggled()) {
            return;
        }

        new Thread(() -> {
            finding = true;
            for (int y = range.getValue(); y >= -range.getValue(); --y) {
                for (int x = -range.getValue(); x <= range.getValue(); ++x) {
                    for (int z = -range.getValue(); z <= range.getValue(); ++z) {
                        BlockPos pos = new BlockPos(
                                mc.thePlayer.posX + x,
                                mc.thePlayer.posY + y,
                                mc.thePlayer.posZ + z
                        );

                        IBlockState state = mc.theWorld.getBlockState(pos);
                        if (state.getBlock() == Blocks.air || state.getBlock() == Blocks.glass || state.getBlock() == Blocks.stained_glass) {
                            IBlockState state1 = mc.theWorld.getBlockState(pos.add(0, -1, 0));
                            IBlockState state7 = mc.theWorld.getBlockState(pos.add(0, -2, 0));
                            IBlockState state2 = mc.theWorld.getBlockState(pos.add(-1, 0, 0));
                            IBlockState state3 = mc.theWorld.getBlockState(pos.add(1, 0, 0));
                            IBlockState state4 = mc.theWorld.getBlockState(pos.add(0, 0, -1));
                            IBlockState state5 = mc.theWorld.getBlockState(pos.add(0, 0, 1));
                            IBlockState state6 = mc.theWorld.getBlockState(pos.add(0, 1, 0));

                            if (state1.getBlock().getMaterial().isSolid() && state1.getBlock() != Blocks.web
                                    && state2.getBlock().getMaterial().isSolid() && state2.getBlock() != Blocks.web
                                    && state3.getBlock().getMaterial().isSolid() && state3.getBlock() != Blocks.web
                                    && state4.getBlock().getMaterial().isSolid() && state4.getBlock() != Blocks.web
                                    && state5.getBlock().getMaterial().isSolid() && state5.getBlock() != Blocks.web
                                    && (state6.getBlock().getMaterial().isSolid() || state.getBlock() != Blocks.air)
                                    && state7.getBlock().getMaterial().isSolid()) {
                                if (!holes.contains(pos)) {
                                    holes.add(pos);
                                }
                            }
                        }
                        blocksChecked++;
                    }
                }
            }
            finding = false;
            blocksChecked = 0;
        }).start();
    }
}