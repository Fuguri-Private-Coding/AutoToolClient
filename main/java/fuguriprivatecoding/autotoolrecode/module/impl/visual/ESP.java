package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.value.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "ESP", category = Category.VISUAL, description = "Отображение игроков сквозь стены.")
public class ESP extends Module {

    final MultiMode modes = new MultiMode("Modes", this)
        .add("HitBox")
        .add("Corner")
        .add("Box")
        ;

    BooleanSupplier renderHitBox = () -> modes.get("HitBox");
    BooleanSupplier renderBox = () -> modes.get("Box");
    BooleanSupplier renderCorner = () -> modes.get("Corner");

    final CheckBox useServerPositions = new CheckBox("UseServerPositions", this, renderHitBox, false);

    final ColorSetting hitBoxColor = new ColorSetting("HitBoxColor", this, renderHitBox);
    final ColorSetting hitBoxTeamColor = new ColorSetting("HitBoxTeamColor", this, renderHitBox);

    final FloatSetting hitBoxLineWidth = new FloatSetting("HitBoxLineWidth", this, renderHitBox, 1, 10, 2, 0.1f);

    final ColorSetting boxColor = new ColorSetting("BoxColor", this, renderBox);
    final ColorSetting boxTeamColor = new ColorSetting("BoxTeamColor", this, renderBox);

    final FloatSetting boxLineWidth = new FloatSetting("BoxLineWidth", this, renderBox, 1, 10, 2, 0.1f);

    final ColorSetting cornerColor = new ColorSetting("CornerColor", this, renderCorner);
    final ColorSetting cornerTeamColor = new ColorSetting("CornerTeamColor", this, renderCorner);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent && modes.get("Box")) {
            List<BoxRender> boxes = new ArrayList<>();

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (shouldContinueRender(player)) continue;

                Vec3 pos = player == mc.thePlayer || !useServerPositions.isToggled() ?
                    RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()) :
                    player.getRealPosition();

                AxisAlignedBB bb = player.getEntityBoundingBox()
                    .offset(pos.xCoord - player.posX, pos.yCoord - player.posY, pos.zCoord - player.posZ)
                    .expand(0.1D);

                Vec3[] points = RotUtils.getPoints(bb);

                boolean skip = false;
                List<Vector2f> posList = new java.util.ArrayList<>();

                for (Vec3 vec3 : points) {
                    mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                    float[] positions = Convertors.convert2D(vec3.subtract(RenderManager.getRenderPosition()), 1);
                    mc.entityRenderer.setupOverlayRendering();

                    if (positions == null || positions[2] > 1) {
                        skip = true;
                        break;
                    }

                    posList.add(new Vector2f(positions[0], positions[1]));
                }

                if (skip) continue;

                float minX = posList.stream().min(Comparator.comparingDouble(Vector2f::x)).orElse(Constants.VEC2F_ZERO).x;
                float maxX = posList.stream().max(Comparator.comparingDouble(Vector2f::x)).orElse(Constants.VEC2F_ZERO).x;
                float minY = posList.stream().min(Comparator.comparingDouble(Vector2f::y)).orElse(Constants.VEC2F_ZERO).y;
                float maxY = posList.stream().max(Comparator.comparingDouble(Vector2f::y)).orElse(Constants.VEC2F_ZERO).y;

                boxes.add(new BoxRender(minX, minY, maxX, maxY, player.isTeam() ? boxTeamColor.getFadedColor() : boxColor.getFadedColor()));
            }

            float scaleFactor = ScaleUtils.getScaledResolution().scaleFactor;
            GL11.glScalef(1 / scaleFactor, 1 / scaleFactor, 1);

            for (BoxRender box : boxes) {
                float width = box.maxX - box.minX;
                float height = box.maxY - box.minY;

                RenderUtils.drawHorizontalLine(box.minX, width, box.minY, boxLineWidth.getValue(), box.color);
                RenderUtils.drawHorizontalLine(box.minX, width, box.maxY, boxLineWidth.getValue(), box.color);
                RenderUtils.drawVerticalLine(box.minX, box.minY, height, boxLineWidth.getValue(), box.color);
                RenderUtils.drawVerticalLine(box.maxX, box.minY, height, boxLineWidth.getValue(), box.color);
            }

            if (glow.isToggled()) {
                BloomUtils.startWrite();

                for (BoxRender box : boxes) {
                    float width = box.maxX - box.minX;
                    float height = box.maxY - box.minY;

                    Color glowColor = this.glowColor.getFadedColor();

                    RenderUtils.drawHorizontalLine(box.minX, width, box.minY, boxLineWidth.getValue(), glowColor);
                    RenderUtils.drawHorizontalLine(box.minX, width, box.maxY, boxLineWidth.getValue(), glowColor);
                    RenderUtils.drawVerticalLine(box.minX, box.minY, height, boxLineWidth.getValue(), glowColor);
                    RenderUtils.drawVerticalLine(box.maxX, box.minY, height, boxLineWidth.getValue(), glowColor);
                }

                BloomUtils.stopWrite();
            }
            GL11.glScalef(scaleFactor, scaleFactor, 1f);
        }

        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            if (glow.isToggled()) {
                BloomUtils.startWrite();
                for (EntityPlayer player : mc.theWorld.playerEntities) {
                    if (!shouldContinueRender(player)) {
                        render(player, true);
                    }
                }
                BloomUtils.stopWrite();
            }

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!shouldContinueRender(player)) {
                    render(player, false);
                }
            }
            RenderUtils.stop3D();
        }
    }

    private void render(EntityPlayer player, boolean glow) {
        Vec3 pos = player == mc.thePlayer || !useServerPositions.isToggled() ?
            RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()).subtract(RenderManager.getRenderPosition()) : player.getRealPosition();

        if (modes.get("HitBox")) {
            AxisAlignedBB bb = player.getEntityBoundingBox().offset(
                pos.xCoord - player.posX,
                pos.yCoord - player.posY,
                pos.zCoord - player.posZ
            );

            Color color = glow
                ? glowColor.getFadedColor()
                : (player.isTeam() ? hitBoxTeamColor.getFadedColor() : hitBoxColor.getFadedColor());

            RenderUtils.drawHitBox(bb, color, hitBoxLineWidth.getValue());
        }

        if (modes.get("Corner")) {
            Color color = glow
                ? glowColor.getFadedColor()
                : (player.isTeam() ? cornerTeamColor.getFadedColor() : cornerColor.getFadedColor());

            RenderUtils.drawCornerESP(player, color);
        }
    }

    @Getter
    @AllArgsConstructor
    static class BoxRender {
        float minX, minY, maxX, maxY;
        Color color;
    }

    private boolean shouldContinueRender(Entity player) {
        return mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead;
    }
}
