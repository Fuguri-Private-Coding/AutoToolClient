package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.DrawBlockHighlightEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Fucker;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.RayTrace;

import java.awt.*;

@ModuleInfo(name = "BlockOverlay", category = Category.VISUAL, description = "Выделяет блок на который вы смотрите.")
public class BlockOverlay extends Module {

    final ColorSetting color = new ColorSetting("Color", this);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    @Override
    public void onEvent(Event event) {
        if (event instanceof DrawBlockHighlightEvent e && !Modules.getModule(Scaffold.class).isToggled() && Modules.getModule(Fucker.class).block == null) {
            e.cancel();
            if (mc.rayTrace.typeOfHit == RayTrace.RayType.BLOCK) {
                BlockPos pos = mc.rayTrace.getBlockPos();

                RenderUtils.start3D();
                if (glow.isToggled()) {
                    BloomUtils.startWrite();
                    RenderUtils.drawBlockESP(pos, glowColor.getFadedFloatColor());
                    BloomUtils.stopWrite();
                }
                RenderUtils.drawBlockESP(pos, color.getFadedFloatColor());
                RenderUtils.stop3D();
            }
        }
    }
}
