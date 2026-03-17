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
        .add("Glow")
        ;

    BooleanSupplier renderBox = () -> modes.get("HitBox");

    final CheckBox useRealPositions = new CheckBox("UseRealPositions", this, renderBox, false);

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f, 5f, 1f, 0.1f);

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            if (modes.get("HitBox")) {
                RenderUtils.start3D();
                for (EntityPlayer player : mc.theWorld.playerEntities) {
                    if (shouldContinueRender(player)) continue;

                    Vec3 pos = player == mc.thePlayer || !useRealPositions.isToggled() ?
                        RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()).subtract(RenderManager.getRenderPosition()) :
                        player.getRealPosition();

                    AxisAlignedBB bb = player.getEntityBoundingBox().offset(pos.xCoord - player.posX, pos.yCoord - player.posY, pos.zCoord - player.posZ);
                    RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                }
                RenderUtils.stop3D();
            }

            if (modes.get("Glow")) {
                for (final EntityPlayer player : mc.theWorld.playerEntities) {
                    if (shouldContinueRender(player)) continue;

                    BloomUtils.addToDraw(() -> mc.renderManager.renderEntitySimple(player, mc.timer.renderPartialTicks));
                }
                RenderHelper.disableStandardItemLighting();
                mc.entityRenderer.disableLightmap();
            }
        }
    }

    private boolean shouldContinueRender(Entity player) {
        return mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead;
    }
}
