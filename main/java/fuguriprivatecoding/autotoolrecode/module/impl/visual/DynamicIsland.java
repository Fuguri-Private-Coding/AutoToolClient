package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl.*;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.FresnelUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import smtc.TrackInfo;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {
    private final CheckBox blur = new CheckBox("Blur", this, false);
    private final CheckBox glass = new CheckBox("Glass", this, false);

    private final ColorSetting color = new ColorSetting("Color", this);

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm");

    private final EasingAnimation width, height, textAlpha, rectRadius, imageSize, buttonAlpha;

    private Runnable currentRun;

    private String displayedKey;

    private float additionalHeight = 0;
    private float additionalWidth = 0;

    private final Date date = new Date();

    private boolean opened, pressed = false, media = false;

    public DynamicIsland() {
        width = new EasingAnimation();
        height = new EasingAnimation();
        textAlpha = new EasingAnimation();
        imageSize = new EasingAnimation();
        buttonAlpha = new EasingAnimation();
        rectRadius = new EasingAnimation();
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof RenderScreenEvent)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        MsdfFont boldFont = Fonts.get("Bold");
        MsdfFont regularFont = Fonts.get("Regular");
        ScaledResolution sc = ScaleUtils.getScaledResolution();

        rectRadius.setEnd(opened ? 10f : 7.5f);

        float rectX = sc.getScaledWidth() / 2f - this.width.getValue() / 2f + 5;
        float rectY = 5 + 5;

        Colors whiteColor = Colors.WHITE;

        MediaController mediaController = Client.MEDIA_CONTROLLER;

        TrackInfo info = mediaController.getCurrent();

        boolean playing = info.isPlaying();

        boolean hoveredRect = GuiUtils.isMouseHovered(rectX - 5, rectY - 5, additionalWidth + 10, additionalHeight + 15);

        if (hoveredRect && Mouse.isButtonDown(0) && !pressed) {
            media = !media;
        }

        if (hoveredRect) {
            pressed = Mouse.isButtonDown(0);
        }

        IslandContext ctx = new IslandContext(boldFont, regularFont, whiteColor, mediaController, info, imageSize, textAlpha, media, mc.currentScreen);

        IslandComponent component = MediaIsland.create(ctx);
        if (component == null) component = NotificationIsland.create(ctx);
        if (component == null) component = DescriptionIsland.create(ctx);
        if (component == null) component = DefaultIsland.create(ctx);

        IslandComponent finalComponent = component;
        updateRun(() -> finalComponent.draw(ctx), component.getKey(ctx), component.getWidth(ctx), component.getHeight());

        if (mc.currentScreen != null && buttonAlpha.getValue() > 0.01f) {
            float widthRect = 50;

            String playText = playing ? "||" : "|>";
            float playTextWidth = boldFont.width(playText, 8);

            float buttonsY = rectY + height.getValue() + 5;

            float renderX = rectX + ((width.getValue() - 10) / 2f) - widthRect / 2f;

            float prevX = renderX + 5;
            float playX = renderX + widthRect / 2f - playTextWidth / 2f;
            float nextX = renderX + widthRect - 10;

            boolean isHoveredPrev = GuiUtils.isMouseHovered(prevX, buttonsY, 10, 10);
            boolean isHoveredPlay = GuiUtils.isMouseHovered(playX, buttonsY, 10, 10);
            boolean isHoveredNext = GuiUtils.isMouseHovered(nextX, buttonsY, 10, 10);

            float ba = buttonAlpha.getValue();
            Color color = whiteColor.withAlpha(ba);

            Color nextColor = isHoveredNext ? color.darker() : color;
            Color playColor = isHoveredPlay ? color.darker() : color;
            Color prevColor = isHoveredPrev ? color.darker() : color;

            if (glass.isToggled()) {
                FresnelUtils.drawScreen(renderX, rectY + height.getValue(), widthRect, 15, 7.5f,2f, 10f, Colors.WHITE.withAlpha(0f), 1f,
                        2f, true, 1f, 0.1f, Colors.WHITE.withAlpha(ba)
                );
            }

            RenderUtils.drawMixedRoundedRect(renderX, rectY + height.getValue(), widthRect, 15, 7.5f, new Colors(this.color.getColor()).withMultiplyAlpha(ba), new Colors(this.color.getFadeColor()).withMultiplyAlpha(ba), this.color.getSpeed());

//            RoundedUtils.drawRect(renderX, rectY + height.getValue(), widthRect, 15, 7.5f, Colors.BLACK.withAlpha(ba * 0.5f));
            regularFont.draw("<", prevX, buttonsY + 1, 12, prevColor);
            boldFont.draw(playText, playX, buttonsY - 1, 8, playColor);
            regularFont.draw(">", nextX, buttonsY + 1, 12, nextColor);

            if (blur.isToggled()) {
                BlurUtils.startWrite();
                RoundedUtils.drawRect(renderX, rectY + height.getValue(), widthRect, 15, 7.5f, Colors.WHITE.withAlpha(ba));
                BlurUtils.stopWrite();
            }

            if (media && this.width.getValue() == 10 + this.additionalWidth) {
                boolean clicked = Mouse.isButtonDown(0) && !pressed;

                if (clicked && isHoveredPrev) mediaController.prev();
                if (clicked && isHoveredPlay) mediaController.playPause();
                if (clicked && isHoveredNext) mediaController.next();

                pressed = Mouse.isButtonDown(0);
            }
        }

        width.setEnd(10 + additionalWidth);
        height.setEnd(15 + additionalHeight);
        imageSize.setEnd(mc.currentScreen != null && media ? 30f : 10f);
        buttonAlpha.setEnd(media);

        opened = additionalHeight > 0;

        width.update(5, Easing.OUT_CUBIC);
        height.update(5, Easing.OUT_CUBIC);
        textAlpha.update(5, Easing.OUT_CUBIC);
        rectRadius.update(4, Easing.IN_OUT_CUBIC);
        imageSize.update(4, Easing.OUT_BACK);
        buttonAlpha.update(5, Easing.OUT_CUBIC);

        float x = sc.getScaledWidth() / 2f - width.getValue() / 2f;
        float y = 5;
        float width = this.width.getValue();
        float height = this.height.getValue();

        date.setTime(System.currentTimeMillis());

        String currentTimeText = FORMAT.format(date);

        float timeWidth = boldFont.width(currentTimeText, 8);

        float timeX = x - timeWidth - 3;
        float timeY = y + 5;

        if (glass.isToggled()) {
            FresnelUtils.drawScreen(x, y, width, height, rectRadius.getValue(), 2f, 10f, Colors.WHITE.withAlpha(0f), 1f,
                    2f, true, 1f, 0.1f, Colors.WHITE.withAlpha(1f)
            );
        }

        RenderUtils.drawMixedRoundedRect(x, y, width, height, rectRadius.getValue(), new Colors(this.color.getColor()), new Colors(this.color.getFadeColor()), this.color.getSpeed());

        if (blur.isToggled()) {
            BlurUtils.startWrite();
            RoundedUtils.drawRect(x, y, width, height, rectRadius.getValue(), Colors.WHITE.withAlpha(1f));
            BlurUtils.stopWrite();
        }

        float translateX = x + 5;
        float translateY = y + 5;

        GL11.glPushMatrix();
        StencilUtils.setUpTexture(x, y, width, height, rectRadius.getValue());
        StencilUtils.writeTexture();
        GL11.glTranslated(translateX, translateY, 0);
        currentRun.run();
        StencilUtils.endWriteTexture();
        GL11.glPopMatrix();

        boldFont.draw(currentTimeText, timeX, timeY, 8, Color.WHITE);

        float internetX = x + width + 5;
        float internetY = y + 5;

        RenderUtils.drawRect(internetX, internetY + 2, 1f, 2, Colors.WHITE);
        RenderUtils.drawRect(internetX + 2f, internetY + 1, 1f, 3, Colors.WHITE);
        RenderUtils.drawRect(internetX + 2f + 2f, internetY, 1f, 4, Colors.WHITE);
    }


    private void updateRun(Runnable run, String key, float additionalWidth, float additionalHeight) {
        this.additionalWidth = additionalWidth;
        this.additionalHeight = additionalHeight;

        if (displayedKey == null || !displayedKey.equals(key)) {
            if (textAlpha.getValue() <= 0.05f) {
                currentRun = run;
                displayedKey = key;
                textAlpha.setEnd(1f);
            } else {
                textAlpha.setEnd(0f);
            }
        } else {
            currentRun = run;
            textAlpha.setEnd(1f);
        }
    }
}
