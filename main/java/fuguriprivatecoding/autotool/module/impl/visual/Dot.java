package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.Render3DEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.ColorSetting;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotool.utils.render.RenderUtils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;

@ModuleInfo(name = "Dot", category = Category.VISUAL)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0f, 1f, 0.5f, 0.05f) {};
    final CheckBox onlyKillAura = new CheckBox("OnlyKillAura", this, true);
    final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);

    Shadows shadows;
    Vec3 prevPos = Vec3.ZERO;
    Vec3 pos = Vec3.ZERO;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (Client.INST.getCombatManager().getTarget() == null && onlyKillAura.isToggled()) { return; }
        MovingObjectPosition mouse = mc.objectMouseOver;
        if (event instanceof Render3DEvent) {
            Vec3 smooth = prevPos.add(pos.subtract(prevPos).multiple(mc.timer.renderPartialTicks));

            if (shadows.isToggled() && shadows.module.get("Dot")) BloomUtils.addToDraw(() -> RenderUtils.drawDot(smooth, size.getValue(), Color.white));
            RenderUtils.drawDot(smooth, size.getValue(), color.getColor());
        }
        if (event instanceof TickEvent) {
            prevPos = pos;
            pos = mouse.hitVec;
        }
    }
}
