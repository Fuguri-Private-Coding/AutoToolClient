package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "BedESP", category = Category.VISUAL, description = "Показывает где находится кроовать.")
public class BedESP extends Module {

    final IntegerSetting range = new IntegerSetting("Range", this, 2, 256, 64);
    final IntegerSetting rate = new IntegerSetting("Rate", this, 1, 30, 5);
    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);
    final CheckBox blur = new CheckBox("Blur", this);

    private final List<BlockPos[]> beds = new ArrayList<>();
    private long lastCheck = 0;

    Thread update;

    @Override
    public void onDisable() {
        super.onDisable();
        if (!beds.isEmpty()) beds.clear();
        if (update.isAlive()) update.interrupt();
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof WorldChangeEvent && !beds.isEmpty()) beds.clear();
        if (event instanceof TickEvent) {
            if (System.currentTimeMillis() - lastCheck < rate.getValue() * 100L) return;
            lastCheck = System.currentTimeMillis();
            updateBedPos();
        }

        if (event instanceof Render3DEvent) {
            if (beds.isEmpty()) return;

            RenderUtils.start3D();
            for (BlockPos[] bed : beds) {
                if (glow.isToggled()) BloomRealUtils.addToDraw(() -> renderBed(bed, glowColor.getFadedFloatColor()));
                if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> renderBed(bed, Color.white));
                renderBed(bed, color.getFadedFloatColor());
            }
            RenderUtils.stop3D();
        }
    }

    public void updateBedPos() {
        update = new Thread(() -> {
            List<BlockPos[]> foundBeds = new ArrayList<>();

            int rangeValue = range.getValue();
            for (int y = rangeValue; y >= -rangeValue; --y) {
                for (int x = -rangeValue; x <= rangeValue; ++x) {
                    for (int z = -rangeValue; z <= rangeValue; ++z) {
                        BlockPos pos = new BlockPos(
                                mc.thePlayer.posX + x,
                                mc.thePlayer.posY + y,
                                mc.thePlayer.posZ + z
                        );

                        IBlockState state = mc.theWorld.getBlockState(pos);
                        if (state.getBlock() == Blocks.bed && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                            BlockPos headPos = pos.offset(state.getValue(BlockBed.FACING));
                            foundBeds.add(new BlockPos[]{pos, headPos});
                        }
                    }
                }
            }

            synchronized(beds) {
                beds.removeIf(bedPair -> {
                    if (bedPair == null || bedPair[0] == null) return true;
                    return foundBeds.stream().noneMatch(found -> isSamePos(bedPair[0], found[0]));
                });

                for (BlockPos[] foundBed : foundBeds) if (beds.stream().noneMatch(bed -> isSamePos(bed[0], foundBed[0]))) beds.add(foundBed);
            }
        });
        update.start();
    }

    public boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    private void renderBed(final BlockPos[] blockPos, Color color) {
        double posX = blockPos[0].getX() - mc.getRenderManager().viewerPosX;
        double posY = blockPos[0].getY() - mc.getRenderManager().viewerPosY;
        double posZ = blockPos[0].getZ() - mc.getRenderManager().viewerPosZ;
        GL11.glDepthMask(false);
        AxisAlignedBB axisAlignedBB;
        if (blockPos[0].getX() != blockPos[1].getX()) {
            if (blockPos[0].getX() > blockPos[1].getX()) {
                axisAlignedBB = new AxisAlignedBB(posX - 1.0, posY, posZ, posX + 1.0, posY + 0.5625F, posZ + 1.0);
            } else {
                axisAlignedBB = new AxisAlignedBB(posX, posY, posZ, posX + 2.0, posY + 0.5625F, posZ + 1.0);
            }
        } else if (blockPos[0].getZ() > blockPos[1].getZ()) {
            axisAlignedBB = new AxisAlignedBB(posX, posY, posZ - 1.0, posX + 1.0, posY + 0.5625F, posZ + 1.0);
        } else {
            axisAlignedBB = new AxisAlignedBB(posX, posY, posZ, posX + 1.0, posY + 0.5625F, posZ + 2.0);
        }
        RenderUtils.drawBoundingBox(axisAlignedBB, color);
        GlStateManager.resetColor();
    }
}
