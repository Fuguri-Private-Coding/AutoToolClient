package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.event.events.WorldChangeEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "BedESP",
        category = Category.VISUAL
)
public class BedESP extends Module {

    IntegerSetting range = new IntegerSetting("Range", this, 30, 256, 64);
    IntegerSetting rate = new IntegerSetting("Range", this, 10, 20, 20);

    MultiBooleanSetting modes = new MultiBooleanSetting("Modes", this)
            .add("ESP");

    ColorSetting color = new ColorSetting("Color",this, 0,0.5f,1f,1f);

    private long lastCheck = 0;

    List<BlockPos> beds = new ArrayList<>();

    Shadows shadows;

    @Override
    public void onDisable() {
        super.onDisable();
        beds.clear();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (event instanceof WorldChangeEvent) {
            beds.clear();
        }
        if (event instanceof TickEvent) {
            if (System.currentTimeMillis() - lastCheck < rate.getValue() * 1000L) return;
            lastCheck = System.currentTimeMillis();
            int i;
            beds.clear();
            for (int n = i = range.getValue(); i >= -n; --i) {
                for (int j = -n; j <= n; ++j) {
                    for (int k = -n; k <= n; ++k) {
                        final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                        final IBlockState getBlockState = mc.theWorld.getBlockState(blockPos);
                        if (getBlockState.getBlock() == Blocks.bed && getBlockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                            beds.add(blockPos);
                        }
                    }
                }
            }
        }

        if (event instanceof Render3DEvent) {
            if (modes.get("ESP")) {
                RenderUtils.start3D();
                for (BlockPos bed : beds) {
                    if (shadows.isToggled() && shadows.bedEsp.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(bed, color.getRed(), color.getGreen(), color.getBlue(), 1f, 0f, 1.0f));
                    RenderUtils.drawBlockESP(bed, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 0f, 1.0f);
                }
                RenderUtils.stop3D();
            }
        }
    }
}
