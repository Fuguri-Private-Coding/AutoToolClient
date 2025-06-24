package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRCModule;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.MidClick;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.MurderMystery;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL)
public class NameTags extends Module {

    FloatSetting height = new FloatSetting("Height", this, 0,2,0.6F, 0.1F);

    ColorSetting color = new ColorSetting("Color", this, 0,0,0,0.4f);

    MurderMystery murderDetector;
    MidClick midClick;
    Shadows shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (murderDetector == null) murderDetector = Client.INST.getModuleManager().getModule(MurderMystery.class);
        if (midClick == null) midClick = Client.INST.getModuleManager().getModule(MidClick.class);
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
        boolean friend = entity instanceof EntityPlayer ent && midClick.showInName.isToggled() && ent.isFriend();
        boolean murder = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.murders.contains(ent.getName());
        boolean detective = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.detectives.contains(ent.getName());
        boolean user = entity instanceof EntityPlayer ent && IRCModule.usersOnline.get(ent.getName()) != null;
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
        String detectiveText = detective ? "§6[Detective]§6 " : "";
        String murderText = murder ? "§4[Murder]§4 " : "";
        String friendText = friend ? "§2[Friend]§a " : "";
        String userText = user ? IRCModule.usersOnline.get(entity.getName()).getColored() + " " : "";
        String text = userText + friendText + murderText + detectiveText + entity.getDisplayName().getFormattedText();
        float offset = mc.fontRendererObj.FONT_HEIGHT - 8f;
        float stringWidth = mc.fontRendererObj.getStringWidth(text) / 2f;
        if (shadows.isToggled() && shadows.module.get("NameTags")) {
            BloomUtils.addToDraw(() -> Gui.drawRect(-stringWidth - 2, offset - 3, (-stringWidth - 2) + (stringWidth * 2 + 4), offset - 3 + mc.fontRendererObj.FONT_HEIGHT + 4, -1));
        }
        Gui.drawRect(-stringWidth - 2, offset - 3, (-stringWidth - 2) + (stringWidth * 2 + 4), offset - 3 + mc.fontRendererObj.FONT_HEIGHT + 4, color.getColor().getRGB());
        mc.fontRendererObj.drawString(text, -stringWidth, offset, -1, true);
        glColor4f(1f, 1f, 1f, 1f);
        glPopMatrix();
    }
}
