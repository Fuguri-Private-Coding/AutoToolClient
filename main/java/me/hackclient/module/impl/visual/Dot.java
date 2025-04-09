package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(
        name = "Dot",
        category = Category.VISUAL,
        toggled = true
)
public class Dot extends Module {

    final FloatSetting size = new FloatSetting("Size", this, 1.0f, 20.0f, 10.0f, 0.1f) {};
    final BooleanSetting onlyKillAura = new BooleanSetting("OnlyKillAura", this, true);

    final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);

    Vec3 lastVec, currentVec;

    Shadows shadows;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (event instanceof TickEvent) {
            MovingObjectPosition mouse = RayCastUtils.rayCast(6, 3, Rotation.getServerRotation());

            if (currentVec != null) {
                lastVec = new Vec3(currentVec);
            }

            if (mouse != null) {
                currentVec = mouse.hitVec;
            }
        }
        if (event instanceof Render3DEvent) {
            if (Client.INSTANCE.getCombatManager().getTarget() == null && onlyKillAura.isToggled() || lastVec == null || currentVec == null) { return; }

            double x = lastVec.xCoord + (currentVec.xCoord - lastVec.xCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = lastVec.yCoord + (currentVec.yCoord - lastVec.yCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = lastVec.zCoord + (currentVec.zCoord - lastVec.zCoord) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

            RenderUtils.start3D();
            if (shadows.isToggled() && shadows.dot.isToggled()) {
                BloomUtils.addToDraw(() -> {
                    RenderUtils.glColor(Color.WHITE);

                    GL11.glPointSize(size.getValue());
                    GL11.glEnable(GL11.GL_POINT_SMOOTH);

                    GL11.glBegin(GL11.GL_POINTS);
                    GL11.glVertex3d(x, y, z);
                    GL11.glEnd();

                    GL11.glDisable(GL11.GL_POINT_SMOOTH);
                    GL11.glPointSize(1);
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                });
            }
            RenderUtils.glColor(color.getColor());

            GL11.glPointSize(size.getValue());
            GL11.glEnable(GL11.GL_POINT_SMOOTH);

            GL11.glBegin(GL11.GL_POINTS);
            GL11.glVertex3d(x, y, z);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_POINT_SMOOTH);
            GL11.glPointSize(1);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            RenderUtils.stop3D();
        }
    }
}
