package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedGradUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
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
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

@ModuleInfo(name = "TargetHUD", category = Category.VISUAL, description = "Показывает информацию о противнике.")
public class TargetHUD extends Module {

    final MultiMode render = new MultiMode("Render", this)
        .addModes("Health", "Background", "Head", "Name");

    final Mode fonts = new Mode("Fonts", this, () -> render.get("Name"));

    final CheckBox follow = new CheckBox("Follow", this);
    final FloatSetting yOffset = new FloatSetting("YOffset", this, follow::isToggled, -2, 2, 0, 0.1f);

    final IntegerSetting xPos = new IntegerSetting("XPos", this, () -> !follow.isToggled(), 0, 100, 0);
    final IntegerSetting yPos = new IntegerSetting("YPos", this, () -> !follow.isToggled(), 0, 100, 0);

    final ColorSetting bgColor = new ColorSetting("BackgroundColor", this, () -> render.get("Background"));
    final FloatSetting bgRadius = new FloatSetting("BackgroundRadius", this, 0f, 20f, 10f, 0.1f);

    final ColorSetting textColor = new ColorSetting("TextColor", this, () -> render.get("Name"));
    final CheckBox textShadow = new CheckBox("TextShadow", this, () -> render.get("Name"));

    final ColorSetting healthColor = new ColorSetting("HealthColor", this, () -> render.get("Health"));

    final FloatSetting headRadius = new FloatSetting("HeadRadius", this, () -> render.get("Head"), 0f, 15f, 7.5f, 0.1f);
    final ColorSetting headHitColor = new ColorSetting("HeadHitColor", this, () -> render.get("Head"));

    final FloatSetting scale = new FloatSetting("Scale", this, 0.5f, 3f, 1.5f, 0.05f);

    final CheckBox glow = new CheckBox("Glow", this);
    final ColorSetting glowColor = new ColorSetting("GlowColor", this, glow::isToggled);

    final CheckBox blur = new CheckBox("Blur", this);

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

        if (event instanceof Render2DEvent) {
            currentScale.update(2, Easing.OUT_BACK);

            if (target == null || target.getSkin() == null || target.getName() == null) return;

            ClientFont font = Fonts.fonts.get(fonts.getMode());

            float width = 120;
            float height = 40;

            float scale = this.scale.getValue() * Math.clamp(currentScale.getValue(), 0, 2);

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

                pos.set(positions[0] - 60, positions[1]);
            } else {
                pos = GuiUtils.getAbsolutePos(xPos.getValue(), yPos.getValue());
            }
            renderHUD(pos.x, pos.y, width, height, font, target, scale, Math.clamp(currentScale.getValue(), 0, 1));
        }
    }

    private void renderHUD(float x, float y, float width, float height, ClientFont font, EntityLivingBase target, float scaleFactor, float alpha) {
        ScaleUtils.startScaling(x, y, width, height, scaleFactor);

        Color bgColor = new Colors(this.bgColor.getFadedColor()).withMultiplyAlphaClamp(alpha);

        if (render.get("Background")) {
            RoundedUtils.drawRect(x, y, width, height, bgRadius.getValue(), bgColor);
        }

        StencilUtils.setUpTexture(x, y, width, height, bgRadius.getValue());

        StencilUtils.writeTexture();

        if (render.get("Name")) {
            Color textColor = new Colors(this.textColor.getFadedColor()).withMultiplyAlphaClamp(alpha);
            font.drawString(target.getName(), x + width / 2f + 18 - font.getStringWidth(target.getName()) / 2f, y + height / 2f - 10f, textColor, textShadow.isToggled());
        }

        if (render.get("Health")) {
            float maxHealth = target.getMaxHealth();
            float currentHealth = target.getHealth();
            float healthPercentage = currentHealth / maxHealth;
            float maxHealthBarWidth = width - height - 5;
            float targetHealthWidth = maxHealthBarWidth * healthPercentage;

            if (healthAnimation.getEnd() != targetHealthWidth) healthAnimation.setEnd(targetHealthWidth);

            healthAnimation.update(7, Easing.LINEAR);

            float healthX = x + height;
            float healthY = y + height / 2f + 2;

            Color healthColorFirst = new Colors(healthColor.getColor()).withMultiplyAlphaClamp(alpha);
            Color healthColorSecond = new Colors(healthColor.getFadeColor()).withMultiplyAlphaClamp(alpha);

            RoundedUtils.drawRect(healthX, healthY, maxHealthBarWidth, 10, 5, bgColor);
            RoundedGradUtils.drawRect(healthX, healthY, healthAnimation.getValue(), 10, 5, healthColorFirst, healthColorSecond, false);
        }

        if (render.get("Head") && target instanceof EntityPlayer) {
            float hurtTime = target.hurtTime / 10f;

            float headX = x + 5 + hurtTime / 2f;
            float headY = y + 5 + hurtTime / 2f;
            float headWidth = height - 10 - hurtTime;
            float headHeight = height - 10 - hurtTime;

            Color headHitColor = new Colors(this.headHitColor.getFadedColor()).withMultiplyAlphaClamp(alpha);
            Color headColor = Colors.WHITE.withAlphaClamp(alpha);

            Color color = ColorUtils.interpolateColor(headColor, headHitColor, hurtTime);

            StencilUtils.setUpTexture(headX, headY, headWidth, headHeight, headRadius.getValue() * currentScale.getValue());
            StencilUtils.writeTexture();

            ColorUtils.glColor(color);
            RenderUtils.quickDrawHead(target.getSkin(), headX, headY, headWidth, headHeight);
            ColorUtils.resetColor();

            StencilUtils.endWriteTexture();
        }

        StencilUtils.endWriteTexture();

        if (glow.isToggled()) {
            Color glowColorFirst = new Colors(this.glowColor.getColor()).withMultiplyAlphaClamp(alpha);
            Color glowColorSecond = new Colors(this.glowColor.getFadeColor()).withMultiplyAlphaClamp(alpha);
            BloomUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(x, y, width, height, bgRadius.getValue(), glowColorFirst, glowColorSecond, glowColor.getSpeed()));
        }

        if (blur.isToggled()) BlurUtils.addToDraw(() -> RoundedUtils.drawRect(x, y, width, height, bgRadius.getValue(), Colors.WHITE.withAlpha(alpha)));

        ScaleUtils.stopScaling();
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