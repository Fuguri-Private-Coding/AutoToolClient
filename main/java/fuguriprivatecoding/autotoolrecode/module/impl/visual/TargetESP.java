package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
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

    CheckBox glow = new CheckBox("Glow", this);

    public final ColorSetting color = new ColorSetting("Color", this);

    public final ColorSetting colorShadow = new ColorSetting("Shadow Color", this, glow::isToggled);

    private final List<Sigma2> poses = new ArrayList<>();

    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            switch (mode.getMode()) {
                case "Sigma" -> {
                    if (glow.isToggled()) {
                        BloomRealUtils.addToDraw(() -> renderSigma(colorShadow.getColor(), colorShadow.getFadeColor()));
                    }
                    renderSigma(color.getColor(), color.getFadeColor());
                }

                case "Sigma2" -> {
                    if (glow.isToggled()) {
                        BloomRealUtils.addToDraw(() -> renderSigma2(colorShadow.getColor(), colorShadow.getFadeColor()));
                    }
                    renderSigma2(color.getColor(), color.getFadeColor());
                }
            }
        }
    }

    private void renderSigma(Color color1, Color color2) {
        double animationTranslate = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());

        final EntityLivingBase target = Client.INST.getTargetStorage().getTarget();

        if (target == null) return;

        final RenderManager renderManager = mc.getRenderManager();
        final double viewerPosX = renderManager.viewerPosX;
        final double viewerPosY = renderManager.viewerPosY;
        final double viewerPosZ = renderManager.viewerPosZ;

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - viewerPosY + 0.2;
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

            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2,i, color.getOffset(), color.getSpeed())
                    : color1;

            ColorUtils.glColor(fadeColor);
            glVertex3d(x1, y1, z1);
            ColorUtils.glColor(fadeColor, 0f);
            glVertex3d(x1, y1 + animationTranslate * length.getValue(), z1);
        }
        glEnd();
        glLineWidth(2f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();
            double y1 = y + (animationTranslate + 1) / 2 * target.height;

            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2,i, color.getOffset(), color.getSpeed())
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
        final EntityLivingBase target = Client.INST.getTargetStorage().getTarget();

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

            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2,i, color.getOffset(), color.getSpeed())
                    : color1;

            RenderUtils.glColor(fadeColor, 0f);
            glVertex3d(x1, y + poses.getFirst().value, z1);
            RenderUtils.glColor(fadeColor);
            glVertex3d(x1, y + poses.getLast().value, z1);
        }

        glEnd();

        glLineWidth(2f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();

            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2,i, color.getOffset(), color.getSpeed())
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
