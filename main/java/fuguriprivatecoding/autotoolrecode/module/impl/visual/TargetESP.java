package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetESP", category = Category.VISUAL, description = "Показывает какого противника вы бьете.")
public class TargetESP extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Sigma", "Sigma2")
            .setMode("Sigma");

    final FloatSetting speed = new FloatSetting("Speed", this, 1f, 10f, 3f, 0.1f) {};
    final IntegerSetting quality = new IntegerSetting("Quality", this, 1, 360, 60);
    final FloatSetting length = new FloatSetting("Length", this, 0.2f, 2.5f, 0.6f, 0.1f) {};
    final FloatSetting radius = new FloatSetting("Radius", this, 0.1f, 2f, 0.7f, 0.1f) {};

    final CheckBox fadeBoxColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeBoxColor::isToggled, 1f,1f,1f,1f);
    final FloatSetting fadeOffset = new FloatSetting("FadeOffset", this, fadeBoxColor::isToggled,0f, 20, 1, 0.1f);

    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeBoxColor::isToggled, 0.1f, 20, 1, 0.1f);

    private final List<Sigma2> poses = new ArrayList<>();

    Glow shadows;
    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (event instanceof Render3DEvent) {
            switch (mode.getMode()) {
                case "Sigma" -> {
                    if (shadows.isToggled() && shadows.module.get("TargetESP")) {
                        BloomUtils.addToDraw(() -> renderSigma(Color.white, Color.white));
                    }
                    renderSigma(color1.getColor(), color2.getColor());
                }

                case "Sigma2" -> {
                    if (shadows.isToggled() && shadows.module.get("TargetESP")) {
                        BloomUtils.addToDraw(() -> renderSigma2(Color.white, Color.white));
                    }
                    renderSigma2(color1.getColor(), color2.getColor());
                }
            }
        }
    }

    private void renderSigma(Color color1, Color color2) {
        double animationTranslate = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());

        final EntityLivingBase target = Client.INST.getCombatManager().getTarget();

        if (target == null) return;

        final RenderManager renderManager = mc.getRenderManager();
        final double viewerPosX = renderManager.viewerPosX;
        final double viewerPosY = renderManager.viewerPosY;
        final double viewerPosZ = renderManager.viewerPosZ;

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - viewerPosY;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - viewerPosZ;

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glShadeModel(7425);
        glBegin(GL_QUAD_STRIP);
        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();
            double y1 = y + (animationTranslate + 1) / 2 * target.height;

            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2,i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            ColorUtils.glColor(fadeColor);
            glVertex3d(x1, y1, z1);
            ColorUtils.glColor(fadeColor, 0f);
            glVertex3d(x1, y1 + animationTranslate * length.getValue(), z1);
        }
        glEnd();
        glLineWidth(5f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();
            double y1 = y + (animationTranslate + 1) / 2 * target.height;

            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2,i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            ColorUtils.glColor(fadeColor, 1.0f);
            glVertex3d(x1, y1, z1);
            ColorUtils.glColor(fadeColor, 1.0f);
            glVertex3d(x1, y1, z1);
        }
        glEnd();
        glEnable(GL_CULL_FACE);
        glShadeModel(7424);
        ColorUtils.resetColor();
        glLineWidth(1f);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
    }

    private void renderSigma2(Color color1, Color color2) {
        final EntityLivingBase target = Client.INST.getCombatManager().getTarget();

        if (target == null) return;

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;

        double f = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());
        f++;
        f /= 2;
        f *= target.height + 0.1f;
        poses.add(new Sigma2((float) f, System.currentTimeMillis()));
        poses.removeIf(pose -> System.currentTimeMillis() - pose.time() >= 500 / speed.getValue() * length.getValue());
        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glShadeModel(7425);
        glBegin(GL_QUAD_STRIP);
        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();

            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2,i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            RenderUtils.glColor(fadeColor, 0f);
            glVertex3d(x1, y + poses.getFirst().value, z1);
            RenderUtils.glColor(fadeColor);
            glVertex3d(x1, y + poses.getLast().value, z1);
        }

        glEnd();

        glLineWidth(5f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();

            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2,i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            RenderUtils.glColor(fadeColor, 1f);
            glVertex3d(x1, y + poses.getLast().value, z1);
            RenderUtils.glColor(fadeColor, 1f);
            glVertex3d(x1, y + poses.getLast().value, z1);
        }

        glEnd();
        glEnable(GL_CULL_FACE);
        glShadeModel(7424);
        glLineWidth(1f);
        ColorUtils.resetColor();
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
    }

    private record Sigma2(float value, long time) { }
}
