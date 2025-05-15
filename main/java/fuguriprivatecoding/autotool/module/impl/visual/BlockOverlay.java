package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.module.impl.player.Scaffold;
import fuguriprivatecoding.autotool.settings.impl.ColorSetting;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotool.utils.render.RenderUtils;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL)
public class BlockOverlay extends Module {

    ColorSetting color = new ColorSetting("Color", this, 0, 0.5f, 1f, 0.3f);
    FloatSetting lineAlpha = new FloatSetting("LineAlpha",this, 0, 1, 1, 0.1f);
    FloatSetting lineWidth = new FloatSetting("LineWidth",this, 0, 5, 1, 0.1f);
    Shadows shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) {
            return;
        }
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof DrawBlockHighlightEvent) {
            MovingObjectPosition renderRayCast = mc.objectMouseOver;
            if (renderRayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                RenderUtils.start3D();
                if (shadows.isToggled() && shadows.module.get("BlockOverlay")) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), 1,1,1,1, 0f, lineWidth.getValue()));
                RenderUtils.drawBlockESP(renderRayCast.getBlockPos(), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), lineAlpha.getValue(), lineWidth.getValue());
                RenderUtils.stop3D();
            }
        }
    }
}
