package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.GaussianBlurUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "HUD", category = Category.VISUAL)
public class HUD extends Module {
    private final MultiMode hudElements = new MultiMode("HUDElements", this)
            .addModes("FPSCounter", "BPSCounter", "BreakIndicator");

    private final FloatSetting fpsPosH = createPositionSetting("FPSPosX", "FPSCounter");
    private final FloatSetting fpsPosV = createPositionSetting("FPSPosY", "FPSCounter");
    private final FloatSetting fpsRadius = createRadiusSetting("FPSRadius", "FPSCounter");
    private final ColorSetting fpsColor = createBgColorSetting("FPSColor", "FPSCounter");
    private final CheckBox fpsTextShadow = createTextShadowSetting("FPSTextShadow", "FPSCounter");
    private final ColorSetting fpsTextColor = createTextColorSetting("FPSTextColor", "FPSCounter");

    private final FloatSetting bpsPosH = createPositionSetting("BPSPosX", "BPSCounter");
    private final FloatSetting bpsPosV = createPositionSetting("BPSPosY", "BPSCounter");
    private final FloatSetting bpsRadius = createRadiusSetting("BPSRadius", "BPSCounter");
    private final ColorSetting bpsColor = createBgColorSetting("BPSColor", "BPSCounter");
    private final CheckBox bpsTextShadow = createTextShadowSetting("BPSTextShadow", "BPSCounter");
    private final ColorSetting bpsTextColor = createTextColorSetting("BPSTextColor", "BPSCounter");
    private final CheckBox includeY = new CheckBox("BPSIncludeY", this, () -> hudElements.get("BPSCounter"), false);

    private final FloatSetting breakIndicatorPosH = createPositionSetting("BreakIndicatorPosX", "BreakIndicator");
    private final FloatSetting breakIndicatorPosV = createPositionSetting("BreakIndicatorPosY", "BreakIndicator");
    private final FloatSetting breakIndicatorRadius = createRadiusSetting("BreakIndicatorRadius", "BreakIndicator");
    private final ColorSetting breakIndicatorColor = createBgColorSetting("BreakIndicatorColor", "BreakIndicator");
    private final CheckBox breakIndicatorTextShadow = createTextShadowSetting("BreakIndicatorTextShadow", "BreakIndicator");
    private final ColorSetting breakIndicatorTextColor = createTextColorSetting("BreakIndicatorTextColor", "BreakIndicator");

    private static final float PADDING = 2.5f;
    private static final float TEXT_PADDING = 4f;
    private static final long FPS_WINDOW_MS = 1000L;

    private final List<Long> frames = new CopyOnWriteArrayList<>();
    private Shadows shadows;

    private FloatSetting createPositionSetting(String name, String element) {
        return new FloatSetting(name, this, () -> hudElements.get(element), 0, 10000, 0, 0.1f);
    }
    private FloatSetting createRadiusSetting(String name, String element) {
        return new FloatSetting(name, this, () -> hudElements.get(element), 0.5f, 5, 1f, 0.1f);
    }
    private ColorSetting createBgColorSetting(String name, String element) {
        return new ColorSetting(name, this, () -> hudElements.get(element), 0, 0, 0, 0.4f);
    }
    private CheckBox createTextShadowSetting(String name, String element) {
        return new CheckBox(name, this, () -> hudElements.get(element), true);
    }
    private ColorSetting createTextColorSetting(String name, String element) {
        return new ColorSetting(name, this, () -> hudElements.get(element), 1, 1, 1, 1);
    }

    @Override
    public void onEnable() {
        ScaledResolution sc = new ScaledResolution(mc);
        float maxWidth = sc.getScaledWidth();
        float maxHeight = sc.getScaledHeight();

        fpsPosH.setMax(maxWidth);
        fpsPosV.setMax(maxHeight);
        bpsPosH.setMax(maxWidth);
        bpsPosV.setMax(maxHeight);
        breakIndicatorPosH.setMax(maxWidth);
        breakIndicatorPosV.setMax(maxHeight);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (shadows == null) shadows = Client.INST.getModuleManager().getModule(Shadows.class);
        if (event instanceof Render2DEvent) {
            renderBPSCounter();
            renderBreakIndicator();
            renderFPSCounter();
        }
    }

    private void renderBPSCounter() {
        if (!hudElements.get("BPSCounter")) return;

        String text = String.format("%.3f", mc.thePlayer.getBps(includeY.isToggled()));
        renderHudElement(text, bpsPosH.getValue(), bpsPosV.getValue(), bpsRadius.getValue(), bpsColor.getColor(), bpsTextColor.getColor(), bpsTextShadow.isToggled(), "BPSCounter");
    }

    private void renderBreakIndicator() {
        if (!hudElements.get("BreakIndicator")) return;
        if (mc.playerController.curBlockDamageMP <= 0 && !Client.INST.getModuleManager().getModule(ClickGui.class).isToggled()) return;

        String text = "Progress: " + String.format("%.0f", mc.playerController.curBlockDamageMP * 100) + "%";
        renderHudElement(text, breakIndicatorPosH.getValue(), breakIndicatorPosV.getValue(), breakIndicatorRadius.getValue(), breakIndicatorColor.getColor(), breakIndicatorTextColor.getColor(), breakIndicatorTextShadow.isToggled(), "BreakIndicator");
    }

    private void renderFPSCounter() {
        if (!hudElements.get("FPSCounter")) return;

        frames.add(System.currentTimeMillis());
        frames.removeIf(timestamp -> System.currentTimeMillis() - timestamp >= FPS_WINDOW_MS);

        String text = "FPS: " + frames.size();
        renderHudElement(text, fpsPosH.getValue(), fpsPosV.getValue(), fpsRadius.getValue(), fpsColor.getColor(), fpsTextColor.getColor(), fpsTextShadow.isToggled(), "FPSCounter");
    }

    private void renderHudElement(String text, float x, float y, float radius, Color bgColor, Color textColor, boolean shadow, String elementName) {
        float width = mc.fontRendererObj.getStringWidth(text) + TEXT_PADDING;
        float height = mc.fontRendererObj.FONT_HEIGHT + TEXT_PADDING;

        if (shadows != null && shadows.isToggled() && shadows.module.get(elementName)) {
            BloomUtils.addToDraw(() -> RoundedUtils.drawRect(x, y, width, height, radius, Color.WHITE));
        }

        RoundedUtils.drawRect(x, y, width, height, radius, bgColor);
        mc.fontRendererObj.drawString(text, x + PADDING, y + PADDING, textColor.getRGB(), shadow);
    }
}