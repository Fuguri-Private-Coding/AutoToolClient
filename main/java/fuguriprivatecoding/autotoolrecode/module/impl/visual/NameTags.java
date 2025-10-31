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
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.Mode;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, description = "Отображение никнейма игроков.")
public class NameTags extends Module {

    Mode fonts = new Mode("Fonts", this);

    FloatSetting yOffset = new FloatSetting("YOffset", this, 0f,5f,1f,0.1f);
    FloatSetting textYOffset = new FloatSetting("TextYOffset", this, -5,5f,0f,0.01f);

    FloatSetting radius = new FloatSetting("Radius", this, 0,10,0,0.1f);

    public final ColorSetting color = new ColorSetting("Color", this);
    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    MurderMystery murderDetector;
    MidClick midClick;

    public NameTags() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

    @EventTarget
    public void onEvent(Event event) {
        if (murderDetector == null) murderDetector = Client.INST.getModules().getModule(MurderMystery.class);
        if (midClick == null) midClick = Client.INST.getModules().getModule(MidClick.class);

        if (event instanceof Render3DEvent) {
            RenderUtils.start3DNameTag();
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                Vec3 pos = calculateTranslatedPos(entity);

                renderNameTag(entity, getText(entity), pos);
            }
            RenderUtils.stop3DNameTag();
        }
    }

    private String getText(Entity entity) {
        boolean friend = entity instanceof EntityPlayer ent && midClick.showInName.isToggled() && ent.isFriend();
        boolean murder = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.murders.contains(ent.getName());
        boolean detective = entity instanceof EntityPlayer ent && murderDetector.isToggled() && murderDetector.detectives.contains(ent.getName());
        boolean user = entity instanceof EntityPlayer ent && IRC.usersOnline.get(ent.getName()) != null;

        String detectiveText = detective ? "§6[Detective]§6 " : "";
        String murderText = murder ? "§4[Murder]§4 " : "";
        String friendText = friend ? "§2[Friend]§a " : "";
        String userText = user ? IRC.usersOnline.get(entity.getName()).toColoredString() + " " : "";

        return detectiveText + murderText + friendText + userText + entity.getDisplayName().getFormattedText();
    }

    private void renderNameTag(Entity entity, String name, Vec3 pos) {
        ClientFontRenderer fontRenderer = Fonts.fonts.get(fonts.getMode());

        float distance = mc.thePlayer.getDistanceToEntity(entity);
        float scale = Math.max(distance / 2.5f, 5.0f);
        scale /= 200f;
        glPushMatrix();
        glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        glScalef(-scale, -scale, scale);

        float nameWidth = (float) fontRenderer.getStringWidth(name);

        float backgroundX = -nameWidth / 2f - 2.5f;
        float backgroundY = 0;
        float backgroundWidth = nameWidth + 5;
        float backgroundHeight = fontRenderer.FONT_HEIGHT + 4;

        RoundedUtils.drawRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight, radius.getValue(), color.getFadedColor());

        fontRenderer.drawString(name, backgroundX + backgroundWidth / 2f - nameWidth / 2f + 1.25f, backgroundY + 5 + textYOffset.getValue(), Color.WHITE);

        if (glow.isToggled()) {
            BloomRealUtils.addToDraw(() -> RoundedUtils.drawRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight, radius.getValue(), glowColor.getFadedColor()));
        }

        glPopMatrix();
    }

    private Vec3 calculateTranslatedPos(Entity entity) {
        return new Vec3(
                (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX),
                (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY + entity.getEyeHeight() + yOffset.getValue()),
                (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ)
        );
    }
}
