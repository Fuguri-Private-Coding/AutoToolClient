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
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    Vector2f vecZEROSUKABLYATKOSNTSANTIDOBAVTE = new Vector2f(0, 0);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent && modes.get("Box")) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (shouldContinueRender(player)) continue;

                Vec3 pos = player == mc.thePlayer || !useServerPositions.isToggled() ?
                    RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()) :
                    player.getRealPosition();

                AxisAlignedBB bb = player.getEntityBoundingBox().offset(pos.xCoord - player.posX, pos.yCoord - player.posY, pos.zCoord - player.posZ).expand(0.1D);

                Vec3[] points = {
                    new Vec3(bb.minX, bb.minY, bb.minZ),
                    new Vec3(bb.maxX, bb.minY, bb.minZ),
                    new Vec3(bb.maxX, bb.minY, bb.maxZ),
                    new Vec3(bb.minX, bb.minY, bb.maxZ),
                    new Vec3(bb.minX, bb.maxY, bb.minZ),
                    new Vec3(bb.maxX, bb.maxY, bb.minZ),
                    new Vec3(bb.maxX, bb.maxY, bb.maxZ),
                    new Vec3(bb.minX, bb.maxY, bb.maxZ)
                };

                AtomicBoolean skip = new AtomicBoolean(false);

                List<Vector2f> posList = Arrays.stream(points).map(vec3 -> {
                    mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                    float[] positions = Convertors.convert2D(vec3.subtract(RenderManager.getRenderPosition()), 1);
                    mc.entityRenderer.setupOverlayRendering();
                    if (positions == null || positions[2] > 1) {
                        skip.set(true);
                        return null;
                    }

                    return new Vector2f(positions[0], positions[1]);
                }).toList();

                if (skip.get()) continue;

                float minX = posList.stream().min(Comparator.comparingDouble(Vector2f::x)).orElse(vecZEROSUKABLYATKOSNTSANTIDOBAVTE).x;
                float maxX = posList.stream().max(Comparator.comparingDouble(Vector2f::x)).orElse(vecZEROSUKABLYATKOSNTSANTIDOBAVTE).x;
                float minY = posList.stream().min(Comparator.comparingDouble(Vector2f::y)).orElse(vecZEROSUKABLYATKOSNTSANTIDOBAVTE).y;
                float maxY = posList.stream().max(Comparator.comparingDouble(Vector2f::y)).orElse(vecZEROSUKABLYATKOSNTSANTIDOBAVTE).y;

                float scaleFactor = ScaleUtils.getScaledResolution().scaleFactor;

                float width = maxX - minX;
                float height = maxY - minY;

                Color color = player.isTeam() ? boxTeamColor.getFadedColor() : boxColor.getFadedColor();

                GL11.glScalef(1 / scaleFactor, 1 / scaleFactor, 1);
                RenderUtils.drawHorizontalLine(minX, width, minY, boxLineWidth.getValue(), color);
                RenderUtils.drawHorizontalLine(minX, width, maxY, boxLineWidth.getValue(), color);
                RenderUtils.drawVerticalLine(minX, minY, height, boxLineWidth.getValue(), color);
                RenderUtils.drawVerticalLine(maxX, minY, height, boxLineWidth.getValue(), color);

                BloomUtils.startWrite();
                RenderUtils.drawHorizontalLine(minX, width, minY, boxLineWidth.getValue(), glowColor.getFadedColor());
                RenderUtils.drawHorizontalLine(minX, width, maxY, boxLineWidth.getValue(), glowColor.getFadedColor());
                RenderUtils.drawVerticalLine(minX, minY, height, boxLineWidth.getValue(), glowColor.getFadedColor());
                RenderUtils.drawVerticalLine(maxX, minY, height, boxLineWidth.getValue(), glowColor.getFadedColor());
                BloomUtils.stopWrite();

                GL11.glScalef(scaleFactor, scaleFactor, 1);
            }
        }

        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!shouldContinueRender(player)) render(player);
            }
            RenderUtils.stop3D();
        }
    }

    private void render(EntityPlayer player) {
        Vec3 pos = player == mc.thePlayer || !useServerPositions.isToggled() ?
            RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()).subtract(RenderManager.getRenderPosition()) :
            player.getRealPosition();

        if (modes.get("HitBox")) {
            AxisAlignedBB bb = player.getEntityBoundingBox().offset(pos.xCoord - player.posX, pos.yCoord - player.posY, pos.zCoord - player.posZ);

            Color color = player.isTeam() ? hitBoxTeamColor.getFadedColor() : hitBoxColor.getFadedColor();

            RenderUtils.drawHitBox(bb, color, hitBoxLineWidth.getValue());

            if (glow.isToggled()) {
                BloomUtils.startWrite();
                RenderUtils.drawHitBox(bb, glowColor.getFadedColor(), hitBoxLineWidth.getValue());
                BloomUtils.stopWrite();
            }
        }

        if (modes.get("Corner")) {
            Color color = player.isTeam() ? cornerTeamColor.getFadedColor() : cornerColor.getFadedColor();

            RenderUtils.drawCornerESP(player, color);

            if (glow.isToggled()) {
                BloomUtils.startWrite();
                RenderUtils.drawCornerESP(player, glowColor.getFadedColor());
                BloomUtils.stopWrite();
            }
        }
    }

    private boolean shouldContinueRender(Entity player) {
        return mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead;
    }
}
