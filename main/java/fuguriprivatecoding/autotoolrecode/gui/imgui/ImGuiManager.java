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
        style.setWindowPadding(20.0f, 20.0f);
        style.setWindowRounding(11.5f);
        style.setWindowBorderSize(0.0f);
        style.setWindowMinSize(20.0f, 20.0f);
        style.setWindowTitleAlign(0.5f, 0.5f);
        style.setWindowMenuButtonPosition(ImGuiDir.None);
        style.setChildRounding(20.0f);
        style.setChildBorderSize(1.0f);
        style.setPopupRounding(17.4f);
        style.setPopupBorderSize(1.0f);
        style.setFramePadding(20.0f, 3.4f);
        style.setFrameRounding(11.9f);
        style.setFrameBorderSize(0.0f);
        style.setItemSpacing(8.9f, 13.4f);
        style.setItemInnerSpacing(7.1f, 1.8f);
        style.setCellPadding(12.1f, 9.2f);
        style.setIndentSpacing(0.0f);
        style.setColumnsMinSpacing(8.7f);
        style.setScrollbarSize(11.6f);
        style.setScrollbarRounding(15.9f);
        style.setGrabMinSize(3.7f);
        style.setGrabRounding(20.0f);
        style.setTabRounding(9.8f);
        style.setTabBorderSize(0.0f);
        style.setTabMinWidthForCloseButton(0.0f);
        style.setColorButtonPosition(ImGuiDir.Right);
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setSelectableTextAlign(0.0f, 0.0f);

        style.setColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.TextDisabled, 0.27450982f, 0.31764707f, 0.4509804f, 1.0f);
        style.setColor(ImGuiCol.WindowBg, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.ChildBg, 0.09411765f, 0.101960786f, 0.11764706f, 1.0f);
        style.setColor(ImGuiCol.PopupBg, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.Border, 0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.BorderShadow, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.FrameBg, 0.11372549f, 0.1254902f, 0.15294118f, 1.0f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.FrameBgActive, 0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.TitleBg, 0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TitleBgActive, 0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.MenuBarBg, 0.09803922f, 0.105882354f, 0.12156863f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.15686275f, 0.16862746f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.CheckMark, 0.03137255f, 0.9490196f, 0.84313726f, 1.0f);
        style.setColor(ImGuiCol.SliderGrab, 0.03137255f, 0.9490196f, 0.84313726f, 1.0f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.6f, 0.9647059f, 0.03137255f, 1.0f);
        style.setColor(ImGuiCol.Button, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.ButtonHovered, 0.18039216f, 0.1882353f, 0.19607843f, 1.0f);
        style.setColor(ImGuiCol.ButtonActive, 0.15294118f, 0.15294118f, 0.15294118f, 1.0f);
        style.setColor(ImGuiCol.Header, 0.14117648f, 0.16470589f, 0.20784314f, 1.0f);
        style.setColor(ImGuiCol.HeaderHovered, 0.105882354f, 0.105882354f, 0.105882354f, 1.0f);
        style.setColor(ImGuiCol.HeaderActive, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.Separator, 0.12941177f, 0.14901961f, 0.19215687f, 1.0f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.SeparatorActive, 0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.ResizeGrip, 0.14509805f, 0.14509805f, 0.14509805f, 1.0f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.03137255f, 0.9490196f, 0.84313726f, 1.0f);
        style.setColor(ImGuiCol.ResizeGripActive, 1.0f, 1.0f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.Tab, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.TabHovered, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.TabActive, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.TabUnfocused, 0.078431375f, 0.08627451f, 0.101960786f, 1.0f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.1254902f, 0.27450982f, 0.57254905f, 1.0f);
        style.setColor(ImGuiCol.PlotLines, 0.52156866f, 0.6f, 0.7019608f, 1.0f);
        style.setColor(ImGuiCol.PlotLinesHovered, 0.039215688f, 0.98039216f, 0.98039216f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogram, 0.03137255f, 0.9490196f, 0.84313726f, 1.0f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.15686275f, 0.18431373f, 0.2509804f, 1.0f);
        style.setColor(ImGuiCol.TableHeaderBg, 0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TableBorderStrong, 0.047058824f, 0.05490196f, 0.07058824f, 1.0f);
        style.setColor(ImGuiCol.TableBorderLight, 0.0f, 0.0f, 0.0f, 1.0f);
        style.setColor(ImGuiCol.TableRowBg, 0.11764706f, 0.13333334f, 0.14901961f, 1.0f);
        style.setColor(ImGuiCol.TableRowBgAlt, 0.09803922f, 0.105882354f, 0.12156863f, 1.0f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.9372549f, 0.9372549f, 0.9372549f, 1.0f);
        style.setColor(ImGuiCol.DragDropTarget, 0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavHighlight, 0.26666668f, 0.2901961f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 0.49803922f, 0.5137255f, 1.0f, 1.0f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.19607843f, 0.1764706f, 0.54509807f, 0.5019608f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.19607843f, 0.1764706f, 0.54509807f, 0.5019608f);
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