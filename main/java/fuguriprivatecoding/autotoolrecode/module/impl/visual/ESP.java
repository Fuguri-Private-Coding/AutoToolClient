package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "ESP", category = Category.VISUAL, description = "Отображение игроков сквозь стены.")
public class ESP extends Module {

    final MultiMode modes = new MultiMode("Modes", this)
        .add("HitBox")
        .add("Corner")
        .add("Glow")
        ;

    BooleanSupplier renderBox = () -> modes.get("HitBox");

    final CheckBox useRealPositions = new CheckBox("UseRealPositions", this, renderBox, false);

    final ColorSetting color = new ColorSetting("Color", this, () -> !modes.get("Glow"));
    final CheckBox glow = new CheckBox("Glow", this, false);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, () -> !modes.get("Glow") && glow.isToggled());

    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f, 5f, 1f, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (shouldContinueRender(player)) continue;

                Vec3 pos = player == mc.thePlayer || !useRealPositions.isToggled() ?
                    RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()).subtract(RenderManager.getRenderPosition()) :
                    player.getRealPosition();

                if (modes.get("HitBox")) {
                    AxisAlignedBB bb = player.getEntityBoundingBox().offset(pos.xCoord - player.posX, pos.yCoord - player.posY, pos.zCoord - player.posZ);
                    RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());

                    if (glow.isToggled()) {
                        BloomUtils.addToDraw(() -> RenderUtils.drawHitBox(bb, glowColor.getFadedColor(), lineWidth.getValue()));
                    }
                }

                if (modes.get("Corner")) {
                    RenderUtils.drawCornerESP(player, color.getFadedColor());

                    if (glow.isToggled()) {
                        BloomUtils.addToDraw(() -> RenderUtils.drawCornerESP(player, glowColor.getFadedColor()));
                    }
                }

                if (modes.get("Glow")) {
                    BloomUtils.addToDraw(() -> mc.renderManager.renderEntitySimple(player, mc.timer.renderPartialTicks));
                    RenderHelper.disableStandardItemLighting();
                    mc.entityRenderer.disableLightmap();
                }
            }
            RenderUtils.stop3D();
        }
    }

    private boolean shouldContinueRender(Entity player) {
        return mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead;
    }
}
