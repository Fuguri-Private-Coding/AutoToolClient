package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL, description = "Выделяет блок на который вы смотрите.")
public class BlockOverlay extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);
    final CheckBox blur = new CheckBox("Blur", this);

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
        if (event instanceof DrawBlockHighlightEvent) {
            MovingObjectPosition renderRayCast = mc.objectMouseOver;
            if (renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                RenderUtils.start3D();
                if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), glowColor.getFadedFloatColor()));
                if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), 1,1,1,1));
                RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), color.getFadedFloatColor());
                GlStateManager.resetColor();
                RenderUtils.stop3D();
            }
        }
    }
}
