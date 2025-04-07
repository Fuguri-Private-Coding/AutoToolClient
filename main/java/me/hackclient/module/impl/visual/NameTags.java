package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.misc.MidClick;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, toggled = true)
public class NameTags extends Module {

    ColorSetting color = new ColorSetting("Color", this, 1,1,1,1);

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (event instanceof Render3DEvent) {
            RenderUtils.start3DNameTag();
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                renderNameTag(entity);
            }
            RenderUtils.stop3DNameTag();
        }
    }

    private void renderNameTag(Entity entity) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        float distance = mc.thePlayer.getDistanceToEntity(entity);
        float scale = Math.max(distance / 2.5f, 5.0f);
        scale /= 200f;
        glPushMatrix();
        glTranslated(
                (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX),
                (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY + entity.getEyeHeight() + 0.6),
                (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ)
        );

        glNormal3f(0.0f, 1.0f, 0.0f);
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        glScalef(-scale, -scale, scale);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        boolean friend = entity instanceof EntityPlayer ent && Client.INSTANCE.getModuleManager().getModule(MidClick.class).showInName.isToggled() && ent.isFriend();
        String text = friend ? "§2[Friend]§9 " + entity.getName() : entity.getName();
        float offset = fontRenderer.FONT_HEIGHT - 8f;
        float stringWidth = fontRenderer.getStringWidth(text) / 2f;
        RoundedUtils.drawRect(-stringWidth - 2, offset - 3, stringWidth * 2 + 4, fontRenderer.FONT_HEIGHT + 4, 2f, color.getColor());
        fontRenderer.drawString(
                text,
                -stringWidth,
                offset,
                -1,
                true
        );
        glColor4f(1f, 1f, 1f, 1f);
        glPopMatrix();
    }
}
