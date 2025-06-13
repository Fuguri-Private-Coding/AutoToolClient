package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(name = "BedESP", category = Category.VISUAL)
public class BedESP extends Module {

    final IntegerSetting range = new IntegerSetting("Range", this, 2, 256, 64);
    final IntegerSetting rate = new IntegerSetting("Rate", this, 1,30, 5);

    final CheckBox fadeColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeColor::isToggled, 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeColor::isToggled,0.1f, 20, 1, 0.1f);

    Shadows shadows;

    private final List<BlockPos[]> beds = new ArrayList<>();
    private long lastCheck = 0;

    @Override
    public void onDisable() {
        super.onDisable();
        if (!beds.isEmpty()) beds.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof WorldChangeEvent && !beds.isEmpty()) beds.clear();
        if (event instanceof TickEvent) {
            if (System.currentTimeMillis() - lastCheck < rate.getValue() * 100L) return;
            lastCheck = System.currentTimeMillis();
            updateBedPos();
        }

        if (event instanceof Render3DEvent) {
            if (beds.isEmpty()) return;
            Color fadeColor;

            if (this.fadeColor.isToggled()) {
                fadeColor = ColorUtils.mixColors(color1.getColor(), color2.getColor(), fadeSpeed.getValue());
            } else {
                fadeColor = color1.getColor();
            }

            RenderUtils.start3D();
            for (BlockPos[] bed : beds) {
                if (shadows.isToggled() && shadows.module.get("BedESP")) BloomUtils.addToDraw(() -> renderBed(bed, Color.white));
                renderBed(bed, fadeColor);
            }
            RenderUtils.stop3D();
        }
    }

    public void updateBedPos() {
        new Thread(() -> {
            if (!beds.isEmpty()) beds.clear();
            int i;
            for (int n = i = range.getValue(); i >= -n; --i) {
                for (int j = -n; j <= n; ++j) {
                    for (int k = -n; k <= n; ++k) {
                        BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                        IBlockState blockState = mc.theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.bed && blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                            for (BlockPos[] bed : beds) if (Arrays.equals(bed, new BlockPos[]{blockPos, blockPos.offset(blockState.getValue(BlockBed.FACING))})) return;
                            this.beds.add(new BlockPos[]{blockPos, blockPos.offset(blockState.getValue(BlockBed.FACING))});
                        }
                    }
                }
            }
        }).start();
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
        ColorUtils.resetColor();
    }
}
