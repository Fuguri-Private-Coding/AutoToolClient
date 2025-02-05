package me.hackclient.module.impl.visual;

import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.shader.impl.TestBloomUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(
        name = "ESP",
        category = Category.VISUAL
)
public class ESP extends Module {

    final MultiBooleanSetting modes = new MultiBooleanSetting("Modes", this)
            .add("Box")
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
                        TestBloomUtils.add(() -> RenderUtils.renderHitBox(playerEntity.getEntityBoundingBox().offset(diff)));
                        RenderUtils.renderHitBox(playerEntity.getEntityBoundingBox().offset(diff));
                    }
                }
            }

            GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            RenderUtils.stop3D();
        }
    }
}
