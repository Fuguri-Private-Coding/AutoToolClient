package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connection.BackTrack;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    Mode fonts = new Mode("Fonts", this)
            .addModes("MuseoSans", "Roboto", "JetBrains", "SFPro")
            .setMode("MuseoSans");

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, 0, 100, 5, 0.1f);

    FloatSetting width = new FloatSetting("Width", this, 0f, 50f, 10f, 0.1f);
    FloatSetting animationSpeed = new FloatSetting("Animation Speed", this, 0f, 10f, 5f, 0.1f);

    public final ColorSetting textColor = new ColorSetting("Text Color", this);

    FloatSetting bgRadius = new FloatSetting("Background Radius", this, 0f, 20, 7.5f, 0.1f);
    public final ColorSetting bgColor = new ColorSetting("Background Color", this);

    CheckBox glow = new CheckBox("Glow", this, true);
    CheckBox blur = new CheckBox("Blur", this, true);

    public final ColorSetting bgColorShadow = new ColorSetting("Background Shadow Color", this, () -> glow.isToggled());

    Color fadeTextColor;
    Color fadeBackgroundColor;

    EasingAnimation currentWidth = new EasingAnimation();
    EasingAnimation currentHeight = new EasingAnimation();
    EasingAnimation needY = new EasingAnimation();
    EasingAnimation radiusAnim = new EasingAnimation();

    String currentText;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ClientFontRenderer font = Client.INST.getFonts().fonts.get(fonts.getMode());
            Vector2f screenSize = new Vector2f(e.getSc().getScaledWidth(), e.getSc().getScaledHeight());

            updateColors();

            float height = 15;
            float yOffsetValue = this.yOffset.getValue();

            EntityLivingBase ent = Client.INST.getCombatManager().getTarget();
            BackTrack backTrack = Client.INST.getModuleManager().getModule(BackTrack.class);
            Scaffold scaffOld = Client.INST.getModuleManager().getModule(Scaffold.class);

            String name = Client.INST.getFullName();
            String bps = String.format("%.3f", mc.thePlayer.getBps(false));
            String target = "Current Target - Undefined";
            String scaffold = "Block Left - Undefined";
            String backtrack = "Distance - Undefined";

            currentHeight.setEnd(height);
            currentText = name;
            needY.setEnd(yOffsetValue);

            if (MoveUtils.isMoving()) {
                currentText = bps;
                resetNeedY(15, yOffsetValue);
            }

            if (ent != null) {
                target = "Current Target - " + ent.getName();
                currentText = target;
                resetNeedY(30, yOffsetValue);
            }

            if (scaffOld.getBlockCount() > 0 && scaffOld.isToggled()) {
                scaffold = "Blocks Left - " + scaffOld.getBlockCount();
                currentText = scaffold;
                resetNeedY(45, yOffsetValue);
            }

            if (backTrack.isToggled() && backTrack.packetBuffer.size() > 10 && ent != null) {
                AxisAlignedBB realBox = ent.getEntityBoundingBox().offset(ent.nx - ent.posX, ent.ny - ent.posY, ent.nz - ent.posZ).expand(
                        ent.getCollisionBorderSize(),
                        ent.getCollisionBorderSize(),
                        ent.getCollisionBorderSize()
                );

                backtrack = "Distance - " + String.format("%.1f", DistanceUtils.getDistance(realBox));
                currentText = backtrack;
                resetNeedY(60, yOffsetValue);
            }

            currentWidth.setEnd((float) (width.getValue() + font.getStringWidth(currentText)));

            updateAnimations();

            String finalTarget = target;
            String finalScaffold = scaffold;
            String finalBacktrack = backtrack;

            StencilUtils.renderStencil(
                    () -> RenderUtils.drawMixedRoundedRect(
                            screenSize.x / 2f - currentWidth.getValue() / 2f,
                            4.5f + yOffsetValue,
                            currentWidth.getValue(),
                            currentHeight.getValue(),
                            radiusAnim.getValue(),
                            Color.BLACK,
                            Color.BLACK,
                            1f
                    ),
                    () -> {
                        RenderUtils.drawMixedRoundedRect(
                                screenSize.x / 2f - currentWidth.getValue() / 2f,
                                4.5f + yOffsetValue,
                                currentWidth.getValue(),
                                currentHeight.getValue(),
                                radiusAnim.getValue(),
                                fadeBackgroundColor,
                                fadeBackgroundColor,
                                bgColor.getSpeed()
                        );

                        font.drawString(name, (screenSize.x / 2f - font.getStringWidth(name) / 2f), 10 + needY.getValue(), fadeTextColor);
                        font.drawString(bps, (screenSize.x / 2f - font.getStringWidth(bps) / 2f), 25 + needY.getValue(), fadeTextColor);
                        font.drawString(finalTarget, (screenSize.x / 2f - font.getStringWidth(finalTarget) / 2f), 40 + needY.getValue(), fadeTextColor);
                        font.drawString(finalScaffold, (screenSize.x / 2f - font.getStringWidth(finalScaffold) / 2f), 55 + needY.getValue(), fadeTextColor);
                        font.drawString(finalBacktrack, (screenSize.x / 2f - font.getStringWidth(finalBacktrack) / 2f), 70 + needY.getValue(), fadeTextColor);
                    }
            );

            if (glow.isToggled()) {
                BloomRealUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.getValue() / 2f, 4.5f + yOffsetValue, currentWidth.getValue(), height, radiusAnim.getValue(), bgColorShadow.getColor(), bgColorShadow.getFadeColor(), bgColorShadow.getSpeed()));
            }

            if (blur.isToggled()) {
                GaussianBlurUtils.addToDraw(() -> RenderUtils.drawMixedRoundedRect(screenSize.x / 2f - currentWidth.getValue() / 2f, 4.5f + yOffsetValue, currentWidth.getValue(), height, radiusAnim.getValue(), Color.WHITE, Color.WHITE, bgColorShadow.getSpeed()));
            }
        }
    }

    private void updateAnimations() {
        Easing widthEasingFunc = Easing.EASE_OUT_BACK;
        Easing heightEasingFunc = Easing.EASE_OUT_BACK;
        Easing positionEasingFunc = Easing.EASE_OUT_BACK;
        Easing radiusEasingFunc = Easing.EASE_OUT_BACK;

        currentWidth.update(animationSpeed.getValue(), widthEasingFunc);
        currentHeight.update(animationSpeed.getValue(), heightEasingFunc);
        needY.update(animationSpeed.getValue(), positionEasingFunc);
        radiusAnim.update(animationSpeed.getValue(), radiusEasingFunc);

        radiusAnim.setEnd(bgRadius.getValue());
    }

    private void updateColors() {
        fadeTextColor = textColor.isFade() ? ColorUtils.fadeColor(
                textColor.getColor(), textColor.getFadeColor(), textColor.getSpeed()) : textColor.getColor();

        fadeBackgroundColor = bgColor.isFade() ? ColorUtils.fadeColor(
                bgColor.getColor(), bgColor.getFadeColor(), bgColor.getSpeed()) : bgColor.getColor();
    }

    public void resetNeedY(float y, float yOffsetValue) {
        needY.setEnd(yOffsetValue - y);
    }
}