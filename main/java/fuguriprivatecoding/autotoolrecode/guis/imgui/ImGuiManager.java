package fuguriprivatecoding.autotoolrecode.guis.imgui;

import com.github.koxx12dev.fuckyou.ImGuiGL3;
import com.github.koxx12dev.fuckyou.ImGuiLwjgl2;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

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

        Client.INST.getEventManager().register(this);
    }

    public long getMinecraftWindowHandle() {
        final String[] windowTitle = new String[1];

        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            char[] title = new char[512];
            User32.INSTANCE.GetWindowText(hwnd, title, title.length);
            String titleStr = Native.toString(title);

            if (titleStr.contains("Minecraft")) {
                windowTitle[0] = titleStr;
                return false;
            }
            return true;
        }, null);

        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle[0]);
        return hwnd != null ? Pointer.nativeValue(hwnd.getPointer()) : -1;
    }

    private static void loadFonts() {
        ImFontAtlas atlas = ImGui.getIO().getFonts();
        ImFontConfig config = new ImFontConfig();
        config.setGlyphRanges(atlas.getGlyphRangesCyrillic());

        try {
            InputStream is = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(new ResourceLocation("minecraft", "hackclient/fonts/SFPro.ttf"))
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

    public static ImGuiWindow getWindowByName(String name) {
        for (ImGuiWindow window : windows) {
            if (window.getName().equalsIgnoreCase(name)) {
                return window;
            }
        }
        return null;
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