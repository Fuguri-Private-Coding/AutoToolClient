package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;

import java.awt.*;

@ModuleInfo(name = "ChestESP", category = Category.VISUAL, description = "Показывает где находятся сундуки.")
public class ChestESP extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox enderChest = new CheckBox("ShowEnderChest", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    @Override
    public void onEvent(Event event) {
        if (!Utils.isWorldLoaded()) return;
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                switch (tileEntity) {
                    case TileEntityChest e -> draw(e.getPos());
                    case TileEntityEnderChest e when enderChest.isToggled() -> draw(e.getPos());
                    default -> {}
                }
            }
            RenderUtils.stop3D();
        }
    }

    private void draw(BlockPos pos) {
        if (glow.isToggled()) {
            BloomUtils.startWrite();
            RenderUtils.drawBlockESP(pos, glowColor.getFadedFloatColor());
            BloomUtils.stopWrite();
        }

        RenderUtils.drawBlockESP(pos, color.getFadedFloatColor());
    }
}
