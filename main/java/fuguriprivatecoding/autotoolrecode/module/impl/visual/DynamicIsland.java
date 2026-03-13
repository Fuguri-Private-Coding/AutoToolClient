package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.hwid.HWID;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.font.ClientFont;
import fuguriprivatecoding.autotoolrecode.utils.render.font.Fonts;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.Shader;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.stencil.StencilUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
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
        ClientFont font = Fonts.fonts.get("SFPro");
        ClientFont regularFont = Fonts.fonts.get("SFProRegular");

        if (event instanceof Render2DEvent) {
            ScaledResolution sc = ScaleUtils.getScaledResolution();

            rectRadius.setEnd(opened ? 10 : 7.5f);

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

            boolean hoveredRect = GuiUtils.isMouseHovered(rectX - 5, rectY - 5, additionalWidth + 10, additionalHeight + 15);

            if (mc.currentScreen instanceof GuiChat && hoveredRect) {
                updateRun(() -> {
                    ResourceLocation songImage = Client.INST.getSongImg();

                    if (songImage != null) {
                        ColorUtils.glColor(whiteColor.withAlpha(textAlpha.getValue()));
                        RenderUtils.drawImage(songImage, 0, 0, 25, 25, true);
                    }

                    regularFont.drawString(title, 30, 5, whiteColor.withAlpha(textAlpha.getValue()));
                    regularFont.drawString(artist, 30, 15, whiteColor.withAlpha(textAlpha.getValue()));

                    String playText = playing ? "||" : "|>";
                    float playTextWidth = font.getStringWidth(playText);

                    float buttonsY = rectY + 40;

                    float prevX = rectX + 5;
                    float playX = rectX + 105 / 2f - playTextWidth / 2f;
                    float nextX = rectX + 95;

                    boolean isHoveredPrev = GuiUtils.isMouseHovered(prevX, buttonsY, 10, 10);
                    boolean isHoveredPlay = GuiUtils.isMouseHovered(playX, buttonsY, 10, 10);
                    boolean isHoveredNext = GuiUtils.isMouseHovered(nextX, buttonsY, 10, 10);

                    Color color = whiteColor.withAlpha(textAlpha.getValue());

                    Color nextColor = isHoveredNext ? color.darker() : color;
                    Color playColor = isHoveredPlay ? color.darker() : color;
                    Color prevColor = isHoveredPrev ? color.darker() : color;

                    float maxDurationWidth = 105;

                    float durationWidth = currentMediaTime / info.durationMs();
                    float currentWidth = maxDurationWidth * durationWidth;

                    RoundedUtils.drawRect(0, 30, maxDurationWidth, 3, 1.5f, Colors.BLACK.withAlpha(textAlpha.getValue() * 0.5f));
                    RoundedUtils.drawRect(0, 30, currentWidth, 3, 1.5f, whiteColor.withAlpha(textAlpha.getValue()));

                    font.drawString("<", 5, 42, prevColor);
                    font.drawString(playText, 105 / 2f - playTextWidth / 2f, 42, playColor);
                    font.drawString(">", 95, 42, nextColor);

                }, 105, 45);

                if (this.width.getValue() == 10 + this.additionalWidth) {
                    if (Mouse.isButtonDown(0) && !pressed) {
                        String playText = info.isPlaying() ? "||" : "|>";

                        float playTextWidth = font.getStringWidth(playText);
                        float buttonsY = rectY + 40;

                        float prevX = rectX + 5;
                        float playX = rectX + 105 / 2f - playTextWidth / 2f;
                        float nextX = rectX + 95;

                        boolean isHoveredPrev = GuiUtils.isMouseHovered(prevX, buttonsY, 10, 10);
                        boolean isHoveredPlay = GuiUtils.isMouseHovered(playX, buttonsY, 10, 10);
                        boolean isHoveredNext = GuiUtils.isMouseHovered(nextX, buttonsY, 10, 10);

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

                    String text = "Нет интернет подключения, клиент закроется через §9" + remainingSec + "§f s.";
                    String staticText = "Нет интернет подключения, клиент закроется через §9" + 30 + "§f s.";

                    float connectionWidth = regularFont.getStringWidth(staticText);

                    updateRun(() -> {
                        regularFont.drawString(text, 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                    }, connectionWidth, 0);
                } else {
                    Notifications notifications = Modules.getModule(Notifications.class);

                    if (notifications.isToggled() && !Notifications.notifications.isEmpty()) {
                        Notification notification = Notifications.notifications.getLast();

                        String toggleText = notification.isToggled() ? "§a включен" : "§c выключен";
                        String notificationText = " §fМодуль " + notification.getText() + "§f был" + toggleText + "§f.";

                        float notificationTextWidth = regularFont.getStringWidth(notificationText);

                        updateRun(() -> {
                            regularFont.drawString(notificationText, 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                        }, notificationTextWidth, 0);
                    } else {
                        boolean needDesc = false;
                        if (mc.currentScreen instanceof ClickScreen) {
                            List<Module> moduleList = Modules.getModulesByCategory(ClickScreen.selectedCategory);

                            for (Module module : moduleList) {
                                if (module.getDescAnim().getValue() != 0 && !module.getDescription().equalsIgnoreCase("")) {
                                    String descText = module.getDescription();

                                    needDesc = true;
                                    updateRun(() -> {
                                        regularFont.drawString(descText, 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                                    }, regularFont.getStringWidth(descText), 0);
                                }
                            }
                        }

                        if (!needDesc) {
                            updateRun(() -> {
                                regularFont.drawString(Client.INST.getFullName(), 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                            }, regularFont.getStringWidth(Client.INST.getFullName()), 0);
                        }
                    }
                }
            }

            width.setEnd(10 + additionalWidth);
            height.setEnd(15 + additionalHeight);

            opened = additionalHeight > 0;

            width.update(3, Easing.OUT_BACK);
            height.update(3, Easing.OUT_BACK);
            textAlpha.update(8, Easing.OUT_CUBIC);
            rectRadius.update(3, Easing.OUT_CUBIC);

            float x = sc.getScaledWidth() / 2f - width.getValue() / 2f;
            float y = 5;
            float width = this.width.getValue();
            float height = this.height.getValue();

            date.setTime(System.currentTimeMillis());

            String currentTimeText = FORMAT.format(date);

            float timeWidth = font.getStringWidth(currentTimeText);

            float timeX = x - timeWidth - 3;
            float timeY = y + 5;

            RenderUtils.drawRoundedOutLineRectangle(x, y, width, height, rectRadius.getValue(), Colors.BLACK.withAlpha(Modules.getModule(Blur.class).isToggled() ? 0f : 0.5f), Colors.WHITE.withAlpha(0.5f), Colors.WHITE.withAlpha(0.5f));

            if (Modules.getModule(Blur.class).isToggled()) {
                BlurUtils.addToDraw(() -> {
                    RoundedUtils.drawRect(x, y, width, height, rectRadius.getValue(), Colors.BLACK.withAlpha(1f));
                });

                BlurUtils.draw();
                mc.getFramebuffer().bindFramebuffer(false);
                mc.getFramebuffer().bindFramebufferTexture();
                Shader.drawQuad();
                GlStateManager.bindTexture(0);

                RoundedUtils.drawRect(x, y, width, height, rectRadius.getValue(), Colors.LIGHT_GRAY.withAlpha(0.05f));
            }

            StencilUtils.setUpTexture(x, y, width, height, rectRadius.getValue());
            StencilUtils.writeTexture();

            float translateX = x + 5;
            float translateY = y + 5;

            float scaleFactor = 0.9f + textAlpha.getValue() * 0.1f;

            GL11.glPushMatrix();

            ScaleUtils.startScaling(x, y, width, height, scaleFactor);
            GL11.glTranslated(translateX, translateY, 0);
            currentRun.run();
            ScaleUtils.stopScaling();

            GL11.glPopMatrix();

            StencilUtils.endWriteTexture();

            font.drawString(currentTimeText, timeX, timeY, Color.WHITE);

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
