package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.combat.KillAuraModule;
import me.hackclient.settings.impl.FloatSettings;
import me.hackclient.settings.impl.IntegerSetting;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetESP", category = Category.VISUAL, toggled = true)
public class TargetESPModule extends Module {

    FloatSettings speed = new FloatSettings("Speed", this, 1f, 5f, 2f, 0.1f);
    IntegerSetting quality = new IntegerSetting("Quality", this, 1, 360, 60);
    FloatSettings length = new FloatSettings("Length", this, 0.2f, 1.5f, 0.3f, 0.1f);

    private KillAuraModule killAura;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (killAura == null)
            killAura = Client.INSTANCE.getModuleManager().getModule(KillAuraModule.class);

        if (killAura.getTarget() == null)
            return;

        if (event instanceof Render3DEvent) {
            double animationTranslate = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());

            final EntityLivingBase target = killAura.getTarget();

            final RenderManager renderManager = mc.getRenderManager();
            final double viewerPosX = renderManager.viewerPosX;
            final double viewerPosY = renderManager.viewerPosY;
            final double viewerPosZ = renderManager.viewerPosZ;

            double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - viewerPosX;
            double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - viewerPosY;
            double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - viewerPosZ;

            glPushMatrix();
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);
            glShadeModel(7425);
            mc.entityRenderer.disableLightmap();
            glBegin(GL_QUAD_STRIP);
            for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
                double x1 = x + sin(i * Math.PI / 180) * 0.7;
                double z1 = z + cos(i * Math.PI / 180) * 0.7;
                double y1 = y + (animationTranslate + 1) / 2 * target.height;
                glColor4f(1f, 1f, 1f, 1f);
                glVertex3d(x1, y1, z1);
                glColor4f(1f, 1f, 1f, 0f);
                glVertex3d(x1, y1 + animationTranslate * length.getValue(), z1);
            }
            glEnd();
            glEnable(GL_CULL_FACE);
            glShadeModel(7424);
            glColor4f(1f, 1f, 1f, 1f);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    }
}
