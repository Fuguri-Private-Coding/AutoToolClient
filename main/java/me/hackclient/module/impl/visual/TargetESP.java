package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.*;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetESP", category = Category.VISUAL, toggled = true)
public class TargetESP extends Module {


    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "Sigma",
            new String[] {
                    "Sigma", "Sigma2"
            }
    );

    final FloatSetting speed = new FloatSetting("Speed", this, 1f, 10f, 3f, 0.1f) {};
    final IntegerSetting quality = new IntegerSetting("Quality", this, 1, 360, 60);
    final FloatSetting length = new FloatSetting("Length", this, 0.2f, 1.5f, 0.6f, 0.1f) {};
    final ColorSetting color = new ColorSetting("Color", this, 1f,1f,1f,1f);
    final BooleanSetting changeColorHit = new BooleanSetting("ChangeHitColor", this, false);
    final ColorSetting hitColor = new ColorSetting("HitColor", this, changeColorHit::isToggled, 1f,1f,1f,1f);

    private final List<Test> poses = new ArrayList<>();

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
            switch (mode.getMode()) {
                case "Sigma" -> {
                    double animationTranslate = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());

                    final EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

                    if (target == null) {
                        return;
                    }

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
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor());
                        glVertex3d(x1, y1, z1);
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 0f);
                        glVertex3d(x1, y1 + animationTranslate * length.getValue(), z1);
                    }

                    for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
                        double x1 = x + sin(i * Math.PI / 180) * 0.7;
                        double z1 = z + cos(i * Math.PI / 180) * 0.7;
                        double y1 = y + (animationTranslate + 1) / 2 * target.height;
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 1.0f);
                        glVertex3d(x1, y1, z1);
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 1.0f);
                        glVertex3d(x1, y1 + 0.02f, z1);
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

                case "Sigma2" -> {
                    final EntityLivingBase target = Client.INSTANCE.getCombatManager().getTarget();

                    if (target == null) {
                        return;
                    }

                    double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
                    double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
                    double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

                    double f = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());
                    f++;
                    f /= 2;
                    f *= target.height;
                    poses.add(new Test((float) f, System.currentTimeMillis()));
                    poses.removeIf(pose -> System.currentTimeMillis() - pose.time() >= 500);

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
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 0f);
                        glVertex3d(x1, y + poses.getFirst().value, z1);
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor());
                        glVertex3d(x1, y + poses.getLast().value, z1);
                    }

                    for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
                        double x1 = x + sin(i * Math.PI / 180) * 0.7;
                        double z1 = z + cos(i * Math.PI / 180) * 0.7;
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 1f);
                        glVertex3d(x1, y + poses.getLast().value, z1);
                        RenderUtils.glColor(changeColorHit.isToggled() && target.hurtTime > 0 ? hitColor.getColor() : color.getColor(), 1f);
                        glVertex3d(x1, y + poses.getLast().value + 0.02, z1);
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
    }

    private record Test(float value, long time) {
    }
}
