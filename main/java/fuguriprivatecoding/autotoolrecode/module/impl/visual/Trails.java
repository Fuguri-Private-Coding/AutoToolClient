package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Trails", category = Category.VISUAL, description = "Оставляет след за вами.")
public class Trails extends Module {

    final IntegerSetting lifeTime = new IntegerSetting("LifeTime", this, 100, 5000, 1);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, 1f, 10f, 1f, 0.1f) {};
    final CheckBox onlyThirdPerson = new CheckBox("OnlyThirdPerson", this, false);

    public final ColorSetting color = new ColorSetting("Color", this);
    final CheckBox glow = new CheckBox("Glow", this, false);
    public final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    final List<Doubles<Vec3, Long>> bottomList;

    public Trails() {
        bottomList = new CopyOnWriteArrayList<>();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (onlyThirdPerson.isToggled() && mc.gameSettings.thirdPersonView == 0) {
            return;
        }

        if (event instanceof Render3DEvent) {
            render();
        }
    }

    private void render() {
        Vec3 smoothVec = calculateSmoothPosition();
        updateTrailPoints(smoothVec);
        setupRender();

        if (glow.isToggled()) {
            renderWithGlowEffect();
        }
        renderTrail();

        cleanupRender();
    }

    private Vec3 calculateSmoothPosition() {
        return new Vec3(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks
        );
    }

    private void updateTrailPoints(Vec3 newPoint) {
        long currentTime = System.currentTimeMillis();
        bottomList.add(new Doubles<>(newPoint, currentTime));

        // Удаление точек, которые существуют дольше установленного времени
        bottomList.removeIf(point -> currentTime - point.getSecond() >= lifeTime.getValue());
    }

    private void setupRender() {
        RenderUtils.start3D();
        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
        GL11.glLineWidth(lineWidth.getValue());
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void renderWithGlowEffect() {
        GL11.glColor4f(1, 1, 1, 1);
        BloomRealUtils.addToDraw(() -> renderSingleLine(glowColor.getColor(), glowColor.getFadeColor()));
    }

    private void renderTrail() {
        renderSingleLine(color.getColor(), color.getFadeColor());
    }

    private void cleanupRender() {
        ColorUtils.resetColor();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(1f);
        GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
        RenderUtils.stop3D();
    }

    private void renderSingleLine(Color primaryColor, Color fadeColor) {
        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (Doubles<Vec3, Long> point : bottomList) {
            Color currentColor = calculatePointColor(primaryColor, fadeColor, point);
            Vec3 position = point.getFirst();
            float alpha = calculatePointAlpha(point);

            RenderUtils.glColor(currentColor, alpha);
            GL11.glVertex3d(position.xCoord, position.yCoord, position.zCoord);
        }

        GL11.glEnd();
    }

    private Color calculatePointColor(Color primaryColor, Color fadeColor, Doubles<Vec3, Long> point) {
        if (color.isFade()) {
            int pointIndex = bottomList.indexOf(point);
            return ColorUtils.mixColor(primaryColor, fadeColor, pointIndex, color.getOffset(), color.getSpeed());
        }
        return primaryColor;
    }

    private float calculatePointAlpha(Doubles<Vec3, Long> point) {
        long timeAlive = System.currentTimeMillis() - point.getSecond();
        return 1 - (float) timeAlive / lifeTime.getValue();
    }
}
