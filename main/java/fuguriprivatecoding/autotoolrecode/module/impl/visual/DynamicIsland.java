package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.notification.Notification;
import fuguriprivatecoding.autotoolrecode.utils.animation.Easing;
import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.client.sound.TrackInfoFinder;
import fuguriprivatecoding.autotoolrecode.utils.gui.GuiUtils;
import fuguriprivatecoding.autotoolrecode.utils.gui.ScaleUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
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
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ModuleInfo(name = "DynamicIsland", category = Category.VISUAL)
public class DynamicIsland extends Module {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm");

    private final EasingAnimation width, height, textAlpha, rectRadius;

    private Runnable currentRun, lastRun;

    private float additionalHeight = 0;
    private float additionalWidth = 0;

    private final Date date = new Date();

    private boolean opened, pressed = false;

    public DynamicIsland() {
        width = new EasingAnimation();
        height = new EasingAnimation();
        textAlpha = new EasingAnimation();
        rectRadius = new EasingAnimation();
    }

    @Override
    public void onEvent(Event event) {
        ClientFont font = Fonts.fonts.get("SFPro");
        ClientFont fontr = Fonts.fonts.get("SFProRegular");

        if (event instanceof RenderScreenEvent) {
            ScaledResolution sc = ScaleUtils.getScaledResolution();

            rectRadius.setEnd(opened ? 10 : 7.5f);

            var notifications = Modules.getModule(Notifications.class);

            if (mc.currentScreen != null && GuiUtils.isMouseHovered(sc.getScaledWidth() / 2f - width.getValue() / 2f, 5, width.getValue(), height.getValue())) {
                String profile = "Profile: " + Client.INST.getProfile().toColoredString();

                float width = fontr.getStringWidth(profile);

                updateText(() -> {
                    RoundedUtils.drawRect(0, 0, 10, 10, 5f, Colors.YELLOW.withAlpha(textAlpha.getValue()));
                    RoundedUtils.drawRect(65, 0, 10, 10, 5f, Colors.RED.withAlpha(textAlpha.getValue()));
                }, 80, 5);

                if (this.width.getValue() == 10 + 80) {
                    float x = sc.getScaledWidth() / 2f - this.width.getValue() / 2f + 5;
                    float y = 5 + 5;

                    if (GuiUtils.isMouseHovered(x, y, 10, 10) && Mouse.isButtonDown(0) && !pressed) {
                        TrackInfoFinder.nextTrack();
                        ClientUtils.chatLog("next");
                    }

                    if (GuiUtils.isMouseHovered(x + 65, y, 10, 10) && Mouse.isButtonDown(0) && !pressed) {
                        TrackInfoFinder.playPause();
                        ClientUtils.chatLog("playpause");
                    }

                    pressed = Mouse.isButtonDown(0);
                }
            } else {
                if (notifications.isToggled() && !Notifications.notifications.isEmpty()) {
                    Notification notification = Notifications.notifications.getLast();

                    String toggleText = notification.isToggled() ? "§a включен" : "§c выключен";
                    String text = " §fМодуль " + notification.getText() + "§f был" + toggleText + "§f.";

                    updateText(() -> {
                        fontr.drawString(text, 0, 0, Colors.WHITE.withAlpha(textAlpha.getValue()));
                    }, fontr.getStringWidth(text), 0);
                } else {
                    updateText(() -> {
                        fontr.drawString(Client.INST.getFullName(), 0, 0, Colors.WHITE.withAlpha(textAlpha.getValue()));
                    }, fontr.getStringWidth(Client.INST.getFullName()), 0);
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
            GL11.glTranslated(renderX, renderY, 0);
            currentRun.run();
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

    private void updateText(Runnable run, float additionalWidth, float additionalHeight) {
        if (width.getValue() != additionalWidth) {
            lastRun = run;
            this.additionalWidth = additionalWidth;
            this.additionalHeight = additionalHeight;
            textAlpha.setEnd(0);
        }

        if (!width.isAnimating() && !height.isAnimating()) {
            textAlpha.setEnd(1);

            if (textAlpha.getValue() == 0f) {
                currentRun = lastRun;
            }
        }
    }
}
