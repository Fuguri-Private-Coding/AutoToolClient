package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
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

    Glow shadows;
    Blur blur;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                if (tileEntity instanceof TileEntityChest) {
                    if (shadows.isToggled() && shadows.module.get("ChestESP")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));
                    if (blur.isToggled() && blur.module.get("ChestESP")) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));
                    RenderUtils.drawBlockESP(tileEntity.getPos(), color.getFadedFloatColor());
                    GlStateManager.resetColor();
                } else if (tileEntity instanceof TileEntityEnderChest && enderChest.isToggled()) {
                    if (shadows.isToggled() && shadows.module.get("ChestESP")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));
                    if (blur.isToggled() && blur.module.get("ChestESP")) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), Color.WHITE));
                    RenderUtils.drawBlockESP(tileEntity.getPos(), color.getFadedFloatColor());
                    GlStateManager.resetColor();
                }
            }
            RenderUtils.stop3D();
        }
    }
}
