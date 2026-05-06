package fuguriprivatecoding.autotoolrecode.gui.imgui;

import com.github.koxx12dev.fuckyou.ImGuiGL3;
import com.github.koxx12dev.fuckyou.ImGuiLwjgl2;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.KeyEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render2DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.RenderScreenEvent;
import fuguriprivatecoding.autotoolrecode.module.impl.client.ClientSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImGuiManager implements Imports, EventListener {
    private static ImGuiLwjgl2 imGuiGlfw;
    private static ImGuiGL3 imGuiGl3;
    private static final List<ImGuiWindow> windows = new ArrayList<>();

    public static ImFont font;

    public void init() {
        ImGui.createContext();

        imGuiGlfw = new ImGuiLwjgl2();
        imGuiGl3 = new ImGuiGL3();

        imGuiGlfw.init();
        loadFonts();
        imGuiGl3.init("#version 150");

        setStyles();

        Events.register(this);
    }

    private static void loadFonts() {
        ImFontAtlas atlas = ImGui.getIO().getFonts();
        ImFontConfig config = new ImFontConfig();
        config.setGlyphRanges(atlas.getGlyphRangesCyrillic());

        try (InputStream is = Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(Client.of("fonts/SFProRegular.otf"))
            .getInputStream()) {

            byte[] fontData = new byte[is.available()];
            is.read(fontData);

            if (fontData.length == 0) {
                System.err.println("Font data is empty!");
                return;
            }

            font = atlas.addFontFromMemoryTTF(fontData, 20, config);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        atlas.build();
    }

    public void setStyles() {
        ImGuiStyle style = ImGui.getStyle();

        style.setAlpha(1.0f);
        style.setDisabledAlpha(1.0f);
        style.setWindowPadding(12.0f, 12.0f);
        style.setWindowRounding(15f);
        style.setWindowBorderSize(1f);
        style.setWindowMinSize(20.0f, 20.0f);
        style.setWindowTitleAlign(0.5f, 0.5f);
        style.setWindowMenuButtonPosition(ImGuiDir.None);
        style.setChildRounding(0.0f);
        style.setChildBorderSize(1f);
        style.setPopupRounding(10f);
        style.setPopupBorderSize(1.0f);
        style.setFramePadding(6.0f, 6.0f);
        style.setFrameRounding(0.0f);
        style.setFrameBorderSize(0f);
        style.setItemSpacing(6, 6.0f);
        style.setItemInnerSpacing(6.0f, 3.0f);
        style.setCellPadding(6, 6.0f);
        style.setIndentSpacing(20.0f);
        style.setColumnsMinSpacing(6.0f);
        style.setScrollbarSize(12.0f);
        style.setScrollbarRounding(0.0f);
        style.setGrabMinSize(12.0f);
        style.setGrabRounding(0.0f);
        style.setTabRounding(0.0f);
        style.setTabBorderSize(0.0f);
        style.setTabMinWidthForCloseButton(0.0f);
        style.setColorButtonPosition(ImGuiDir.Right);
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setSelectableTextAlign(0.0f, 0.0f);

        // Colors
        style.setColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.TextDisabled,0.27450982f, 0.31764707f, 0.4509804f, 1.0f);
        style.setColor(ImGuiCol.WindowBg,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.ChildBg,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.PopupBg,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.Border,0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.BorderShadow,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.FrameBg,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.FrameBgHovered,0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.FrameBgActive,0.23529412f, 0.21568628f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.TitleBg,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TitleBgActive,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TitleBgCollapsed,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.MenuBarBg,0.09803922f, 0.105882354f, 0.12156863f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarBg,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrab,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered,0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrabActive,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.CheckMark,0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.SliderGrab,0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.SliderGrabActive,0.5372549f, 0.5529412f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.Button,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.ButtonHovered,0.19607843f, 0.1764706f, 0.54509807f, 1.0f);
        style.setColor(ImGuiCol.ButtonActive,0.23529412f, 0.21568628f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.Header,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.HeaderHovered,0.19607843f, 0.1764706f, 0.54509807f, 1.0f);
        style.setColor(ImGuiCol.HeaderActive,0.23529412f, 0.21568628f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.Separator,0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.SeparatorHovered,0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.SeparatorActive,0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.ResizeGrip,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.ResizeGripHovered,0.19607843f, 0.1764706f, 0.54509807f, 1.0f);
        style.setColor(ImGuiCol.ResizeGripActive,0.23529412f, 0.21568628f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.Tab,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TabHovered,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.TabActive,0.09803922f, 0.105882354f, 0.12156863f, 1.0f);
        style.setColor(ImGuiCol.TabUnfocused,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TabUnfocusedActive,0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.PlotLines,0.52156866f, 0.6f, 0.7019608f, 1.0f);
        style.setColor(ImGuiCol.PlotLinesHovered,0.039215688f, 0.98039216f, 0.98039216f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogram,1.0f, 0.2901961f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogramHovered,0.99607843f, 0.4745098f, 0.69803923f, 1.0f);
        style.setColor(ImGuiCol.TableHeaderBg,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TableBorderStrong,0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TableBorderLight,0.0f, 0.0f, 0.0f, 1.0f);
        style.setColor(ImGuiCol.TableRowBg,0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.TableRowBgAlt,0.09803922f, 0.105882354f, 0.12156863f, 1.0f);
        style.setColor(ImGuiCol.TextSelectedBg,0.23529412f, 0.21568628f, 0.59607846f, 1.0f);
        style.setColor(ImGuiCol.DragDropTarget,0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavHighlight,0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavWindowingHighlight,0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavWindowingDimBg,0.19607843f, 0.1764706f, 0.54509807f, 0.5019608f);
        style.setColor(ImGuiCol.ModalWindowDimBg,0.19607843f, 0.1764706f, 0.54509807f, 0.5019608f);
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

    @Override
    public boolean listen() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer == null) return;
        if (event instanceof RenderScreenEvent) {
            renderAll();
        }

        if (event instanceof Render2DEvent) {
            imGuiGlfw.scrollCallback(ClientSettings.getScroll());
        }

        if (event instanceof KeyEvent e) {
            imGuiGlfw.charCallback(e.getKey());
        }
    }
}