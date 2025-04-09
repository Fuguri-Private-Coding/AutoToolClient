package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(
        name = "ESP",
        category = Category.VISUAL
)
public class ESP extends Module {

    final MultiBooleanSetting modes = new MultiBooleanSetting("Modes", this)
            .add("Box")
            .add("Glow")
            .add("Chams")
            //.add("")
            ;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

            if (modes.get("Box")) {
                for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                    if (playerEntity.equals(mc.thePlayer)) { continue; }

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
            }

            GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            RenderUtils.stop3D();

            if (modes.get("Glow")) {
                InstanceAccess.NORMAL_BlOOM_RUNNABLE.add(() -> {
                    for (final EntityPlayer player : mc.theWorld.playerEntities) {
                        final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(player);

                        if (mc.getRenderManager() == null || render == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead) {
                            continue;
                        }

                        final Color color = Color.white;

                        if (color.getAlpha() <= 0) {
                            continue;
                        }

                        final double x = player.prevPosX + (player.posX - player.prevPosX) * mc.timer.renderPartialTicks;
                        final double y = player.prevPosY + (player.posY - player.prevPosY) * mc.timer.renderPartialTicks;
                        final double z = player.prevPosZ + (player.posZ - player.prevPosZ) * mc.timer.renderPartialTicks;
                        final float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.timer.renderPartialTicks;

                        RendererLivingEntity.setShaderBrightness(Color.green);
                        render.doRender(player, x - RenderManager.renderPosX, y - RenderManager.renderPosY, z - RenderManager.renderPosZ, yaw, mc.timer.renderPartialTicks);
                        RendererLivingEntity.unsetShaderBrightness();
                    }

                    RenderHelper.disableStandardItemLighting();
                    mc.entityRenderer.disableLightmap();
                });
            }

            if (modes.get("Chams")) {
                for (final EntityPlayer player : mc.theWorld.playerEntities) {
                    final Render<EntityPlayer> render = mc.getRenderManager().getEntityRenderObject(player);

                    if (mc.getRenderManager() == null || render == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead) {
                        continue;
                    }

                    final Color color = Color.white;

                    if (color.getAlpha() <= 0) {
                        continue;
                    }

                    final double x = player.prevPosX + (player.posX - player.prevPosX) * mc.timer.renderPartialTicks;
                    final double y = player.prevPosY + (player.posY - player.prevPosY) * mc.timer.renderPartialTicks;
                    final double z = player.prevPosZ + (player.posZ - player.prevPosZ) * mc.timer.renderPartialTicks;
                    final float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.timer.renderPartialTicks;

                    RendererLivingEntity.setShaderBrightness(Color.green);
                    render.doRender(player, x - RenderManager.renderPosX, y - RenderManager.renderPosY, z - RenderManager.renderPosZ, yaw, mc.timer.renderPartialTicks);
                    RendererLivingEntity.unsetShaderBrightness();
                }

                RenderHelper.disableStandardItemLighting();
                mc.entityRenderer.disableLightmap();
            }
        }
    }
}
