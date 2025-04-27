package me.hackclient.module.impl.visual;

import me.hackclient.Client;
import me.hackclient.event.Event;
import me.hackclient.event.events.ClickEvent;
import me.hackclient.event.events.Render2DEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.BooleanSetting;
import me.hackclient.settings.impl.ColorSetting;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.MultiBooleanSetting;
import me.hackclient.shader.impl.BloomUtils;
import me.hackclient.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "HUD", category = Category.VISUAL)
public class HUD extends Module {

    MultiBooleanSetting hudElements = new MultiBooleanSetting("HUDElements", this)
            .add("BPSCounter")
            .add("CPSCounter")
            .add("BreakIndicator")
            .add("WaterMark");

    FloatSetting bpsPosH = new FloatSetting("BPSPosX", this,() -> hudElements.get("BPSCounter"), 0,10000,0,0.1f);
    FloatSetting bpsPosV = new FloatSetting("BPSPosY", this,() -> hudElements.get("BPSCounter"), 0,10000,0,0.1f);
    FloatSetting bpsRadius = new FloatSetting("BPSRadius", this,() -> hudElements.get("BPSCounter"), 0.5f,5,1f,0.1f);
    ColorSetting bpsColor = new ColorSetting("BPSColor", this,() -> hudElements.get("BPSCounter"), 0,0,0,0.4f);
    BooleanSetting bpsTextShadow = new BooleanSetting("BPSTextShadow",this,() -> hudElements.get("BPSCounter"), true);
    ColorSetting bpsTextColor = new ColorSetting("BPSTextColor", this,() -> hudElements.get("BPSCounter"), 1,1,1,1);
    BooleanSetting includeY = new BooleanSetting("BPSIncludeY", this,() -> hudElements.get("BPSCounter"), false);

    FloatSetting cpsPosH = new FloatSetting("CPSPosX", this,() -> hudElements.get("CPSCounter"), 0,10000,0,0.1f);
    FloatSetting cpsPosV = new FloatSetting("CPSPosY", this,() -> hudElements.get("CPSCounter"), 0,10000,0,0.1f);
    FloatSetting cpsRadius = new FloatSetting("CPSRadius", this,() -> hudElements.get("CPSCounter"), 0.5f,5,1f,0.1f);
    ColorSetting cpsColor = new ColorSetting("CPSColor", this,() -> hudElements.get("CPSCounter"), 0,0,0,0.4f);
    BooleanSetting cpsTextShadow = new BooleanSetting("CPSTextShadow",this,() -> hudElements.get("CPSCounter"), true);
    ColorSetting cpsTextColor = new ColorSetting("CPSTextColor", this,() -> hudElements.get("CPSCounter"), 1,1,1,1);

    FloatSetting breakIndicatorPosH = new FloatSetting("BreakIndicatorPosX", this,() -> hudElements.get("BreakIndicator"), 0,10000,0,0.1f);
    FloatSetting breakIndicatorPosV = new FloatSetting("BreakIndicatorPosY", this,() -> hudElements.get("BreakIndicator"), 0,10000,0,0.1f);
    FloatSetting breakIndicatorRadius = new FloatSetting("BreakIndicatorRadius", this,() -> hudElements.get("BreakIndicator"), 0.5f,5,1f,0.1f);
    ColorSetting breakIndicatorColor = new ColorSetting("BreakIndicatorColor", this,() -> hudElements.get("BreakIndicator"), 0,0,0,0.4f);
    BooleanSetting breakIndicatorTextShadow = new BooleanSetting("BreakIndicatorTextShadow",this,() -> hudElements.get("BreakIndicator"), true);
    ColorSetting breakIndicatorTextColor = new ColorSetting("BreakIndicatorTextColor", this,() -> hudElements.get("BreakIndicator"), 1,1,1,1);

    FloatSetting waterMarkPosH = new FloatSetting("WaterMarkPosX", this,() -> hudElements.get("WaterMark"), 0,10000,0,0.1f);
    FloatSetting waterMarkPosV = new FloatSetting("WaterMarkPosY", this,() -> hudElements.get("WaterMark"), 0,10000,0,0.1f);
    FloatSetting waterMarkRadius = new FloatSetting("WaterMarkRadius", this,() -> hudElements.get("WaterMark"), 0.5f,5,1f,0.1f);
    ColorSetting waterMarkColor = new ColorSetting("WaterMarkColor", this,() -> hudElements.get("WaterMark"), 0,0,0,0.4f);
    BooleanSetting waterMarkTextShadow = new BooleanSetting("WaterMarkTextShadow",this,() -> hudElements.get("WaterMark"), true);
    ColorSetting waterMarkTextColor = new ColorSetting("WaterMarkTextColor", this,() -> hudElements.get("WaterMark"), 1,1,1,1);

