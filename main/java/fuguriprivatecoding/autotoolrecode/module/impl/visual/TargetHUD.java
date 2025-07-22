package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL, description = "Показывает информацию о противнике.")
public class TargetHUD extends Module {

    private final MultiMode render = new MultiMode("Render", this)
            .addModes("Health","Name","Background","Head");

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, -5f,5f,0f,0.1f);

    CheckBox fade = new CheckBox("Text Fade", this, () -> render.get("Name"), false);
    ColorSetting color1 = new ColorSetting("Text Color1", this, () -> render.get("Name"), 1,1,1,1);
    ColorSetting color2 = new ColorSetting("Text Color2", this,() -> render.get("Name") && fade.isToggled(), 0,0,0,1);
    FloatSetting speed = new FloatSetting("Text Speed", this,() -> render.get("Name") && fade.isToggled(),0.1f, 20, 1, 0.1f);
    CheckBox shadow = new CheckBox("Text Shadow", this, () -> render.get("Name"), true);

    CheckBox bgFade = new CheckBox("Background Fade", this, () -> render.get("Background"), false);
    ColorSetting bgColor1 = new ColorSetting("Background Color1", this, () -> render.get("Background"), 0,0,0,1);
    ColorSetting bgColor2 = new ColorSetting("Background Color2", this,() -> render.get("Background") && bgFade.isToggled(), 0,0,0,1);
    FloatSetting bgSpeed = new FloatSetting("Background Speed", this,() -> render.get("Background") && bgFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox healthFade = new CheckBox("Health Fade", this, () -> render.get("Health"), false);
    ColorSetting healthColor1 = new ColorSetting("Health Color1", this, () -> render.get("Health"), 1,0,0,1);
    ColorSetting healthColor2 = new ColorSetting("Health Color2", this,() -> render.get("Health") && healthFade.isToggled(), 0,0,0,1);
    FloatSetting healthSpeed = new FloatSetting("Health Speed", this,() -> render.get("Health") && healthFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox healthBackFade = new CheckBox("Health Back Fade", this, () -> render.get("Health"), false);
    ColorSetting healthBackColor1 = new ColorSetting("Health Back Color1", this, () -> render.get("Health"), 0,0,0,1);
    ColorSetting healthBackColor2 = new ColorSetting("Health Back Color2", this,() -> render.get("Health") && healthBackFade.isToggled(), 0,0,0,1);
    FloatSetting healthBackSpeed = new FloatSetting("Health Back Speed", this,() -> render.get("Health") && healthBackFade.isToggled(),0.1f, 20, 1, 0.1f);

    CheckBox headRound = new CheckBox("Rounded Head", this, () -> render.get("Head"), true);
    FloatSetting headRoundFactor = new FloatSetting("Head Round Factor", this, () -> render.get("Head"), 0f,50f,10f,0.1f);

    private final IntegerSetting scale = new IntegerSetting("Scale", this, 1, 10, 5);

    private final Animation2D healthAnimation = new Animation2D();
    private Glow shadows;

    Color textNameFadeColor;
    Color backgroundFadeColor;
    Color healthFadeColor;
    Color healthBackFadeColor;

    private static final float TEXTURE_WIDTH = 400f;
    private static final float TEXTURE_HEIGHT = 150f;
    private static final float HEAD_SIZE = 125f;
    private static final float PADDING = 20f;
    private static final float HEALTH_BAR_HEIGHT = 50f;
    private static final float CORNER_RADIUS = 20f;
    private static final float TEXT_SCALE = 3f;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Glow.class);
        if (event instanceof Render3DEvent && mc.currentScreen == null) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();
            if (target == null || target.getName() == null || target.getSkin() == null) return;

            updateColors();

            Vec3 pos = calculateEntityPosition(target);

            setupRendering(pos, target.height, yOffset.getValue());

            try {
                renderHUD(target);
            } finally {
                cleanupRendering();
            }
        }
    }

    private void updateColors() {
        if (render.get("Health")) {
            healthFadeColor = healthFade.isToggled()
                    ? ColorUtils.fadeColor(
                    healthColor1.getColor(), healthColor2.getColor(),
                    healthSpeed.getValue()) : healthColor1.getColor();

            healthBackFadeColor = healthBackFade.isToggled()
                    ? ColorUtils.fadeColor(
                    healthBackColor1.getColor(), healthBackColor2.getColor(),
                    healthBackSpeed.getValue()) : healthBackColor1.getColor();
        }

        if (render.get("Background")) {
            backgroundFadeColor = bgFade.isToggled()
                    ? ColorUtils.fadeColor(
                    bgColor1.getColor(), bgColor2.getColor(),
                    bgSpeed.getValue()) : bgColor1.getColor();
        }

        if (render.get("Name")) {
            textNameFadeColor = fade.isToggled()
                    ? ColorUtils.fadeColor(
                    color1.getColor(), color2.getColor(),
                    speed.getValue()) : color1.getColor();
        }
    }

    private Vec3 calculateEntityPosition(EntityLivingBase target) {
        return new Vec3(
                target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY,
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ
        );
    }

    private void setupRendering(Vec3 pos, float height, float offset) {
        glPushMatrix();
        glTranslatef((float)pos.xCoord, (float)pos.yCoord + height / 2f + offset, (float)pos.zCoord);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);

        double scaleFactor = this.scale.getValue() / 1000D;
        glScaled(-scaleFactor, -scaleFactor, scaleFactor);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderHUD(EntityLivingBase target) {
        float hurtTime = target.hurtTime / 10f;
        float maxHealth = target.getMaxHealth();
        float currentHealth = target.getHealth();
        float healthPercentage = currentHealth / maxHealth;

        if (shadows != null && shadows.isToggled() && shadows.module.get("TargetHUD") && render.get("Background")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f, -TEXTURE_HEIGHT / 2f, TEXTURE_WIDTH, TEXTURE_HEIGHT, CORNER_RADIUS, Color.WHITE));
        }

        if (render.get("Background")) RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f, -TEXTURE_HEIGHT / 2f, TEXTURE_WIDTH, TEXTURE_HEIGHT, CORNER_RADIUS, backgroundFadeColor);

        if (target instanceof EntityPlayer && render.get("Head")) renderPlayerHead((EntityPlayer)target, hurtTime, headRoundFactor.getValue(), headRound.isToggled());
        if (render.get("Name")) renderPlayerName(target);

        if (render.get("Health")) {
            updateHealthAnimation(healthPercentage);
            renderHealthBar();
        }
    }

    private void updateHealthAnimation(float healthPercentage) {
        float maxHealthBarWidth = TEXTURE_WIDTH - 150f - 7.5f * 2;
        float targetHealthWidth = maxHealthBarWidth * healthPercentage;

        if (healthAnimation.endX != targetHealthWidth) {
            healthAnimation.endX = targetHealthWidth;
            healthAnimation.reset();
        }
        healthAnimation.update(20f);
    }

    private void renderPlayerHead(EntityPlayer player, float hurtTime, float roundFactor, boolean round) {
        if (roundFactor > 0 && round) {
            StencilUtils.renderStencil(
                    () -> RoundedUtils.drawRect((-TEXTURE_WIDTH / 2f + PADDING), (-TEXTURE_HEIGHT / 2f + PADDING), HEAD_SIZE, (TEXTURE_HEIGHT - 2 * PADDING), roundFactor, Color.white),
                    () -> {
                        glColor4f(1f, 1f - hurtTime, 1f - hurtTime, 1f);
                        RenderUtils.quickDrawHead(player.getSkin(), (int) (-TEXTURE_WIDTH / 2f + PADDING), (int) (-TEXTURE_HEIGHT / 2f + PADDING), (int) HEAD_SIZE, (int) (TEXTURE_HEIGHT - 2 * PADDING));
                    });
        } else {
            glColor4f(1f, 1f - hurtTime, 1f - hurtTime, 1f);
            RenderUtils.quickDrawHead(player.getSkin(), (int) (-TEXTURE_WIDTH / 2f + PADDING), (int) (-TEXTURE_HEIGHT / 2f + PADDING), (int) HEAD_SIZE, (int) (TEXTURE_HEIGHT - 2 * PADDING));
        }
    }

    private void renderPlayerName(EntityLivingBase target) {
        float textYOffset = -TEXTURE_HEIGHT / 2f + 30f;
        glPushMatrix();
        glScalef(TEXT_SCALE,TEXT_SCALE,TEXT_SCALE);
        mc.fontRendererObj.drawString(target.getName(), (int)((-TEXTURE_WIDTH / 2f + 135f + PADDING) + 200 / TEXT_SCALE - mc.fontRendererObj.getStringWidth(target.getName()) / 2f), (int)(textYOffset / TEXT_SCALE), textNameFadeColor.getRGB(), shadow.isToggled());
        glPopMatrix();
    }

    private void renderHealthBar() {
        float maxHealthBarWidth = TEXTURE_WIDTH - 150f - 7.5f * 2;
        float animatedHealthWidth = healthAnimation.x;

        Color healthColor = healthFadeColor;
        Color healthBackground = healthBackFadeColor;

        RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f + 150f + 5f, -TEXTURE_HEIGHT / 2f + TEXTURE_HEIGHT / 2f, maxHealthBarWidth, HEALTH_BAR_HEIGHT, CORNER_RADIUS, healthBackground);
        RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f + 150f + 5f, -TEXTURE_HEIGHT / 2f + TEXTURE_HEIGHT / 2f, animatedHealthWidth, HEALTH_BAR_HEIGHT, CORNER_RADIUS, healthColor);
    }

    private void cleanupRendering() {
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glPopMatrix();
        ColorUtils.resetColor();
    }
}