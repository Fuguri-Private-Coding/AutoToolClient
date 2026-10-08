package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import java.awt.*;

@ModuleInfo(name = "ESP", category = Category.VISUAL, description = "Отображение игроков сквозь стены.")
public class ESP extends Module {

    final ColorSetting hitBoxColor = new ColorSetting("HitBoxColor", this);
    final ColorSetting hitBoxTeamColor = new ColorSetting("HitBoxTeamColor", this);

    final IntegerSetting hitBoxLineWidth = new IntegerSetting("HitBoxLineWidth", this, 1, 10, 2);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();

            if (glow.isToggled()) {
                BloomUtils.startWrite();
                RenderUtils.startHitBoxBegin(hitBoxLineWidth.getValue());
                for (EntityPlayer entity : mc.theWorld.playerEntities) {
                    if (shouldContinueRender(entity))
                        continue;

                    AxisAlignedBB bb = entity.getExpandedBoundingBox()
                        .offset(entity.getSmoothPositionVector().subtract(RenderManager.getRenderPosition())
                            .subtract(entity.getPositionVector()));

                    ColorUtils.glColor(glowColor.getFadedColor());
                    RenderUtils.renderHitBoxBatch(bb);
                }
                ColorUtils.resetColor();
                RenderUtils.endHitBoxBegin();
                BloomUtils.stopWrite();
            }

            RenderUtils.startHitBoxBegin(hitBoxLineWidth.getValue());
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (shouldContinueRender(entity))
                    continue;

                AxisAlignedBB bb = entity.getExpandedBoundingBox()
                    .offset(entity.getSmoothPositionVector().subtract(RenderManager.getRenderPosition())
                        .subtract(entity.getPositionVector()));

                ColorUtils.glColor(entity.isTeam() ? hitBoxTeamColor.getFadedColor() : hitBoxColor.getFadedColor());
                RenderUtils.renderHitBoxBatch(bb);
            }
            ColorUtils.resetColor();
            RenderUtils.endHitBoxBegin();
            RenderUtils.stop3D();
        }
    }

    private boolean shouldContinueRender(Entity entity) {
        return mc.getRenderManager() == null || (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || entity.isDead;
    }
}
