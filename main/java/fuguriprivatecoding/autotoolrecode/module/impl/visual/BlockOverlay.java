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

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL)
public class BlockOverlay extends Module {

    final CheckBox fadeBoxColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeBoxColor::isToggled, 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeBoxColor::isToggled,0.1f, 20, 1, 0.1f);

    Shadows shadows;
    Blur blur;
    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) {
            return;
        }
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (blur == null) blur = Client.INST.getModuleManager().getModule(Blur.class);
        if (event instanceof DrawBlockHighlightEvent) {

            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.fadeColor(color1.getColor(), color2.getColor(), fadeSpeed.getValue())
                    : color1.getColor();


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
