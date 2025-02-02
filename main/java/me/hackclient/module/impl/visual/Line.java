package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.event.events.UpdateEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.IntegerSetting;
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.utils.doubles.Doubles;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "Line",
        category = Category.VISUAL
)
public class Line extends Module {

    final ModeSetting mode = new ModeSetting(
            "Mode",
            this,
            "SingleLine",
            new String[] {
                    "SingleLine",
                    "1.16.5 govno"
            }
    );

    final List<Doubles<Vec3, Long>> bottomList, topList;

    final IntegerSetting lifeTime = new IntegerSetting("LifeTime", this, 1, 30, 5);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, 1f, 10f, 5f, 0.1f);
    final BooleanSetting onlyThirdPerson = new BooleanSetting("OnlyThirdPerson", this, true);

    public Line() {
        topList = new CopyOnWriteArrayList<>();
        bottomList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (onlyThirdPerson.isToggled() && mc.gameSettings.thirdPersonView == 0) return;
        if (event instanceof Render3DEvent) {
            Vec3 smoothVec = new Vec3(
                    mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks,
                    mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks
            );
            bottomList.add(new Doubles<>(smoothVec, System.currentTimeMillis()));
            topList.add(new Doubles<>(new Vec3(smoothVec.xCoord, smoothVec.yCoord + mc.thePlayer.height, smoothVec.zCoord), System.currentTimeMillis()));

            bottomList.removeIf(p -> System.currentTimeMillis() - p.getSecond() >= lifeTime.getValue() * 1000L);
            topList.removeIf(p -> System.currentTimeMillis() - p.getSecond() >= lifeTime.getValue() * 1000L);
            RenderUtils.start3D();
            GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
            GL11.glLineWidth(lineWidth.getValue());
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            PixelReplacerUtils.addToDraw( () -> {

                switch (mode.getMode()) {
                    case "SingleLine" -> {
                        GL11.glBegin(GL11.GL_LINE_STRIP);
                        bottomList.forEach(p -> {
                            Vec3 pos = p.getFirst();
                            GL11.glColor4f(1f, 1f, 1f, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue() * 1000));
                            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
                        });
                        GL11.glEnd();
                    }
                    case "1.16.5 govno" -> {
                        GL11.glBegin(GL11.GL_LINE_STRIP);
                        bottomList.forEach(p -> {
                            Vec3 pos = p.getFirst();
                            GL11.glColor4f(1f, 1f, 1f, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue() * 1000));
                            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
                        });
                        GL11.glEnd();

                        GL11.glBegin(GL11.GL_LINE_STRIP);
                        topList.forEach(p -> {
                            Vec3 pos = p.getFirst();
                            GL11.glColor4f(1f, 1f, 1f, 1 - (float) (System.currentTimeMillis() - p.getSecond()) / (lifeTime.getValue() * 1000));
                            GL11.glVertex3d(pos.xCoord, pos.yCoord, pos.zCoord);
                        });
                        GL11.glEnd();


                        GL11.glShadeModel(GL11.GL_SMOOTH);
                        GL11.glBegin(GL11.GL_QUAD_STRIP);

                        for (Doubles<Vec3, Long> bottomVec : bottomList) {
                            GL11.glColor4f(1f, 1f, 1f, 0.6f * (1 - (float) (System.currentTimeMillis() - bottomVec.getSecond()) / (lifeTime.getValue() * 1000)));
                            GL11.glVertex3d(bottomVec.getFirst().xCoord, bottomVec.getFirst().yCoord, bottomVec.getFirst().zCoord);
                            GL11.glVertex3d(bottomVec.getFirst().xCoord, bottomVec.getFirst().yCoord + mc.thePlayer.height, bottomVec.getFirst().zCoord);
                        }

                        GL11.glEnd();
                        GL11.glShadeModel(GL11.GL_FLAT);
                    }
                }
            });

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glLineWidth(1f);
            GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            RenderUtils.stop3D();
        }
    }
}
