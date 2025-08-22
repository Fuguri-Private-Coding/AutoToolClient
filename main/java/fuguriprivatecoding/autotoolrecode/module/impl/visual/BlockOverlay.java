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
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MovingObjectPosition;

import java.awt.*;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL, description = "Выделяет блок на который вы смотрите.")
public class BlockOverlay extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    Glow shadows;
    Blur blur;
    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) {
            return;
        }
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        if (event instanceof DrawBlockHighlightEvent) {

            fadeColor = color.isFade() ?
                    ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                    : color.getColor();


            MovingObjectPosition renderRayCast = mc.objectMouseOver;
            if (renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                RenderUtils.start3D();
                if (shadows.isToggled() && shadows.module.get("BlockOverlay")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), 1,1,1,1));
                if (blur.isToggled() && blur.module.get("BlockOverlay")) GaussianBlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), 1,1,1,1));
                RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), fadeColor.getRed() / 255f, fadeColor.getGreen() / 255f, fadeColor.getBlue() / 255f, fadeColor.getAlpha() / 255f);
                GlStateManager.resetColor();
                RenderUtils.stop3D();
            }
        }
    }
}
