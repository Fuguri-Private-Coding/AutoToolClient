package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL, description = "Выделяет блок на который вы смотрите.")
public class BlockOverlay extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);
    final CheckBox blur = new CheckBox("Blur", this);

    @Override
    public void onEvent(Event event) {
        if (Modules.getModule(Scaffold.class).isToggled()) return;
        if (event instanceof DrawBlockHighlightEvent) {
            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                RenderUtils.start3D();
                if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawBlockESP(mc.objectMouseOver.getBlockPos(), glowColor.getFadedFloatColor()));
                if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(mc.objectMouseOver.getBlockPos(), 1,1,1,1));
                RenderUtils.drawBlockESP(mc.objectMouseOver.getBlockPos(), color.getFadedFloatColor());
                GlStateManager.resetColor();
                RenderUtils.stop3D();
            }
        }
    }
}
