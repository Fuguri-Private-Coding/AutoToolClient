package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.ColorSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.MultiMode;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "HUD", category = Category.VISUAL)
public class HUD extends Module {

    MultiMode hudElements = new MultiMode("HUDElements", this)
            .add("FPSCounter")
            .add("BPSCounter")
            .add("CPSCounter")
            .add("BreakIndicator")
            .add("WaterMark");

    FloatSetting fpsPosH = new FloatSetting("FPSPosX", this,() -> hudElements.get("FPSCounter"), 0,10000,0,0.1f);
    FloatSetting fpsPosV = new FloatSetting("FPSPosY", this,() -> hudElements.get("FPSCounter"), 0,10000,0,0.1f);
    FloatSetting fpsRadius = new FloatSetting("FPSRadius", this,() -> hudElements.get("FPSCounter"), 0.5f,5,1f,0.1f);
    ColorSetting fpsColor = new ColorSetting("FPSColor", this,() -> hudElements.get("FPSCounter"), 0,0,0,0.4f);
    CheckBox fpsTextShadow = new CheckBox("FPSTextShadow",this,() -> hudElements.get("FPSCounter"), true);
    ColorSetting fpsTextColor = new ColorSetting("FPSTextColor", this,() -> hudElements.get("FPSCounter"), 1,1,1,1);

    FloatSetting bpsPosH = new FloatSetting("BPSPosX", this,() -> hudElements.get("BPSCounter"), 0,10000,0,0.1f);
    FloatSetting bpsPosV = new FloatSetting("BPSPosY", this,() -> hudElements.get("BPSCounter"), 0,10000,0,0.1f);
    FloatSetting bpsRadius = new FloatSetting("BPSRadius", this,() -> hudElements.get("BPSCounter"), 0.5f,5,1f,0.1f);
    ColorSetting bpsColor = new ColorSetting("BPSColor", this,() -> hudElements.get("BPSCounter"), 0,0,0,0.4f);
    CheckBox bpsTextShadow = new CheckBox("BPSTextShadow",this,() -> hudElements.get("BPSCounter"), true);
    ColorSetting bpsTextColor = new ColorSetting("BPSTextColor", this,() -> hudElements.get("BPSCounter"), 1,1,1,1);
    CheckBox includeY = new CheckBox("BPSIncludeY", this,() -> hudElements.get("BPSCounter"), false);

    FloatSetting cpsPosH = new FloatSetting("CPSPosX", this,() -> hudElements.get("CPSCounter"), 0,10000,0,0.1f);
    FloatSetting cpsPosV = new FloatSetting("CPSPosY", this,() -> hudElements.get("CPSCounter"), 0,10000,0,0.1f);
    FloatSetting cpsRadius = new FloatSetting("CPSRadius", this,() -> hudElements.get("CPSCounter"), 0.5f,5,1f,0.1f);
    ColorSetting cpsColor = new ColorSetting("CPSColor", this,() -> hudElements.get("CPSCounter"), 0,0,0,0.4f);
    CheckBox cpsTextShadow = new CheckBox("CPSTextShadow",this,() -> hudElements.get("CPSCounter"), true);
    ColorSetting cpsTextColor = new ColorSetting("CPSTextColor", this,() -> hudElements.get("CPSCounter"), 1,1,1,1);

    FloatSetting breakIndicatorPosH = new FloatSetting("BreakIndicatorPosX", this,() -> hudElements.get("BreakIndicator"), 0,10000,0,0.1f);
    FloatSetting breakIndicatorPosV = new FloatSetting("BreakIndicatorPosY", this,() -> hudElements.get("BreakIndicator"), 0,10000,0,0.1f);
    FloatSetting breakIndicatorRadius = new FloatSetting("BreakIndicatorRadius", this,() -> hudElements.get("BreakIndicator"), 0.5f,5,1f,0.1f);
    ColorSetting breakIndicatorColor = new ColorSetting("BreakIndicatorColor", this,() -> hudElements.get("BreakIndicator"), 0,0,0,0.4f);
    CheckBox breakIndicatorTextShadow = new CheckBox("BreakIndicatorTextShadow",this,() -> hudElements.get("BreakIndicator"), true);
    ColorSetting breakIndicatorTextColor = new ColorSetting("BreakIndicatorTextColor", this,() -> hudElements.get("BreakIndicator"), 1,1,1,1);

