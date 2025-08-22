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
import fuguriprivatecoding.autotoolrecode.utils.doubles.Doubles;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Trails", category = Category.VISUAL, description = "Оставляет след за вами.")
public class Trails extends Module {

    final Mode mode = new Mode("Mode", this)
            .addModes("SingleLine", "PlayerLine")
            .setMode("PlayerLine");

    final List<Doubles<Vec3, Long>> bottomList, topList;

    final IntegerSetting lifeTime = new IntegerSetting("LifeTime", this, 100, 5000, 1);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, 1f, 10f, 1f, 0.1f) {};
    final CheckBox onlyThirdPerson = new CheckBox("OnlyThirdPerson", this, false);

    public final ColorSetting color = new ColorSetting("Color", this);

    Glow shadows;
    Color fadeColor;

    public Trails() {
        topList = new CopyOnWriteArrayList<>();
        bottomList = new CopyOnWriteArrayList<>();
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (onlyThirdPerson.isToggled() && mc.gameSettings.thirdPersonView == 0) return;
        if (event instanceof Render3DEvent) {
            Vec3 smoothVec = new Vec3(
                    mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks
            );
            bottomList.add(new Doubles<>(smoothVec, System.currentTimeMillis()));
            topList.add(new Doubles<>(new Vec3(smoothVec.xCoord, smoothVec.yCoord + mc.thePlayer.height, smoothVec.zCoord), System.currentTimeMillis()));

            bottomList.removeIf(p -> System.currentTimeMillis() - p.getSecond() >= lifeTime.getValue());
            topList.removeIf(p -> System.currentTimeMillis() - p.getSecond() >= lifeTime.getValue());
            RenderUtils.start3D();
            GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            GL11.glLineWidth(lineWidth.getValue());
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            switch (mode.getMode()) {
                case "SingleLine" -> {
                    if (shadows.isToggled() && shadows.module.get("Trails")) {
                        GL11.glColor4f(1,1,1,1);
                        BloomUtils.addToDraw(() -> renderSingleLine(Color.white, Color.white));
                    }
                    renderSingleLine(color.getColor(), color.getFadeColor());
                }
                case "PlayerLine" -> {
                    if (shadows.isToggled() && shadows.module.get("Trails")) {
                        GL11.glColor4f(1,1,1,1);
                        BloomUtils.addToDraw(() -> renderPlayerLine(Color.white, Color.white));
                    }
                    renderPlayerLine(color.getColor(), color.getFadeColor());
                }
            }
            ColorUtils.resetColor();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glLineWidth(1f);
            GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            RenderUtils.stop3D();
        }
    }

    private void renderSingleLine(Color color1, Color color2) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        bottomList.forEach(p -> {
            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2, bottomList.indexOf(p), color.getOffset(), color.getSpeed())
                    : color1;

            Vec3 pos = p.getFirst();
            RenderUtils.glColor(fadeColor, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue()));
            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
        });
        GL11.glEnd();
    }

    private void renderPlayerLine(Color color1, Color color2) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        bottomList.forEach(p -> {
            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2, bottomList.indexOf(p), color.getOffset(), color.getSpeed())
                    : color1;

            Vec3 pos = p.getFirst();
            RenderUtils.glColor(fadeColor, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue()));
            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
        });
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        topList.forEach(p -> {
            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2, topList.indexOf(p), color.getOffset(), color.getSpeed())
                    : color1;

            Vec3 pos = p.getFirst();
            RenderUtils.glColor(fadeColor, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue()));
            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
        });
        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (Doubles<Vec3, Long> bottomVec : bottomList) {
            fadeColor = color.isFade() ?
                    ColorUtils.mixColor(color1, color2, bottomList.indexOf(bottomVec), color.getOffset(), color.getSpeed())
                    : color1;

            RenderUtils.glColor(fadeColor, 0.6f * (1 - (float) (System.currentTimeMillis() - bottomVec.getSecond()) / (lifeTime.getValue())));
            GL11.glVertex3d(bottomVec.getFirst().xCoord, bottomVec.getFirst().yCoord, bottomVec.getFirst().zCoord);
            GL11.glVertex3d(bottomVec.getFirst().xCoord, bottomVec.getFirst().yCoord + mc.thePlayer.height, bottomVec.getFirst().zCoord);
        }

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
    }
}
