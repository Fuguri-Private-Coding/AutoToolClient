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
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;

import java.awt.*;

@ModuleInfo(name = "ChestESP", category = Category.VISUAL, description = "Показывает где находятся сундуки.")
public class ChestESP extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox enderChest = new CheckBox("ShowEnderChest", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);
    final CheckBox blur = new CheckBox("Blur", this);

    @Override
    public void onEvent(Event event) {
        if (!Utils.isWorldLoaded()) return;
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                switch (tileEntity) {
                    case TileEntityChest tileEntityChest -> {
                        if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntityChest.getPos(), glowColor.getFadedFloatColor()));
                        if (blur.isToggled()) BlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntityChest.getPos(), Color.WHITE));

                        RenderUtils.drawBlockESP(tileEntityChest.getPos(), color.getFadedFloatColor());
                        GlStateManager.resetColor();
                    }

                    case TileEntityEnderChest tileEntityEnderChest when enderChest.isToggled() -> {
                        if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntityEnderChest.getPos(), glowColor.getFadedFloatColor()));
                        if (blur.isToggled()) BlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntityEnderChest.getPos(), Color.WHITE));

                        RenderUtils.drawBlockESP(tileEntityEnderChest.getPos(), color.getFadedFloatColor());
                        GlStateManager.resetColor();
                    }
                    default -> {}
                }
            }
            RenderUtils.stop3D();
        }
    }
}
