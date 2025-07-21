package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "FindHoles", category = Category.PLAYER)
public class FindHoles extends Module {

    IntegerSetting range = new IntegerSetting("Range", this, 5, 100, 1);

    final CheckBox fadeBoxColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f, 1f, 1f, 1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeBoxColor::isToggled, 1f, 1f, 1f, 1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeBoxColor::isToggled, 0.1f, 20, 1, 0.1f);

    private transient final List<BlockPos> holes = new CopyOnWriteArrayList<>();
    private transient boolean finding;
    private transient long blocksChecked = 0;

    Color fadeColor;

    @Override
    public void onEnable() {
        findHoles();
    }

    @Override
    public void onDisable() {
        holes.clear();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            updateColors();
            RenderUtils.start3D();
            for (BlockPos hole : holes) {
                RenderUtils.drawBlockESP(hole, fadeColor.getRed() / 255f,fadeColor.getGreen() / 255f,fadeColor.getBlue() / 255f,fadeColor.getAlpha() / 255f);
            }
            RenderUtils.stop3D();
        }

        if (event instanceof Render2DEvent) {
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            String text = getProcessText();
            mc.fontRendererObj.drawString(text, scaledResolution.getScaledWidth() / 2f, scaledResolution.getScaledHeight() / 2f, -1, true);
        }
    }

    private void updateColors() {
        fadeColor = fadeBoxColor.isToggled() ?
                ColorUtils.fadeColor(color1.getColor(), color2.getColor(), fadeSpeed.getValue())
                : color1.getColor();
    }

    private void findHoles() {
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

    private String getProcessText() {
        double progress = (blocksChecked / Math.pow(range.getValue(), 3)) * 10D;
        String progressStr = String.format("%.2f", progress);

        String findingText = finding ? "(Finding holes) " : "";
        String progressText = finding ? "checked " + blocksChecked + "/" + Math.pow(range.getValue(), 3) + " (" + progressStr + "%) " : "";
        String foundHolesText = "found " + holes.size();

        return findingText + progressText + foundHolesText;
    }
}
