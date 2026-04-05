package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.client.IRC;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.MidClick;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.MurderMystery;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.CameraRot;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "NameTags", category = Category.VISUAL, description = "Отображение никнейма игроков.")
public class NameTags extends Module {

    FloatSetting yOffset = new FloatSetting("YOffset", this, 0f,5f,1f,0.1f);
    FloatSetting textYOffset = new FloatSetting("TextYOffset", this, -5,5f,0f,0.01f);

    private final FloatSetting scaleFactor = new FloatSetting("ScaleFactor", this, 0.1f, 1.5f, 0.7f, 0.1f);
    private final CheckBox background = new CheckBox("Background", this, false);
    public final ColorSetting backgroundColor = new ColorSetting("BackgroundColor", this, background::isToggled);
    public final CheckBox textShadow = new CheckBox("TextShadow", this);

    public final CheckBox renderAllNames = new CheckBox("RenderAllNames", this, false);

    private final IntegerSetting maxDistanceToRender = new IntegerSetting("MaxDistanceToRender", this,0, 256, 256);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this);

    MurderMystery murderDetector;
    MidClick midClick;

    @Override
    public void onEvent(Event event) {
        if (murderDetector == null) murderDetector = Modules.getModule(MurderMystery.class);
        if (midClick == null) midClick = Modules.getModule(MidClick.class);

        if (event instanceof Render3DEvent) {
            RenderUtils.start3D();
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (mc.getRenderManager() == null || (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || entity.isDead || DistanceUtils.getDistance(entity) > maxDistanceToRender.getValue())
                    continue;

                Vec3 pos = RenderUtils.getAbsoluteSmoothPos(entity.getLastPositionVector(), entity.getPositionVector()).subtract(RenderManager.getRenderPosition());;
                Vec3 addPos = new Vec3(0, entity.getEyeHeight() + yOffset.getValue(), 0);

                renderNameTag(getText(entity), pos.add(addPos));
            }
            RenderUtils.stop3D();
        }
    }

    private String getText(EntityPlayer ent) {
        boolean friend = midClick.showInName.isToggled() && ent.isFriend();
        boolean murder = MurderMystery.isMurder(ent);
        boolean detective = MurderMystery.isDetective(ent);
        boolean user = IRC.isClientUser(ent);

        String detectiveText = detective ? "§6[Detective]§6 " : "";
        String murderText = murder ? "§4[Murder]§4 " : "";
        String friendText = friend ? "§2[Friend]§a " : "";
        String userText = user ? IRC.usersOnline.get(ent.getName()).toColoredString() + " " : "";

        return detectiveText + murderText + friendText + userText + ent.getDisplayName().getFormattedText();
    }

    private void renderNameTag(String name, Vec3 pos) {
        Vec3 playerPos = RenderUtils.getAbsoluteSmoothPos(mc.thePlayer.getLastPositionVector(), mc.thePlayer.getPositionVector()).subtract(RenderManager.getRenderPosition());

        float distance = (float) playerPos.distanceTo(pos);
        float scale = Math.max(distance * scaleFactor.getValue(), 5.0f);
        scale /= 200f;
        glPushMatrix();
        glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glRotatef(-CameraRot.INST.getYaw(), 0.0f, 1.0f, 0.0f);
        glRotatef(CameraRot.INST.getPitch(), 1.0f, 0.0f, 0.0f);
        glScalef(mc.gameSettings.thirdPersonView == 2 ? scale : -scale, -scale, scale);

        float nameWidth = mc.fontRendererObj.getStringWidth(name);

        float backgroundX = -nameWidth / 2f - 2.5f;
        float backgroundY = 0;
        float backgroundWidth = nameWidth + 5;
        float backgroundHeight = mc.fontRendererObj.FONT_HEIGHT + 4;

        Gui.drawRect(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, backgroundColor.getFadedColor().getRGB());

        mc.fontRendererObj.drawString(name, backgroundX + backgroundWidth / 2f - nameWidth / 2f + 1.25f, backgroundY + 3 + textYOffset.getValue(), Color.white.getRGB(), textShadow.isToggled());

        if (glow.isToggled()) {
            BloomUtils.startWrite();
            if (background.isToggled()) {
                Gui.drawRect(backgroundX, backgroundY, backgroundX + backgroundWidth, backgroundY + backgroundHeight, glowColor.getFadedColor().getRGB());
            } else {
                mc.fontRendererObj.drawString(name, backgroundX + backgroundWidth / 2f - nameWidth / 2f + 1.25f, backgroundY + 3 + textYOffset.getValue(), Color.white.getRGB(), textShadow.isToggled());
            }
            BloomUtils.stopWrite();
        }

        glPopMatrix();
    }
}