    FloatSetting waterMarkPosH = new FloatSetting("WaterMarkPosX", this,() -> hudElements.get("WaterMark"), 0,10000,0,0.1f);
    FloatSetting waterMarkPosV = new FloatSetting("WaterMarkPosY", this,() -> hudElements.get("WaterMark"), 0,10000,0,0.1f);
    FloatSetting waterMarkRadius = new FloatSetting("WaterMarkRadius", this,() -> hudElements.get("WaterMark"), 0.5f,5,1f,0.1f);
    ColorSetting waterMarkColor = new ColorSetting("WaterMarkColor", this,() -> hudElements.get("WaterMark"), 0,0,0,0.4f);
    CheckBox waterMarkTextShadow = new CheckBox("WaterMarkTextShadow",this,() -> hudElements.get("WaterMark"), true);
    ColorSetting waterMarkTextColor = new ColorSetting("WaterMarkTextColor", this,() -> hudElements.get("WaterMark"), 1,1,1,1);

    Shadows shadows;

    private final java.util.List<Long> leftClicks = new CopyOnWriteArrayList<>();
    private final List<Long> rightClicks = new CopyOnWriteArrayList<>();
    private final List<Long> frames = new CopyOnWriteArrayList<>();

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        ScaledResolution sc = new ScaledResolution(mc);

        bpsPosH.setMax(sc.getScaledWidth());
        bpsPosV.setMax(sc.getScaledHeight());
        cpsPosH.setMax(sc.getScaledWidth());
        cpsPosV.setMax(sc.getScaledHeight());
        breakIndicatorPosH.setMax(sc.getScaledWidth());
        breakIndicatorPosV.setMax(sc.getScaledHeight());
        waterMarkPosH.setMax(sc.getScaledWidth());
        waterMarkPosV.setMax(sc.getScaledHeight());
        fpsPosH.setMax(sc.getScaledWidth());
        fpsPosV.setMax(sc.getScaledHeight());

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

                if (shadows.isToggled() && shadows.module.get("BPSCounter"))
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

                if (shadows.isToggled() && shadows.module.get("CPSCounter")) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(cpsPosH.getValue(), cpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, cpsRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(cpsPosH.getValue(), cpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, cpsRadius.getValue(), cpsColor.getColor());
                mc.fontRendererObj.drawString(text,cpsPosH.getValue() + 2.5f ,cpsPosV.getValue() + 2.5f, cpsTextColor.getColor().getRGB(), cpsTextShadow.isToggled());
            }

            if (hudElements.get("BreakIndicator") && (mc.playerController.curBlockDamageMP > 0 || Client.INST.getModuleManager().getModule(ClickGui.class).isToggled())) {
                String text = "Progress: " + String.format("%.0f", mc.playerController.curBlockDamageMP * 100) + "%";
                float width = mc.fontRendererObj.getStringWidth(text);

                if (shadows.isToggled() && shadows.module.get("BreakIndicator")) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(breakIndicatorPosH.getValue(), breakIndicatorPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, breakIndicatorRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(breakIndicatorPosH.getValue(), breakIndicatorPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, breakIndicatorRadius.getValue(), breakIndicatorColor.getColor());
                mc.fontRendererObj.drawString(text, breakIndicatorPosH.getValue() + 2.5f, breakIndicatorPosV.getValue() + 2.5f, breakIndicatorTextColor.getColor().getRGB(), breakIndicatorTextShadow.isToggled());
            }

            if (hudElements.get("WaterMark")) {

            }

            if (hudElements.get("FPSCounter")) {
                frames.add(System.currentTimeMillis());
                frames.removeIf(aLong -> System.currentTimeMillis() - aLong >= 1000);

                String text = "FPS: " + frames.size();
                float width = mc.fontRendererObj.getStringWidth(text);

                if (shadows.isToggled() && shadows.module.get("FPSCounter")) BloomUtils.addToDraw(() -> RoundedUtils.drawRect(fpsPosH.getValue(), fpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, fpsRadius.getValue(), Color.WHITE));

                RoundedUtils.drawRect(fpsPosH.getValue(), fpsPosV.getValue(), width + 4, mc.fontRendererObj.FONT_HEIGHT + 4, fpsRadius.getValue(), fpsColor.getColor());
                mc.fontRendererObj.drawString(text,fpsPosH.getValue() + 2.5f ,fpsPosV.getValue() + 2.5f, fpsTextColor.getColor().getRGB(), fpsTextShadow.isToggled());
            }
        }
    }
}
