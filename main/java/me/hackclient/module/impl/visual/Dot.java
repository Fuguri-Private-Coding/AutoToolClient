package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.CheckBox;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.utils.render.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
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
