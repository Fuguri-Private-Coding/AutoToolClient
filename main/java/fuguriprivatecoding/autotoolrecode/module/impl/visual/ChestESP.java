package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
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
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                if (tileEntity instanceof TileEntityChest) {
                    if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), glowColor.getFadedFloatColor()));
                    if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));

                    RenderUtils.drawBlockESP(tileEntity.getPos(), color.getFadedFloatColor());
                    GlStateManager.resetColor();
                } else if (tileEntity instanceof TileEntityEnderChest && enderChest.isToggled()) {
                    if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), glowColor.getFadedFloatColor()));
                    if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));

                    RenderUtils.drawBlockESP(tileEntity.getPos(), color.getFadedFloatColor());
                    GlStateManager.resetColor();
                }
            }
            RenderUtils.stop3D();
        }
    }
}
