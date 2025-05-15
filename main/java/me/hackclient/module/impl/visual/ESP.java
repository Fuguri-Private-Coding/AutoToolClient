package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.MultiMode;
import me.hackclient.utils.render.shader.impl.BloomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name = "ESP", category = Category.VISUAL)
public class ESP extends Module {

    final MultiMode modes = new MultiMode("Modes", this)
            .add("Box")
            .add("Glow")
            //.add("")
            ;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            if (modes.get("Box")) {
                RenderUtils.start3D();
                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
                for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                    if (playerEntity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) { continue; }

                    Vec3 smoothPos = new Vec3(
                            playerEntity.lastTickPosX + (playerEntity.posX - playerEntity.lastTickPosX) * mc.timer.renderPartialTicks,
                            playerEntity.lastTickPosY + (playerEntity.posY - playerEntity.lastTickPosY) * mc.timer.renderPartialTicks,
                            playerEntity.lastTickPosZ + (playerEntity.posZ - playerEntity.lastTickPosZ) * mc.timer.renderPartialTicks
                    );

                    Vec3 diff = smoothPos.subtract(playerEntity.getPositionVector());

                    if (!playerEntity.equals(mc.thePlayer)) {
                        RenderUtils.renderHitBox(playerEntity.getEntityBoundingBox().offset(diff));
                        RenderUtils.renderHitBox(playerEntity.getEntityBoundingBox().offset(diff));
                    }
                }
                GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
                RenderUtils.stop3D();
            }

            if (modes.get("Glow")) {
                for (final EntityPlayer player : mc.theWorld.playerEntities) {
                    if (mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead) continue;
                    BloomUtils.addToDraw(() -> mc.renderManager.renderEntitySimple(player, mc.timer.renderPartialTicks));
                }
                RenderHelper.disableStandardItemLighting();
                mc.entityRenderer.disableLightmap();
            }
        }
    }
}
