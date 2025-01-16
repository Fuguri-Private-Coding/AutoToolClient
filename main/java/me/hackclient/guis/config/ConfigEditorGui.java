package me.hackclient.guis.config;

import me.hackclient.Client;
import me.hackclient.shader.impl.PixelReplacerUtils;
import me.hackclient.shader.impl.RoundedUtils;
import me.hackclient.utils.render.scissor.ScissorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ConfigEditorGui extends GuiScreen {

    final GuiScreen parrentScreen;
    int scroll;

    public ConfigEditorGui(GuiScreen parrentScreen) {
        mc = Minecraft.getMinecraft();
        this.parrentScreen = parrentScreen;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sc = new ScaledResolution(mc);
        final float centerX = sc.getScaledWidth() / 2f;
        final float centerY = sc.getScaledHeight() / 2f;

        int counter = 0;
        float x = 0;
        float y = 0;
        ScissorUtils.enableScissor();
        ScissorUtils.scissor(sc,
                centerX - 150 - 4,
                centerY - 100 - 1,
                300 + 8,
                200 + 2
        );
        PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(
                centerX - 150 - 4,
                centerY - 100 - 1,
                300 + 8,
                200 + 2,
                5,
                new Color(0, 0, 0, 120)
        ));
        for (File file : Client.INSTANCE.getClientDirectory().listFiles()) {
            float finalX = x;
            float finalY = y;
            PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(
                    centerX - 150 + 3 + finalX,
                    centerY - 100 + 3 + finalY,
                    50,
                    25,
                    5f,
                    new Color(0, 0, 0, 120)
            ));
            PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(
                    centerX - 150 + 3 + 1 + finalX,
                    centerY - 100 + 3 - 1 + finalY + 13,
                    25 - 0.5f,
                    11,
                    3.5f,
                    new Color(0, 0, 0, 120)
            ));
            PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(
                    centerX - 150 + 3 + finalX + 25,
                    centerY - 100 + 3 - 1 + finalY + 13,
                    25 - 0.5f,
                    11,
                    3.5f,
                    new Color(0, 0, 0, 120)
            ));
            mc.fontRendererObj.drawString(file.getName(), centerX - 150 + 3 + 3 + x, centerY - 100 + 3 + 3 + y, -1);
            x += 50 + 1;
            if (counter++ == 6) {
                counter = 0;
                x = 0;
                y += 25 + 1;
            }
        }
        ScissorUtils.disableScissor();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sc = new ScaledResolution(mc);
        final float centerX = sc.getScaledWidth() / 2f;
        final float centerY = sc.getScaledHeight() / 2f;

        int counter = 0;
        float x = 0;
        float y = 0;

        for (File file : Client.INSTANCE.getClientDirectory().listFiles()) {
//            float finalX = x;
//            float finalY = y;
//            PixelReplacerUtils.addToDraw(() -> RoundedUtils.drawRect(
//                    centerX - 150 + 3 + finalX,
//                    centerY - 100 + 3 + finalY,
//                    50,
//                    25,
//                    5f,
//                    new Color(0, 0, 0, 255)
//            ));
            //mc.fontRendererObj.drawString(file.getName(), centerX - 150 + 3 + 3 + x, centerY - 100 + 3 + 3 + y, -1);
            x += 50 + 1;
            if (counter++ == 6) {
                counter = 0;
                x = 0;
                y += 25 + 1;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        mc.currentScreen = parrentScreen;
    }
}
