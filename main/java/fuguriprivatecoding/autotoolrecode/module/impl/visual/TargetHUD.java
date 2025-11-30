package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedGradUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.render.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL, description = "Показывает информацию о противнике.")
public class TargetHUD extends Module {

    MultiMode render = new MultiMode("Render", this)
        .addModes("Health", "Background", "Head", "Name");

    Mode fonts = new Mode("Fonts", this, () -> render.get("Name"));

    CheckBox follow = new CheckBox("Follow", this);

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, follow::isToggled, -2, 2, 0, 0.1f);

    IntegerSetting xPos = new IntegerSetting("XPos", this, () -> !follow.isToggled(), 0, 100, 0);
    IntegerSetting yPos = new IntegerSetting("YPos", this, () -> !follow.isToggled(), 0, 100, 0);

    public final ColorSetting textColor = new ColorSetting("TextColor", this, () -> render.get("Name"));
    public final ColorSetting bgColor = new ColorSetting("BackgroundColor", this, () -> render.get("Background"));

    public final CheckBox textShadow = new CheckBox("TextShadow", this, () -> render.get("Name"));
    FloatSetting bgRadius = new FloatSetting("BackgroundRadius", this, 0f, 20f, 10f, 0.1f);

    public final ColorSetting healthColor = new ColorSetting("HealthColor", this, () -> render.get("Health"));

    FloatSetting headRadius = new FloatSetting("HeadRadius", this, () -> render.get("Head"), 0f, 15f, 7.5f, 0.1f);

    private final FloatSetting scale = new FloatSetting("Scale", this, 0.5f, 3f, 1.5f, 0.05f);

    CheckBox glow = new CheckBox("Glow", this);
    CheckBox blur = new CheckBox("Blur", this);

    public final ColorSetting bgShadowColor = new ColorSetting("Background Glow Color", this, () -> glow.isToggled());

    EntityLivingBase target;

    EasingAnimation currentScale = new EasingAnimation();
    EasingAnimation healthAnimation = new EasingAnimation();

    Vector2f pos = new Vector2f();

    public TargetHUD() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TickEvent) {
            updateTarget(mc.currentScreen);
        }

        if (event instanceof Render2DEvent e) {
            currentScale.update(2, Easing.OUT_BACK);

            if (target == null || target.getSkin() == null || target.getName() == null) return;

            ClientFontRenderer font = Fonts.fonts.get(fonts.getMode());

            float width = 120;
            float height = 40;

            double scale = this.scale.getValue() * Math.clamp(currentScale.getValue(), 0, 2);

            if (follow.isToggled()) {
                mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                Vec3 entityPos = new Vec3(
                    (float) (target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX),
                    (float) (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY) + target.height / 2f + yOffset.getValue(),
                    (float) (target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ)
                );

                float[] positions = Convertors.convert2D(entityPos, mc.gameSettings.guiScale);
                mc.entityRenderer.setupOverlayRendering();

                if (positions == null || positions[2] > 1) return;

                pos.set(
                    positions[0] - 60,
                    positions[1]
                );
            } else {
                pos.set(
                    (e.getSc().getScaledWidth() / 100f) * xPos.getValue(),
                    (e.getSc().getScaledHeight() / 100f) * yPos.getValue()
                );
            }
            renderHUD(pos.x, pos.y, width, height, bgRadius.getValue(), font, target, scale);
        }
    }

    private void renderHUD(float posX, float posY, float width, float height, float radius, ClientFontRenderer font, EntityLivingBase target, double scaleFactor) {
        glPushMatrix();
        double centerX = posX + width / 2.0;
        double centerY = posY + height / 2.0;

        double offsetX = centerX * (1 - scaleFactor);
        double offsetY = centerY * (1 - scaleFactor);

        glTranslated(offsetX, offsetY, 0);
        glScaled(scaleFactor, scaleFactor, 1);

        if (render.get("Background")) RoundedUtils.drawRect(posX, posY, width, height, radius, bgColor.getFadedColor());
        StencilUtils.setUpTexture(posX, posY, width, height, radius);

        StencilUtils.writeTexture();
        if (render.get("Name")) font.drawString(target.getName(), posX + width / 2f + 18 - font.getStringWidth(target.getName()) / 2f, posY + height / 2f - 10f, textColor.getFadedColor(), textShadow.isToggled());

        if (render.get("Health")) {
            float maxHealth = target.getMaxHealth();
            float currentHealth = target.getHealth();
            float healthPercentage = currentHealth / maxHealth;
            float maxHealthBarWidth = width - height - 5;
            float targetHealthWidth = maxHealthBarWidth * healthPercentage;

            if (healthAnimation.getEnd() != targetHealthWidth) healthAnimation.setEnd(targetHealthWidth);

            healthAnimation.update(7, Easing.LINEAR);

            float animatedHealthWidth = healthAnimation.getValue();

            float healthX = posX + height;
            float healthY = posY + height / 2f + 2;

            RoundedUtils.drawRect(healthX, healthY, maxHealthBarWidth, 10, 5, bgColor.getFadedColor());
            RoundedGradUtils.drawRect(healthX, healthY, animatedHealthWidth, 10, 5, healthColor.getColor(), healthColor.getFadeColor(), false);
        }

        if (render.get("Head") && target instanceof EntityPlayer) {
            float hurtTime = target.hurtTime / 10f;

            float headX = posX + 5 + hurtTime / 2f;
            float headY = posY + 5 + hurtTime / 2f;
            float headWidth = height - 10 - hurtTime;
            float headHeight = height - 10 - hurtTime;

            StencilUtils.setUpTexture(headX, headY, headWidth, headHeight, headRadius.getValue() * currentScale.getValue());
            StencilUtils.writeTexture();
            glColor4f(1f / hurtTime, 1f - hurtTime, 1f - hurtTime, 1f);
            RenderUtils.quickDrawHead(target.getSkin(), headX, headY, headWidth, headHeight);
            ColorUtils.resetColor();
            StencilUtils.endWriteTexture();
        }

        StencilUtils.endWriteTexture();

        if (glow.isToggled()) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(posX, posY, width, height, radius, bgShadowColor.getFadedColor()));
        if (blur.isToggled()) BlurUtils.addToDraw(() -> RoundedUtils.drawRect(posX, posY, width, height, radius, Color.WHITE));

        glPopMatrix();
    }

    private void updateTarget(GuiScreen screen) {
        EntityLivingBase currentTarget = TargetStorage.getTarget();

        if (screen instanceof GuiChat || screen instanceof ClickScreen) {
            currentTarget = mc.thePlayer;
            target = currentTarget;
        } else if (screen == null) {

            if (currentTarget == null) {
                if (currentScale.getValue() == 0) target = null;
            } else {
                target = currentTarget;
            }
        }

        currentScale.setEnd(currentTarget != null);
    }
}