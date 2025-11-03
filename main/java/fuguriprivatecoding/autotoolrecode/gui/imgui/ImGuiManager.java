package fuguriprivatecoding.autotoolrecode.gui.imgui;

import com.github.koxx12dev.fuckyou.ImGuiGL3;
import com.github.koxx12dev.fuckyou.ImGuiLwjgl2;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import imgui.ImGui;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImGuiManager implements Imports {
    private static ImGuiLwjgl2 imGuiGlfw;
    private static ImGuiGL3 imGuiGl3;
    private static final List<ImGuiWindow> windows = new ArrayList<>();

    public void init() {
        ImGui.createContext();

        imGuiGlfw = new ImGuiLwjgl2();
        imGuiGl3 = new ImGuiGL3();

        imGuiGlfw.init();
        loadFonts();
        imGuiGl3.init("#version 150");

        Client.INST.getEvents().register(this);
    }

    private static void loadFonts() {
        ImFontAtlas atlas = ImGui.getIO().getFonts();
        ImFontConfig config = new ImFontConfig();
        config.setGlyphRanges(atlas.getGlyphRangesCyrillic());

        try {
            InputStream is = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(new ResourceLocation("minecraft", "autotool/fonts/SFProRegular.ttf"))
                    .getInputStream();

            byte[] fontData = new byte[is.available()];
            is.read(fontData);

            if (fontData.length == 0) {
                System.err.println("Font data is empty!");
                return;
            }

            ImFont font = atlas.addFontFromMemoryTTF(fontData, 20, config);
            if (font == null) {
                System.err.println("Failed to load custom font!");
                atlas.addFontDefault();
            }

            is.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        atlas.build();
    }

    public static void addWindow(ImGuiWindow window) {
        windows.add(window);
    }

    public static void removeWindow(ImGuiWindow window) {
        windows.remove(window);
    }

    public static void render(ImGuiWindow imGuiWindow) {
        imGuiGlfw.newFrame(mc.displayWidth, mc.displayHeight, System.currentTimeMillis());
        ImGui.newFrame();

        imGuiWindow.renderContent();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public static void renderAll() {
        for (ImGuiWindow window : windows) {
            render(window);
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 100) return;
        if (event instanceof Render2DEvent) {
            imGuiGlfw.scrollCallback(ClientSettings.getScroll());
        }

        if (event instanceof KeyEvent e) {
            imGuiGlfw.charCallback(e.getKey());
        }
    }
}