    Shadows shadows;

    private final java.util.List<Long> leftClicks = new CopyOnWriteArrayList<>();
    private final List<Long> rightClicks = new CopyOnWriteArrayList<>();

    @Override
    public void onEvent(Event event) {
        super.onEvent(event);
        if (shadows == null) shadows = Client.INSTANCE.getModuleManager().getModule(Shadows.class);
        ScaledResolution sc = new ScaledResolution(mc);

        bpsPosH.setMax(sc.getScaledWidth());
        bpsPosV.setMax(sc.getScaledHeight());
        cpsPosH.setMax(sc.getScaledWidth());
        cpsPosV.setMax(sc.getScaledHeight());
        breakIndicatorPosH.setMax(sc.getScaledWidth());
        breakIndicatorPosV.setMax(sc.getScaledHeight());
        waterMarkPosH.setMax(sc.getScaledWidth());
        waterMarkPosV.setMax(sc.getScaledHeight());

        if (event instanceof ClickEvent clickEvent && hudElements.get("CPSCounter")) {
            switch (clickEvent.getButton()) {
                case LEFT -> leftClicks.add(System.currentTimeMillis());
                case RIGHT -> rightClicks.add(System.currentTimeMillis());
            }
        }

        if (event instanceof Render2DEvent) {
            if (hudElements.get("BPSCounter")) {
                String text = String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled()));
                float width = mc.fontRendererObj.getStringWidth(text);

                if (shadows.isToggled() && shadows.bpsCounter.isToggled())
                    BloomUtils.addToDraw(() -> RoundedUtils.drawRect(bpsPosH.getValue(), bpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, bpsRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(bpsPosH.getValue(), bpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, bpsRadius.getValue(), bpsColor.getColor());
                mc.fontRendererObj.drawString(String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled())), bpsPosH.getValue() + 2.5f, bpsPosV.getValue() + 2.5f, bpsTextColor.getColor().getRGB(), bpsTextShadow.isToggled());
            }

            if (hudElements.get("CPSCounter")) {
                leftClicks.removeIf(time -> System.currentTimeMillis() - time >= 1000L);
                rightClicks.removeIf(time -> System.currentTimeMillis() - time >= 1000L);

                int leftCps = leftClicks.size();
                int rightCps = rightClicks.size();

                String text = leftCps + " / " + rightCps;
                float width = mc.fontRendererObj.getStringWidth(text);

                if (shadows.isToggled() && shadows.cpsCounter.isToggled()) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(cpsPosH.getValue(), cpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, cpsRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(cpsPosH.getValue(), cpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, cpsRadius.getValue(), cpsColor.getColor());
                mc.fontRendererObj.drawString(text,cpsPosH.getValue() + 2.5f ,cpsPosV.getValue() + 2.5f, cpsTextColor.getColor().getRGB(), cpsTextShadow.isToggled());
            }

            if (hudElements.get("BreakIndicator") && (mc.playerController.curBlockDamageMP > 0 || Client.INSTANCE.getModuleManager().getModule(ClickGui.class).isToggled())) {
                String text = "Progress: " + String.format("%.0f", mc.playerController.curBlockDamageMP * 100) + "%";
                float width = mc.fontRendererObj.getStringWidth(text);

                if (shadows.isToggled() && shadows.breakIndicator.isToggled()) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(breakIndicatorPosH.getValue(), breakIndicatorPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, breakIndicatorRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(breakIndicatorPosH.getValue(), breakIndicatorPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, breakIndicatorRadius.getValue(), breakIndicatorColor.getColor());
                mc.fontRendererObj.drawString(text, breakIndicatorPosH.getValue() + 2.5f, breakIndicatorPosV.getValue() + 2.5f, breakIndicatorTextColor.getColor().getRGB(), breakIndicatorTextShadow.isToggled());
            }

            if (hudElements.get("WaterMark")) {

            }
        }
    }
}
