package fuguriprivatecoding.autotool.module.impl.visual;

import fuguriprivatecoding.autotool.Client;
import fuguriprivatecoding.autotool.event.Event;
import fuguriprivatecoding.autotool.event.EventTarget;
import fuguriprivatecoding.autotool.event.events.Render3DEvent;
import fuguriprivatecoding.autotool.event.events.TickEvent;
import fuguriprivatecoding.autotool.module.Category;
import fuguriprivatecoding.autotool.module.Module;
import fuguriprivatecoding.autotool.module.ModuleInfo;
import fuguriprivatecoding.autotool.module.impl.combat.KillAura;
import fuguriprivatecoding.autotool.settings.impl.CheckBox;
import fuguriprivatecoding.autotool.settings.impl.ColorSetting;
import fuguriprivatecoding.autotool.settings.impl.FloatSetting;
import fuguriprivatecoding.autotool.utils.color.ColorUtils;
import fuguriprivatecoding.autotool.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotool.utils.render.RenderUtils;
import fuguriprivatecoding.autotool.utils.rotation.Rot;
import fuguriprivatecoding.autotool.utils.rotation.RotUtils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;

@ModuleInfo(name = "Dot", category = Category.VISUAL)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 0f, 1f, 0.5f, 0.05f) {};
    final CheckBox onlyChangeRotationModules = new CheckBox("OnlyChangeRotationModules", this, true);

    final CheckBox fadeColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeColor::isToggled, 1f,1f,1f,1f);
    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeColor::isToggled,0.1f, 20, 1, 0.1f);

    Shadows shadows;
    Vec3 prevPos = Vec3.ZERO;
    Vec3 pos = Vec3.ZERO;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (!Rot.isChanged() && onlyChangeRotationModules.isToggled()) { return; }
        MovingObjectPosition mouse = mc.objectMouseOver;
        if (event instanceof Render3DEvent) {
            Vec3 smooth = prevPos.add(pos.subtract(prevPos).multiple(mc.timer.renderPartialTicks));

            Color fadeColor;

            if (this.fadeColor.isToggled()) {
                fadeColor = ColorUtils.mixColors(color1.getColor(), color2.getColor(), (Math.sin(System.currentTimeMillis() / 1000D * (double) fadeSpeed.getValue()) + 1) / 2);
            } else {
                fadeColor = color1.getColor();
            }

            if (shadows.isToggled() && shadows.module.get("Dot")) BloomUtils.addToDraw(() -> RenderUtils.drawDot(smooth, size.getValue(), Color.white));
            RenderUtils.drawDot(smooth, size.getValue(), fadeColor);
        }
        if (event instanceof TickEvent) {
            prevPos = pos;
            pos = mouse.hitVec;
        }
    }
}
