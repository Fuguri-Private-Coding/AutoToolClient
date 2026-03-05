package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.WorldChangeEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "BedESP", category = Category.VISUAL, description = "Показывает где находится кроовать.")
public class BedESP extends Module {

    public final IntegerSetting range = new IntegerSetting("Range", this, 2, 256, 64);
    public final IntegerSetting rate = new IntegerSetting("Rate", this, 1, 30, 5);
    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);
    final CheckBox blur = new CheckBox("Blur", this);

    public static final List<BlockPos[]> beds = new ArrayList<>();

    private volatile boolean running = false;
    private BedThread thread;

    @Override
    public void onEnable() {
        running = true;

        if (thread == null || !thread.isAlive()) {
            thread = new BedThread();
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void onDisable() {
        running = false;

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        synchronized (beds) {
            beds.clear();
        }
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof WorldChangeEvent && !beds.isEmpty()) beds.clear();
        if (event instanceof Render3DEvent) {
            if (beds.isEmpty()) return;

            RenderUtils.start3D();
            for (BlockPos[] bed : beds) {
                if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.renderBed(bed, glowColor.getFadedFloatColor()));
                if (blur.isToggled()) BlurUtils.addToDraw(() -> RenderUtils.renderBed(bed, Color.white));
                RenderUtils.renderBed(bed, color.getFadedFloatColor());
            }
            RenderUtils.stop3D();
        }
    }

    public boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    private class BedThread extends Thread {
        @Override
        public void run() {
            while (running && !isInterrupted()) {
                if (Utils.isWorldLoaded()) {
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

                                if (state.getBlock() == Blocks.bed &&
                                    state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {

                                    BlockPos headPos = pos.offset(state.getValue(BlockBed.FACING));
                                    foundBeds.add(new BlockPos[]{pos, headPos});
                                }
                            }
                        }
                    }

                    synchronized (beds) {

                        beds.removeIf(bedPair -> {
                            if (bedPair == null || bedPair[0] == null) return true;
                            return foundBeds.stream()
                                .noneMatch(found -> isSamePos(bedPair[0], found[0]));
                        });

                        for (BlockPos[] foundBed : foundBeds) {
                            if (beds.stream().noneMatch(bed -> isSamePos(bed[0], foundBed[0]))) {
                                beds.add(foundBed);
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(rate.getValue() * 1000L);
                } catch (InterruptedException e) {
                    interrupt();
                    return;
                }
            }
        }
    }
}
