package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.RayTrace;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL, description = "Выделяет блок на который вы смотрите.")
public class BlockOverlay extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    final CheckBox blur = new CheckBox("Blur", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof DrawBlockHighlightEvent && !Modules.getModule(Scaffold.class).isToggled()) {
            RayTrace hit = mc.objectMouseOver;

            if (hit.typeOfHit == RayTrace.RayType.BLOCK) {
                BlockPos pos = hit.getBlockPos();

                RenderUtils.start3D();
                if (glow.isToggled()) BloomUtils.addToDraw(() -> RenderUtils.drawBlockESP(pos, glowColor.getFadedFloatColor()));
                if (blur.isToggled()) BlurUtils.addToDraw(() -> RenderUtils.drawBlockESP(pos, 1,1,1,1));
                RenderUtils.drawBlockESP(pos, color.getFadedFloatColor());
                RenderUtils.stop3D();
            }
        }
    }
}
