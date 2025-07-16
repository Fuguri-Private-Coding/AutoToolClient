package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glShadeModel;

@ModuleInfo(name = "Hat", category = Category.VISUAL)
public class Hat extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("ChinaHat", "Halo")
            .setMode("Halo");

    final FloatSetting yOffset = new FloatSetting("Y-Offset", this, () -> mode.getMode().equalsIgnoreCase("Halo"), -1,1,-0.10f,0.01f);

    final IntegerSetting quality = new IntegerSetting("Quality", this, 1, 360, 60);
    final FloatSetting radius = new FloatSetting("Radius", this, 0.1f, 2f, 0.7f, 0.1f) {};
    final CheckBox whileThirdPerson = new CheckBox("WhileThirdPerson", this);

    final CheckBox fadeBoxColor = new CheckBox("FadeColor", this);
    final ColorSetting color1 = new ColorSetting("Color1", this, 1f,1f,1f,1f);
    final ColorSetting color2 = new ColorSetting("Color2", this, fadeBoxColor::isToggled, 1f,1f,1f,1f);
    final FloatSetting fadeOffset = new FloatSetting("FadeOffset", this, fadeBoxColor::isToggled,0f, 20, 1, 0.1f);

    final FloatSetting fadeSpeed = new FloatSetting("FadeSpeed", this, fadeBoxColor::isToggled, 0.1f, 20, 1, 0.1f);

    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, 1f,10f,1f,0.1f);

    Color fadeColor;
    Shadows shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render3DEvent) {
            if (mc.gameSettings.thirdPersonView == 0 && whileThirdPerson.isToggled()) return;
            switch (mode.getMode()) {
                case "ChinaHat" -> {
                    if (shadows.isToggled() && shadows.module.get("Hat")) {
                        BloomUtils.addToDraw(() -> renderChinaHat(Color.white, Color.white));
                    }
                    renderChinaHat(color1.getColor(), color2.getColor());
                }

                case "Halo" -> {
                    if (shadows.isToggled() && shadows.module.get("Hat")) {
                        BloomUtils.addToDraw(() -> renderHaloHat(Color.white, Color.white));
                    }
                    renderHaloHat(color1.getColor(), color2.getColor());
                }
            }
        }
    }

    private void renderChinaHat(Color color1, Color color2) {
        final RenderManager renderManager = mc.getRenderManager();

        EntityLivingBase target = mc.thePlayer;

        if (target == null) return;

        final double viewerPosX = renderManager.viewerPosX;
        final double viewerPosY = renderManager.viewerPosY;
        final double viewerPosZ = renderManager.viewerPosZ;

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - viewerPosY + (mc.thePlayer.getEyeHeight() + 0.5 + (mc.thePlayer.isSneaking() ? -0.2 : 0));
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - viewerPosZ;

        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glShadeModel(7425);
        glBegin(GL_QUAD_STRIP);
        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2, i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            ColorUtils.glColor(fadeColor);
            GL11.glVertex3d(Math.sin(i * Math.PI / 180.0F) * radius.getValue(), y - 0.25, -Math.cos(i * Math.PI / 180.0F) * radius.getValue());
            ColorUtils.glColor(fadeColor);
            glVertex3d(x, y, z);
        }

        glEnd();
        glLineWidth(5f);
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2, i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            ColorUtils.glColor(fadeColor, 1f);
            glVertex3d(Math.sin(i * Math.PI / 180.0F) * radius.getValue(), y - 0.25, -Math.cos(i * Math.PI / 180.0F) * radius.getValue());
        }
        glEnd();
        glEnable(GL_CULL_FACE);
        glShadeModel(7424);
        ColorUtils.resetColor();
        glLineWidth(1f);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
    }

    private void renderHaloHat(Color color1, Color color2) {
        final RenderManager renderManager = mc.getRenderManager();
        EntityLivingBase target = mc.thePlayer;
        if (target == null) return;

        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - renderManager.viewerPosY + (mc.thePlayer.getEyeHeight() + 0.5 + (mc.thePlayer.isSneaking() ? -0.2 : 0));

        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glShadeModel(7425);
        glLineWidth(lineWidth.getValue());
        glBegin(GL_LINE_STRIP);

        for (int i = 0; i <= 360; i += 360 / quality.getValue()) {
            fadeColor = fadeBoxColor.isToggled() ?
                    ColorUtils.mixColor(color1, color2, i, fadeOffset.getValue(), fadeSpeed.getValue())
                    : color1;

            ColorUtils.glColor(fadeColor, 1f);
            glVertex3d(Math.sin(i * Math.PI / 180.0F) * radius.getValue(), y + yOffset.getValue(), -Math.cos(i * Math.PI / 180.0F) * radius.getValue());
        }
        glEnd();
        glEnable(GL_CULL_FACE);
        glShadeModel(7424);
        ColorUtils.resetColor();
        glLineWidth(1f);
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
    }

}
