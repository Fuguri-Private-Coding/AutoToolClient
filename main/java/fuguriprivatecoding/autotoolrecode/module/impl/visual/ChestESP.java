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
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;

import java.awt.*;

@ModuleInfo(name = "ChestESP", category = Category.VISUAL, description = "Показывает где находятся сундуки.")
public class ChestESP extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox enderChest = new CheckBox("ShowEnderChest", this);

    Color fadeColor;
    Glow shadows;
    Blur blur;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event instanceof Render3DEvent) {
            fadeColor = color.isFade() ?
                    ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                    : color.getColor();

            RenderUtils.start3D();
            for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                if (tileEntity instanceof TileEntityChest) {
                    if (shadows.isToggled() && shadows.module.get("ChestESP")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), 1,1,1,1));
                    if (blur.isToggled() && blur.module.get("ChestESP")) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), 1,1,1,1));
                    RenderUtils.drawBlockESP(tileEntity.getPos(), fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
                    ColorUtils.resetColor();
                } else if (tileEntity instanceof TileEntityEnderChest && enderChest.isToggled()) {
                    if (shadows.isToggled() && shadows.module.get("ChestESP")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), 1,1,1,1));
                    if (blur.isToggled() && blur.module.get("ChestESP")) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(tileEntity.getPos(), 1,1,1,1));
                    RenderUtils.drawBlockESP(tileEntity.getPos(), fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
                    ColorUtils.resetColor();
                }
            }
            RenderUtils.stop3D();
        }
    }
}
