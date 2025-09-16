package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "ESP", category = Category.VISUAL, description = "Отображение игроков сквозь стены.")
public class ESP extends Module {

    final MultiMode modes = new MultiMode("Modes", this)
        .add("HitBox")
        .add("Glow")
        //.add("")
        ;

    BooleanSupplier renderBox = () -> (modes.get("HitBox"));

    final ColorSetting color = new ColorSetting("Color", this, renderBox);

    final FloatSetting lineWidth = new FloatSetting("LineWidth", this, renderBox, 1f, 5f, 1f, 0.1f);

    Color fadeColor;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            if (modes.get("HitBox")) {
                RenderUtils.start3D();
                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
                for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                    if (playerEntity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) {
                        continue;
                    }

                    fadeColor = color.isFade() ?
                        ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                        : color.getColor();

                    Vec3 smoothPos = new Vec3(
                        playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * mc.timer.renderPartialTicks,
                        playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * mc.timer.renderPartialTicks,
                        playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * mc.timer.renderPartialTicks
                    );

                    Vec3 diff = smoothPos.subtract(playerEntity.getPositionVector());

                    if (!playerEntity.equals(mc.thePlayer)) {
                        RenderUtils.drawHitBox(playerEntity.getEntityBoundingBox().offset(diff), fadeColor, lineWidth.getValue());
                    }
                }
                GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
                RenderUtils.stop3D();
            }

            if (modes.get("Glow")) {
                for (final EntityPlayer player : mc.theWorld.playerEntities) {
                    if (mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead)
                        continue;
                    BloomUtils.addToDraw(() -> mc.renderManager.renderEntitySimple(player, mc.timer.renderPartialTicks));
                }
                RenderHelper.disableStandardItemLighting();
                mc.entityRenderer.disableLightmap();
            }
        }
    }
}
