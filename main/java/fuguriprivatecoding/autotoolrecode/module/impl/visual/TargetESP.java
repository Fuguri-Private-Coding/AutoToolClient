package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import java.awt.*;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetESP", category = Category.VISUAL, description = "Показывает какого противника вы бьете.")
public class TargetESP extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("Sigma")
            .setMode("Sigma");

    final FloatSetting speed = new FloatSetting("Speed", this, 1f, 10f, 3f, 0.1f) {};
    final IntegerSetting quality = new IntegerSetting("Quality", this, 1, 360, 60);
    final FloatSetting length = new FloatSetting("Length", this, 0.2f, 2.5f, 0.6f, 0.1f) {};
    final FloatSetting radius = new FloatSetting("Radius", this, 0.1f, 2f, 0.7f, 0.1f) {};

    public final ColorSetting color = new ColorSetting("Color", this);

    CheckBox changeHitColor = new CheckBox("ChangeHitColor", this);
    public final ColorSetting hitColor = new ColorSetting("HitColor", this, changeHitColor::isToggled);

    CheckBox glow = new CheckBox("Glow", this);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            EntityLivingBase target = TargetStorage.getTarget();

            if (target != null) {
                float hurt = target.hurtTime / 10f;

                if (mode.getMode().equals("Sigma")) {
                    if (glow.isToggled()) BloomUtils.addToDraw(() -> renderSigma(target, hurt));
                    renderSigma(target, hurt);
                }
            }
        }
    }

    private void renderSigma(EntityLivingBase target, float hurt) {
        double animation = sin(System.currentTimeMillis() / 1000.0 * speed.getValue());

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY + 0.2;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ;

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
            double y1 = y + (animation + 1) / 2 * target.height;

            Color fadeColor = hurt == 0 ? color.getMixedColor(i) : hitColor.getMixedColor(i);

            ColorUtils.glColor(fadeColor);
            glVertex3d(x1, y1, z1);
            ColorUtils.glColor(fadeColor, 0f);
            glVertex3d(x1, y1 + animation * length.getValue(), z1);
        }
        glEnd();
        glLineWidth(2f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            double x1 = x + sin(i * Math.PI / 180) * radius.getValue();
            double z1 = z + cos(i * Math.PI / 180) * radius.getValue();
            double y1 = y + (animation + 1) / 2 * target.height;

            Color fadeColor = hurt == 0 ? color.getMixedColor(i) : hitColor.getMixedColor(i);

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
}
