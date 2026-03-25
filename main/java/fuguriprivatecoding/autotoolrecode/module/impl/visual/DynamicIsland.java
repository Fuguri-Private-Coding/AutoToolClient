package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWID;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.scissor.ScissorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RectUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import smtc.TrackInfo;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    private final CheckBox blur = new CheckBox("Blur", this, false);

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm");

    private final EasingAnimation width, height, textAlpha, rectRadius;

    private Runnable currentRun, lastRun;

    private float additionalHeight = 0;
    private float additionalWidth = 0;

    private final Date date = new Date();

    private boolean opened, pressed = false;

    DynamicTexture dynamicTexture;

    public DynamicIsland() {
        width = new EasingAnimation();
        height = new EasingAnimation();
        textAlpha = new EasingAnimation();
        rectRadius = new EasingAnimation();
    }

    @Override
    public void onEvent(Event event) {
        MsdfFont boldFont = Fonts.get("Bold");
        MsdfFont regularFont = Fonts.get("Regular");

        if (event instanceof RenderScreenEvent) {
            ScaledResolution sc = ScaleUtils.getScaledResolution();

            rectRadius.setEnd(opened ? 10f : 7.5f);

            float rectX = sc.getScaledWidth() / 2f - this.width.getValue() / 2f + 5;
            float rectY = 5 + 5;

            Colors whiteColor = Colors.WHITE;

            float currentMediaTime = Client.INST.getMediaController().getInterpolatedPositionMs();

            MediaController mediaController = Client.INST.getMediaController();

            TrackInfo info = mediaController.getCurrent();

            String title = info.title();
            String artist = info.artist();
            boolean playing = info.isPlaying();

            if (!Objects.equals(title, Client.INST.getSongName())) {
                BufferedImage img = mediaController.getArtworkImage();

                if (img != null) {
                    dynamicTexture = new DynamicTexture(img);

                    String name = "song_image_" + info.title();
                    ResourceLocation songImage = mc.getTextureManager().getDynamicTextureLocation(name, dynamicTexture);

                    Client.INST.setSongImg(songImage);
                    Client.INST.setSongName(info.title());
                }
            }

            boolean hoveredRect = GuiUtils.isMouseHovered(rectX - 5, rectY - 5, additionalWidth + 10, additionalHeight + (opened ? 50 : 15));

            if (mc.currentScreen != null && hoveredRect) {
                float titleWidth = Math.max(regularFont.width(info.title(), 8), 105 - 35);
                float artistWidth = Math.max(regularFont.width(info.artist(), 6), 105 - 35);

                float needWidth = Math.max(titleWidth, artistWidth);

                updateRun(() -> {
                    ResourceLocation songImage = Client.INST.getSongImg();

                    if (songImage != null) {
                        ColorUtils.glColor(whiteColor.withAlpha(textAlpha.getValue()));
                        StencilUtils.initStencil();
                        GL11.glEnable(2960);
                        StencilUtils.bindWriteStencilBuffer();
                        RectUtils.drawRect(0, 0, 30, 30, 7.5f, Color.WHITE);
                        StencilUtils.writeTexture();
                        RenderUtils.drawImage(songImage, 0, 0, 30, 30, true);
                        StencilUtils.endWriteTexture();
                    }

                    regularFont.draw(title, 35, 7, 8, whiteColor.withAlpha(textAlpha.getValue()));
                    regularFont.draw(artist, 35, 7 + regularFont.height(title, 8) + 3, 6, Colors.WHITE.withAlpha(textAlpha.getValue()));
                }, needWidth + 35, 25);

                float widthRect = 50;

                String playText = playing ? "||" : "|>";
                float playTextWidth = boldFont.width(playText, 8);

                float buttonsY = rectY + height.getValue() + 15;

                float elementAlpha = height.getValue() / (additionalHeight + 15);

                float renderX = rectX + ((width.getValue() - 10) / 2f) - widthRect / 2f;

                float prevX = renderX + 5;
                float playX = renderX + widthRect / 2f - playTextWidth / 2f;
                float nextX = renderX + widthRect - 10;

                boolean isHoveredPrev = GuiUtils.isMouseHovered(prevX, buttonsY, 10, 10);
                boolean isHoveredPlay = GuiUtils.isMouseHovered(playX, buttonsY, 10, 10);
                boolean isHoveredNext = GuiUtils.isMouseHovered(nextX, buttonsY, 10, 10);

                Color color = whiteColor.withAlpha(elementAlpha);

                Color nextColor = isHoveredNext ? color.darker() : color;
                Color playColor = isHoveredPlay ? color.darker() : color;
                Color prevColor = isHoveredPrev ? color.darker() : color;

                float maxDurationWidth = width.getValue() - 30;

                float durationWidth = currentMediaTime / info.durationMs();
                float currentWidth = maxDurationWidth * durationWidth;

                RoundedUtils.drawRect(rectX + 7.5f, rectY + height.getValue() - 1.25f, width.getValue() - 25f, 3f + 5, 4.75f, Colors.BLACK.withAlpha(elementAlpha * 0.5f));
                RoundedUtils.drawRect(rectX + 10, rectY + height.getValue() + 1f, width.getValue() - 30, 3f, 1.5f, Colors.BLACK.withAlpha(elementAlpha * 0.5f));
                RoundedUtils.drawRect(rectX + 10, rectY + height.getValue() + 1f, currentWidth, 3f, 1.5f, Colors.WHITE.withAlpha(elementAlpha));

                RoundedUtils.drawRect(renderX, rectY + height.getValue() + 10, widthRect, 15, 7.5f, Colors.BLACK.withAlpha(elementAlpha * 0.5f));
                regularFont.draw("<", prevX, buttonsY + 1, 12, prevColor);
                boldFont.draw(playText, playX, buttonsY - 1, 8, playColor);
                regularFont.draw(">", nextX, buttonsY + 1, 12, nextColor);

                if (blur.isToggled()) {
                    BlurUtils.addToDraw(() -> {
                        RoundedUtils.drawRect(rectX + 7.5f, rectY + height.getValue() - 1.25f, width.getValue() - 25f, 3f + 5, 4.75f, Colors.WHITE.withAlpha(elementAlpha));
                        RoundedUtils.drawRect(renderX, rectY + height.getValue() + 10, widthRect, 15, 7.5f, Colors.WHITE.withAlpha(elementAlpha));
                    });
                }

                if (this.width.getValue() == 10 + this.additionalWidth) {
                    if (Mouse.isButtonDown(0) && !pressed) {
                        if (isHoveredPrev) mediaController.prev();
                        if (isHoveredPlay) mediaController.playPause();
                        if (isHoveredNext) mediaController.next();
                    }

                    pressed = Mouse.isButtonDown(0);
                }
            } else {
                if (HWID.noConnection) {
                    long time = System.currentTimeMillis() - HWID.lastTimeConnection;
                    int sec = Integer.parseInt(String.valueOf(time / 1000L));

                    int remainingSec = 30 - sec;

                    String text = "Нет интернет подключения, клиент закроется через " + remainingSec + " s.";
                    String staticText = "Нет интернет подключения, клиент закроется через " + 30 + " s.";

                    float connectionWidth = regularFont.width(staticText, 8);

                    updateRun(() -> {
                        regularFont.draw(text, 0, 0, 8, whiteColor.withAlpha(textAlpha.getValue()));
                    }, connectionWidth, 0);
                } else {
                    Notifications notifications = Modules.getModule(Notifications.class);

                    if (notifications.isToggled() && !Notifications.notifications.isEmpty()) {
                        Notification notification = Notifications.notifications.getLast();

                        String toggleText = notification.isToggled() ? "включен" : "выключен";
                        String notificationText = "Модуль " + notification.getText() + " был " + toggleText + ".";

                        float notificationTextWidth = regularFont.width(notificationText, 8);

                        updateRun(() -> {
                            regularFont.draw(notificationText, 0, 0, 8, whiteColor.withAlpha(textAlpha.getValue()));
                        }, notificationTextWidth, 0);
                    } else {
                        boolean needDesc = false;
                        if (mc.currentScreen instanceof ClickScreen) {
                            List<Module> moduleList = Modules.getModulesByCategory(ClickScreen.selectedCategory);

                            for (Module module : moduleList) {
                                if (module.isHovered() && !module.getDescription().equalsIgnoreCase("")) {
                                    String descText = module.getDescription();

                                    needDesc = true;
                                    updateRun(() -> {
                                        regularFont.draw(descText, 0, 0, 8, whiteColor.withAlpha(textAlpha.getValue()));
                                    }, regularFont.width(descText, 8), 0);
                                }
                            }
                        }

                        if (!needDesc) {
                            updateRun(() -> {
                                regularFont.draw(Client.INST.getFullName(), 0, 0, 8, whiteColor.withAlpha(textAlpha.getValue()));
                            }, regularFont.width(Client.INST.getFullName(), 8), 0);
                        }
                    }
                }
            }

            width.setEnd(10 + additionalWidth);
            height.setEnd(15 + additionalHeight);

            opened = additionalHeight > 0;

            width.update(4, Easing.IN_OUT_CUBIC);
            height.update(4, Easing.IN_OUT_CUBIC);
            textAlpha.update(5, Easing.OUT_CUBIC);
            rectRadius.update(4, Easing.IN_OUT_CUBIC);

            float x = sc.getScaledWidth() / 2f - width.getValue() / 2f;
            float y = 5;
            float width = this.width.getValue();
            float height = this.height.getValue();

            date.setTime(System.currentTimeMillis());

            String currentTimeText = FORMAT.format(date);

            float timeWidth = boldFont.width(currentTimeText, 8);

            float timeX = x - timeWidth - 3;
            float timeY = y + 5;

            RoundedUtils.drawRect(x, y, width, height, rectRadius.getValue(), Colors.BLACK.withAlpha(0.5f));

            if (blur.isToggled()) {
                BlurUtils.addToDraw(() -> RoundedUtils.drawRect(x, y, width, height, rectRadius.getValue(), Colors.WHITE.withAlpha(1f)));
            }

            ScissorUtils.enableScissor();
            ScissorUtils.scissor(sc, x + 2, y + 2, width - 4, height - 4);

            float translateX = x + 5;
            float translateY = y + 5;

            float scaleFactor = 0.95f + textAlpha.getMultipleValue(0.05f);

            GL11.glPushMatrix();

            ScaleUtils.startScaling(x, y, width, height, scaleFactor);
            GL11.glTranslated(translateX, translateY, 0);
            currentRun.run();
            ScaleUtils.stopScaling();

            GL11.glPopMatrix();

            ScissorUtils.disableScissor();

            boldFont.draw(currentTimeText, timeX, timeY, 8, Color.WHITE);

            float internetX = x + width + 5;
            float internetY = y + 5;

            RoundedUtils.drawRect(internetX, internetY + 2, 0.5f, 2, 0, Colors.WHITE);
            RoundedUtils.drawRect(internetX + 2.5f, internetY + 1, 0.5f, 3, 0, Colors.WHITE);
            RoundedUtils.drawRect(internetX + 2.5f + 2.5f, internetY, 0.5f, 4, 0, Colors.WHITE);
        }
    }

    private void updateRun(Runnable run, float additionalWidth, float additionalHeight) {
        if (width.getValue() != additionalWidth) {
            lastRun = run;
            this.additionalWidth = additionalWidth;
            this.additionalHeight = additionalHeight;
            textAlpha.setEnd(0);
        }

        if (width.getValue() - 10 == additionalWidth) {
            lastRun = run;
            currentRun = lastRun;
        }

        if (!width.isAnimating() && !height.isAnimating()) {
            if (textAlpha.getValue() == 0f) {
                currentRun = lastRun;
            }

            textAlpha.setEnd(1);
        }
    }
}
