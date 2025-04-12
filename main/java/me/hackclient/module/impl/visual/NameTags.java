package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.Render3DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.module.impl.misc.MidClick;
import me.hackclient.module.impl.misc.MurderDetector;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.render.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, toggled = true)
public class NameTags extends Module {

    FloatSetting height = new FloatSetting("Height", this, 0,2,0.6F, 0.1F);

    ColorSetting color = new ColorSetting("Color", this, 1,1,1,1);
    ColorSetting textColor = new ColorSetting("TextColor", this, 1,1,1,1);

    MurderDetector murderDetector;
    MidClick midClick;
    Shadows shadows;

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        if (murderDetector == null) murderDetector = Client.INSTANCE.getModuleManager().getModule(MurderDetector.class);
        if (midClick == null) midClick = Client.INSTANCE.getModuleManager().getModule(MidClick.class);
        if (event instanceof Render3DEvent) {
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue;
                RenderUtils.start3DNameTag();
                renderNameTag(entity);
                RenderUtils.stop3DNameTag();
            }
        }
    }

    public void renderNameTag(Entity entity) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        float distance = mc.thePlayer.getDistanceToEntity(entity);
        float scale = Math.max(distance / 2.5f, 5.0f);
        scale /= 200f;
        glPushMatrix();
        glTranslated(
                (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX),
                (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY + entity.getEyeHeight() + height.getValue()),
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
        boolean friend = entity instanceof EntityPlayer ent && midClick.showInName.isToggled() && ent.isFriend();
        boolean murder = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.murders.contains(ent.getName());
        String murderText = murder ? "§4[Murder]§4 " : "";
        String friendText = friend ? "§2[Friend]§a " : "";
        String text = friendText + murderText + entity.getName();
        float offset = fontRenderer.FONT_HEIGHT - 8f;
        float stringWidth = fontRenderer.getStringWidth(text) / 2f;
        if (shadows.isToggled() && shadows.nameTags.isToggled()) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(-stringWidth - 2, offset - 3, stringWidth * 2 + 4, fontRenderer.FONT_HEIGHT + 4, 2f, Color.WHITE));
        }
        RoundedUtils.drawRect(-stringWidth - 2, offset - 3, stringWidth * 2 + 4, fontRenderer.FONT_HEIGHT + 4, 2f, color.getColor());
        fontRenderer.drawString(text, -stringWidth, offset, textColor.getColor().getRGB(), true);
        glColor4f(1f, 1f, 1f, 1f);
        glPopMatrix();
    }
}
