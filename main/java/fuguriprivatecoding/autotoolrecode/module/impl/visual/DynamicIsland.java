package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
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

            var notifications = Modules.getModule(Notifications.class);

            float rectX = sc.getScaledWidth() / 2f - this.width.getValue() / 2f + 5;
            float rectY = 5 + 5;

            Colors whiteColor = Colors.WHITE;

            if (mc.currentScreen != null && GuiUtils.isMouseHovered(rectX - 5, rectY - 5, additionalWidth + 10, additionalHeight + 15)) {
                var nowPlaying = Client.INST.getNowPlaying();

                TrackInfo info = nowPlaying.getCurrent();
                BufferedImage img = nowPlaying.getArtworkImage();

                if (img == null) {
                    String mediaText = "Медиа контент не найден.";

                    float mediaTextWidth = regularFont.getStringWidth(mediaText);

                    updateRun(() -> {
                         regularFont.drawString(mediaText, 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                    }, mediaTextWidth, 0);
                } else {
                    if (!Objects.equals(info.title(), Client.INST.getSongName())) {
                        dynamicTexture = new DynamicTexture(img);

                        String name = "song_image_" + info.title();
                        ResourceLocation songImage = mc.getTextureManager().getDynamicTextureLocation(name, dynamicTexture);

                        Client.INST.setSongImg(songImage);
                        Client.INST.setSongName(info.title());
                    }

                    String title = info.title();
                    String artist = info.artist();
                    boolean playing = info.isPlaying();

                    updateRun(() -> {
                        if (Client.INST.getSongImg() != null) {
                            ColorUtils.glColor(whiteColor.withAlpha(textAlpha.getValue()));
                            RenderUtils.drawImage(Client.INST.getSongImg(), 0, 0, 25, 25, true);
                        }

                        regularFont.drawString(title, 30, 5, whiteColor.withAlpha(textAlpha.getValue()));
                        regularFont.drawString(artist, 30, 15, whiteColor.withAlpha(textAlpha.getValue()));

                        boolean isHoveredNext = GuiUtils.isMouseHovered(rectX + 95, rectY + 30, 10, 10);
                        boolean isHoveredPlay = GuiUtils.isMouseHovered(rectX + 95 / 2f, rectY + 30, 10, 10);
                        boolean isHoveredPrev = GuiUtils.isMouseHovered(rectX, rectY + 30, 10, 10);

                        Color nextColor = isHoveredNext ? Colors.YELLOW.withAlpha(textAlpha.getValue()).darker() : Colors.YELLOW.withAlpha(textAlpha.getValue());
                        Color playColor = isHoveredPlay ? playing ? Colors.GREEN.withAlpha(textAlpha.getValue()).darker() : Colors.RED.withAlpha(textAlpha.getValue()).darker() : playing ? Colors.GREEN.withAlpha(textAlpha.getValue()) : Colors.RED.withAlpha(textAlpha.getValue());
                        Color prevColor = isHoveredPrev ? Colors.YELLOW.withAlpha(textAlpha.getValue()).darker() : Colors.YELLOW.withAlpha(textAlpha.getValue());

                        RoundedUtils.drawRect(0, 30, 10, 10, 5f, prevColor);
                        RoundedUtils.drawRect(95 / 2f, 30, 10, 10, 5, playColor);
                        RoundedUtils.drawRect(95, 30, 10, 10, 5f, nextColor);
                    }, 105, 35);

                    if (this.width.getValue() == 10 + 105) {
                        if (Mouse.isButtonDown(0) && !pressed) {
                            if (GuiUtils.isMouseHovered(rectX, rectY + 30, 10, 10)) MediaController.prev();
                            if (GuiUtils.isMouseHovered(rectX + 95 / 2f, rectY + 30, 10, 10)) MediaController.playPause();
                            if (GuiUtils.isMouseHovered(rectX + 95, rectY + 30, 10, 10)) MediaController.next();
                        }

                        pressed = Mouse.isButtonDown(0);
                    }
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
                    if (notifications.isToggled() && !Notifications.notifications.isEmpty()) {
                        Notification notification = Notifications.notifications.getLast();

                        String toggleText = notification.isToggled() ? "§a включен" : "§c выключен";
                        String notificationText = " §fМодуль " + notification.getText() + "§f был" + toggleText + "§f.";

                        float notificationTextWidth = regularFont.getStringWidth(notificationText);

                        updateRun(() -> {
                            regularFont.drawString(notificationText, 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                        }, notificationTextWidth, 0);
                    } else {
                        updateRun(() -> {
                            regularFont.drawString(Client.INST.getFullName(), 0, 0, whiteColor.withAlpha(textAlpha.getValue()));
                        }, regularFont.getStringWidth(Client.INST.getFullName()), 0);
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

            float renderX = x + 5;
            float renderY = y + 5;

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

            GL11.glPushMatrix();

            float scaleFactor = 0.9f + textAlpha.getValue() * 0.1f;
            ScaleUtils.startScaling(x, y, width, height, scaleFactor);
            GL11.glTranslated(renderX, renderY, 0);
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
