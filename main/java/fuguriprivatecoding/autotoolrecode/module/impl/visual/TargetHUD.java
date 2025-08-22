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

    public final ColorSetting textColor = new ColorSetting("Text Color", this);

    CheckBox shadow = new CheckBox("Text Shadow", this, () -> render.get("Name"), true);

    public final ColorSetting bgColor = new ColorSetting("Background Color", this);

    public final ColorSetting healthColor = new ColorSetting("Health Color", this);

    public final ColorSetting healthBackColor = new ColorSetting("Health Back Color", this);

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
            healthFadeColor = healthColor.isFade()
                    ? ColorUtils.fadeColor(
                    healthColor.getColor(), healthColor.getFadeColor(),
                    healthColor.getSpeed()) : healthColor.getColor();

            healthBackFadeColor = healthBackColor.isFade()
                    ? ColorUtils.fadeColor(
                    healthBackColor.getColor(), healthBackColor.getFadeColor(),
                    healthBackColor.getSpeed()) : healthBackColor.getColor();
        }

        if (render.get("Background")) {
            backgroundFadeColor = bgColor.isFade()
                    ? ColorUtils.fadeColor(
                    bgColor.getColor(), bgColor.getFadeColor(),
                    bgColor.getSpeed()) : bgColor.getColor();
        }

        if (render.get("Name")) {
            textNameFadeColor = textColor.isFade()
                    ? ColorUtils.fadeColor(
                    textColor.getColor(), textColor.getFadeColor(),
                    textColor.getSpeed()) : textColor.getColor();
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