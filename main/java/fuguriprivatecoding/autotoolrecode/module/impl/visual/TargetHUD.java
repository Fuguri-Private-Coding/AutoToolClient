package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.TickEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.projection.Convertors;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

    IntegerSetting xPos = new IntegerSetting("X-Pos", this, () -> !follow.isToggled(), 0, 100, 0);
    IntegerSetting yPos = new IntegerSetting("Y-Pos", this, () -> !follow.isToggled(), 0, 100, 0);

    public final ColorSetting textColor = new ColorSetting("Text Color", this, () -> render.get("Name"));
    public final ColorSetting bgColor = new ColorSetting("Background Color", this, () -> render.get("Background"));

    public final CheckBox textShadow = new CheckBox("Text Shadow", this, () -> render.get("Name"));
    FloatSetting bgRadius = new FloatSetting("Background Radius", this, 0f, 20f, 10f, 0.1f);

    public final ColorSetting healthColor = new ColorSetting("Health Color", this, () -> render.get("Health"));

    FloatSetting headRadius = new FloatSetting("Head Radius", this, () -> render.get("Head"), 0f, 15f, 7.5f, 0.1f);

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

        if (target == null || target.getSkin() == null || target.getName() == null) return;

        if (event instanceof Render2DEvent e) {
            currentScale.update(2, Easing.OUT_BACK);

            ClientFontRenderer font = Fonts.fonts.get(fonts.getMode());

            float width = 120;
            float height = 40;

            if (follow.isToggled()) {
                EntityRenderer entityRenderer = mc.entityRenderer;

                entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
                float[] positions = Convertors.convert2D(
                    (float) (target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX),
                    (float) (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY) + target.height / 2f + yOffset.getValue(),
                    (float) (target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ), mc.gameSettings.guiScale);
                entityRenderer.setupOverlayRendering();

                if (positions == null || positions[2] > 1) return;

                float scale = this.scale.getValue() * currentScale.getValue();

                pos.set(
                    (positions[0] / scale) - width / 2f,
                    (positions[1] / scale) - height / 2f
                );

                renderHUD(pos.x, pos.y, width, height, bgRadius.getValue(), font, target, scale);
            } else {
                pos.set(
                    (e.getSc().getScaledWidth() / 100f) * xPos.getValue() / currentScale.getValue() - width / 2f,
                    (e.getSc().getScaledHeight() / 100f) * yPos.getValue() / currentScale.getValue() - height / 2f
                );

                double scale = this.scale.getValue() * currentScale.getValue();

                renderHUD(pos.x, pos.y, width, height, bgRadius.getValue(), font, target, scale);
            }
        }
    }

    private void renderHUD(float posX, float posY, float width, float height, float radius, ClientFontRenderer font, EntityLivingBase target, double scaleFactor) {
        glPushMatrix();
        glScaled(scaleFactor, scaleFactor, scaleFactor);

        if (render.get("Background")) RoundedUtils.drawRect(posX, posY, width, height, radius, bgColor.getFadedColor());
        StencilUtils.setUpTexture(posX, posY, width, height, radius);

        StencilUtils.writeTexture();
        if (render.get("Name")) font.drawString(target.getName(), posX + width / 2f + 18 - font.getStringWidth(target.getName()) / 2f, posY + height / 2f - 10f, textColor.getColor(), textColor.getFadeColor(), textColor.getOffset(), textColor.getSpeed(), textShadow.isToggled());

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
            RoundedUtils.drawRect(healthX, healthY, animatedHealthWidth, 10, 5, healthColor.getFadedColor());
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

        if (glow.isToggled()) BloomRealUtils.addToDraw(() -> RoundedUtils.drawRect(posX, posY, width, height, radius, bgShadowColor.getFadedColor()));
        if (blur.isToggled()) GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(posX, posY, width, height, radius, Color.WHITE));

        glPopMatrix();
    }

    private void updateTarget(GuiScreen screen) {
        EntityLivingBase currentTarget = TargetStorage.getTarget();
        if (screen instanceof GuiChat || screen instanceof ClickScreen) {
            currentScale.setEnd(1);
            target = mc.thePlayer;
        } else if (screen == null) {
            if (target == null && currentTarget != null) {
                target = currentTarget;
                currentScale.setEnd(1);
            } else if (target != null) {
                if (currentTarget == null) {
                    currentScale.setEnd(0);
                    if (!currentScale.isAnimating()) target = null;
                } else {
                    target = currentTarget;
                    currentScale.setEnd(1f);
                }
            }
        }
    }
}