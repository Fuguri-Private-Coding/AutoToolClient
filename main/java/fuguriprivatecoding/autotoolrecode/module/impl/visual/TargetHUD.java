package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.animation.Animation2D;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL)
public class TargetHUD extends Module {

    private final MultiMode render = new MultiMode("Render", this)
            .addModes("RenderHealth","RenderName","RenderBackground","RenderHead");

    private final IntegerSetting scale = new IntegerSetting("Scale", this, 1, 10, 5);

    private static final float TEXTURE_WIDTH = 400f;
    private static final float TEXTURE_HEIGHT = 150f;
    private static final float HEAD_SIZE = 125f;
    private static final float PADDING = 20f;
    private static final float HEALTH_BAR_HEIGHT = 50f;
    private static final float CORNER_RADIUS = 20f;
    private static final float TEXT_SCALE = 2f;

    private final Animation2D healthAnimation = new Animation2D();
    private Shadows shadows;

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render3DEvent && mc.currentScreen == null) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();
            if (target == null || target.getName() == null || target.getSkin() == null) return;

            double[] pos = calculateEntityPosition(target);

            setupRendering(pos[0], pos[1], pos[2], target.height);

            try {
                renderHUD(target);
            } finally {
                cleanupRendering();
            }
        }
    }

    private double[] calculateEntityPosition(EntityLivingBase target) {
        return new double[] {
                target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY,
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ
        };
    }

    private void setupRendering(double x, double y, double z, float height) {
        glPushMatrix();
        glTranslatef((float)x, (float)y + height / 2f, (float)z);
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

        if (shadows != null && shadows.isToggled() && shadows.module.get("TargetHUD")) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f, -TEXTURE_HEIGHT / 2f, TEXTURE_WIDTH, TEXTURE_HEIGHT, CORNER_RADIUS, Color.WHITE));
        }

        if (render.get("RenderBackground")) RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f, -TEXTURE_HEIGHT / 2f, TEXTURE_WIDTH, TEXTURE_HEIGHT, CORNER_RADIUS, new Color(0, 0, 0, 150));

        if (target instanceof EntityPlayer && render.get("RenderHead")) renderPlayerHead((EntityPlayer)target, hurtTime);
        if (render.get("RenderName")) renderPlayerName(target);

        if (render.get("RenderHealth")) {
            updateHealthAnimation(healthPercentage);
            renderHealthBar(hurtTime, healthPercentage);
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

    private void renderPlayerHead(EntityPlayer player, float hurtTime) {
        StencilUtils.renderStencil(
                () -> RoundedUtils.drawRect((-TEXTURE_WIDTH / 2f + PADDING), (-TEXTURE_HEIGHT / 2f + PADDING), HEAD_SIZE, (TEXTURE_HEIGHT - 2 * PADDING), 10f, Color.white),
                () -> {
            glColor4f(1f, 1f - hurtTime, 1f - hurtTime, 1f);
            RenderUtils.quickDrawHead(player.getSkin(), (int) (-TEXTURE_WIDTH / 2f + PADDING), (int) (-TEXTURE_HEIGHT / 2f + PADDING), (int) HEAD_SIZE, (int) (TEXTURE_HEIGHT - 2 * PADDING));
        });
    }

    private void renderPlayerName(EntityLivingBase target) {
        float textYOffset = -TEXTURE_HEIGHT / 2f + 15f + 25f;
        glPushMatrix();
        glScalef(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE);
        mc.fontRendererObj.drawStringWithShadow(target.getName(), (int)((-TEXTURE_WIDTH / 2f + 150f + PADDING) / TEXT_SCALE), (int)(textYOffset / TEXT_SCALE), -1);
        glPopMatrix();
    }

    private void renderHealthBar(float hurtTime, float healthPercentage) {
        float maxHealthBarWidth = TEXTURE_WIDTH - 150f - 7.5f * 2;
        float targetHealthWidth = maxHealthBarWidth * healthPercentage;
        float animatedHealthWidth = healthAnimation.x;

        Color healthColor = new Color((int)(255 * (1.0f - hurtTime * 0.5f)), (int)(50 * (1.0f - hurtTime)), 50);
        Color healthBackground = new Color(50, 50, 50, 150);
        Color healthDamageIndicator = new Color(200, 50, 50, 150);

        RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f + 150f + 5f, -TEXTURE_HEIGHT / 2f + TEXTURE_HEIGHT / 2f, maxHealthBarWidth, HEALTH_BAR_HEIGHT, CORNER_RADIUS, healthBackground);
        RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f + 150f + 5f, -TEXTURE_HEIGHT / 2f + TEXTURE_HEIGHT / 2f, animatedHealthWidth, HEALTH_BAR_HEIGHT, CORNER_RADIUS, healthColor);
        if (animatedHealthWidth < targetHealthWidth) RoundedUtils.drawRect(-TEXTURE_WIDTH / 2f + 150f + 7.5f + animatedHealthWidth, -TEXTURE_HEIGHT / 2f + TEXTURE_HEIGHT / 2f, targetHealthWidth - animatedHealthWidth, HEALTH_BAR_HEIGHT, CORNER_RADIUS, healthDamageIndicator);
    }

    private void cleanupRendering() {
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glPopMatrix();
        ColorUtils.resetColor();
    }
}