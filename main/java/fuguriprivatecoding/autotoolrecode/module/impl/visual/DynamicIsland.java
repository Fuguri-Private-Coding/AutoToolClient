package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFontRenderer;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.interpolation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomRealUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.util.vector.Vector2f;
import java.awt.*;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    Mode fonts = new Mode("Fonts", this);

    FloatSetting yOffset = new FloatSetting("Y-Offset", this, 0, 100, 5, 0.1f);

    FloatSetting width = new FloatSetting("Width", this, 0f, 50f, 10f, 0.1f);
    FloatSetting animationSpeed = new FloatSetting("Animation Speed", this, 0f, 5f, 2f, 0.1f);

    public final ColorSetting textColor = new ColorSetting("Text Color", this);

    FloatSetting bgRadius = new FloatSetting("Background Radius", this, 0f, 20, 7.5f, 0.1f);
    public final ColorSetting bgColor = new ColorSetting("Background Color", this);

    CheckBox glow = new CheckBox("Glow", this, true);
    CheckBox blur = new CheckBox("Blur", this, true);

    public final ColorSetting bgColorShadow = new ColorSetting("Background Shadow Color", this, () -> glow.isToggled());

    EasingAnimation currentWidth = new EasingAnimation();
    EasingAnimation currentHeight = new EasingAnimation();
    EasingAnimation needY = new EasingAnimation();
    EasingAnimation radiusAnim = new EasingAnimation();
    EasingAnimation textAlpha = new EasingAnimation();

    String currentText, currentWidthText;

    public DynamicIsland() {
        Fonts.fonts.forEach((fontName, _) -> fonts.addMode(fontName));
        fonts.setMode("SFProRounded");
        currentText = "AutoTool";
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render2DEvent e) {
            ClientFontRenderer font = Fonts.fonts.get(fonts.getMode());
            Vector2f screenSize = new Vector2f(e.getSc().getScaledWidth(), e.getSc().getScaledHeight());

            float height = 15;
            float yOffsetValue = this.yOffset.getValue();

            EntityLivingBase ent = TargetStorage.getTarget();
            BackTrack backTrack = Modules.getModule(BackTrack.class);
            Scaffold scaffOld = Modules.getModule(Scaffold.class);

            String name = Client.INST.getFullName();
            String target, backtrack = "", scaffold = "", bps = "";

            currentHeight.setEnd(height);
            updateText(name);
            needY.setEnd(yOffsetValue);

            if (MoveUtils.isMoving()) {
                bps = "BPS - " + String.format("%.3f", mc.thePlayer.getBps(false));
                updateText(bps);
            }

            if (ent != null) {
                target = "Current Target - " + ent.getName();
                updateText(target);
            }

            if (scaffOld.getBlockCount() > 0 && scaffOld.isToggled()) {
                scaffold = "Blocks Left - " + scaffOld.getBlockCount();
                updateText(scaffold);
            }

            if (backTrack.isToggled() && backTrack.packetBuffer.size() > 10 && ent != null) {
                AxisAlignedBB realBox = ent.getEntityBoundingBox().offset(ent.nx - ent.posX, ent.ny - ent.posY, ent.nz - ent.posZ).expand(
                    ent.getCollisionBorderSize(),
                    ent.getCollisionBorderSize(),
                    ent.getCollisionBorderSize()
                );

                backtrack = "Distance - " + String.format("%.1f", DistanceUtils.getDistance(realBox));
                updateText(backtrack);
            }

            currentWidth.setEnd((float) (width.getValue() + font.getStringWidth(currentWidthText.equalsIgnoreCase(bps) ? "BPS - 00000" : currentWidthText.equalsIgnoreCase(scaffold) ? "Blocks Left - 255" : currentWidthText.equalsIgnoreCase(backtrack) ? "Distance - 12" : currentWidthText)));

            updateAnimations();

            RoundedUtils.drawRect(
                screenSize.x / 2f - currentWidth.getValue() / 2f,
                yOffsetValue,
                currentWidth.getValue(),
                currentHeight.getValue(),
                radiusAnim.getValue(),
                bgColor.getFadedColor()
            );

            font.drawString(currentText, (screenSize.x / 2f - font.getStringWidth(currentText) / 2f), 5.5f + needY.getValue(), new Color(textColor.getFadedColor().getRed() / 255f, textColor.getFadedColor().getGreen() / 255f, textColor.getFadedColor().getBlue() / 255f, textAlpha.getValue()));

            if (glow.isToggled())
                BloomRealUtils.addToDraw(() -> RoundedUtils.drawRect(screenSize.x / 2f - currentWidth.getValue() / 2f, yOffsetValue, currentWidth.getValue(), currentHeight.getValue(), radiusAnim.getValue(), bgColorShadow.getFadedColor()));
            if (blur.isToggled())
                GaussianBlurUtils.addToDraw(() -> RoundedUtils.drawRect(screenSize.x / 2f - currentWidth.getValue() / 2f, yOffsetValue, currentWidth.getValue(), currentHeight.getValue(), radiusAnim.getValue(), Color.WHITE));
        }
    }

    private void updateAnimations() {
        currentWidth.update(animationSpeed.getValue(), Easing.LINEAR);
        currentHeight.update(animationSpeed.getValue(), Easing.LINEAR);
        needY.update(animationSpeed.getValue(), Easing.LINEAR);
        radiusAnim.update(animationSpeed.getValue(), Easing.LINEAR);
        textAlpha.update(7, Easing.LINEAR);

        radiusAnim.setEnd(bgRadius.getValue());
    }

    public void updateText(String text) {
        currentWidthText = text;

        if (!text.startsWith(currentText.substring(0,3))) {
            textAlpha.setEnd(0);
        } else {
            currentText = text;
        }

        if (textAlpha.getValue() == 0) currentText = text;

        if (!currentWidth.isAnimating()) {
            textAlpha.setEnd(1);
        }
    }
}