package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
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
import net.minecraft.util.Vec3;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, description = "Отображение никнейма игроков.")
public class NameTags extends Module {

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, 0f,5f,1f,0.1f);

    public final ColorSetting color = new ColorSetting("Color", this);

    MurderMystery murderDetector;
    MidClick midClick;
    Glow shadows;

    String detectiveText, murderText, friendText, userText, text;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (murderDetector == null) murderDetector = Client.INST.getModuleManager().getModule(MurderMystery.class);
        if (midClick == null) midClick = Client.INST.getModuleManager().getModule(MidClick.class);
        if (event instanceof Render3DEvent) {
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue;
                updateText(entity);

                Vec3 pos = calculateTranslatedPos(entity);

                RenderUtils.start3DNameTag();
                setRendering(entity, pos, this::render);
                RenderUtils.stop3DNameTag();
            }
        }
    }

    public void updateText(Entity entity) {
        boolean friend = entity instanceof EntityPlayer ent && midClick.showInName.isToggled() && ent.isFriend();
        boolean murder = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.murders.contains(ent.getName());
        boolean detective = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.detectives.contains(ent.getName());
        boolean user = entity instanceof EntityPlayer ent && IRC.usersOnline.get(ent.getName()) != null;
        detectiveText = detective ? "§6[Detective]§6 " : "";
        murderText = murder ? "§4[Murder]§4 " : "";
        friendText = friend ? "§2[Friend]§a " : "";
        userText = user ? IRC.usersOnline.get(entity.getName()).toColoredString() + " " : "";
        text = userText + friendText + murderText + detectiveText + entity.getDisplayName().getFormattedText();
    }

    private Vec3 calculateTranslatedPos(Entity entity) {
        return new Vec3(
                (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX),
                (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY + entity.getEyeHeight() + yOffset.getValue()),
                (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ)
        );
    }

    public void render() {
        float offset = mc.fontRendererObj.FONT_HEIGHT - 8f;
        float stringWidth = mc.fontRendererObj.getStringWidth(text) / 2f;
        if (shadows.isToggled() && shadows.module.get("NameTags")) BloomUtils.addToDraw(() -> Gui.drawRect(-stringWidth - 2, offset - 3, (-stringWidth - 2) + (stringWidth * 2 + 4), offset - 3 + mc.fontRendererObj.FONT_HEIGHT + 4, -1));
        Gui.drawRect(-stringWidth - 2, offset - 3, (-stringWidth - 2) + (stringWidth * 2 + 4), offset - 3 + mc.fontRendererObj.FONT_HEIGHT + 4, color.getFadedColor().getRGB());
        mc.fontRendererObj.drawString(text, -stringWidth, offset, -1, true);
    }

    private void setRendering(Entity entity, Vec3 pos, Runnable render) {
        float distance = mc.thePlayer.getDistanceToEntity(entity);
        float scale = Math.max(distance / 2.5f, 5.0f);
        scale /= 200f;
        glPushMatrix();
        glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        glScalef(-scale, -scale, scale);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        render.run();
        glColor4f(1f, 1f, 1f, 1f);
        glPopMatrix();
    }
}
