package fuguriprivatecoding.autotoolrecode.gui.imgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.ClickGui;
import fuguriprivatecoding.autotoolrecode.setting.Setting;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import org.lwjgl.input.Keyboard;

public class ClickGuiWindow extends ImGuiWindow {

    public ClickGuiWindow() {
        super("ClickGui");
    }

    public Module featureListeningForKey;
    private Category selectedCategory = Category.COMBAT;

    private Module selectedModule;

    @Override
    protected void renderContent() {
        ImGuiManager.removeWindow(ClickGui.window);
        ImGui.getStyle().setWindowRounding(10);
        ImGui.getStyle().setPopupRounding(10);
        ImGui.getStyle().setTabRounding(10);

        Client.imGuiManager.setStyles();

        ImGui.begin(name);

        ImGui.beginChild("TopBar", 0, 55, true);
        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;

            if (selected) {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.5f, 0.2f, 0.8f, 1.0f);
            }

            if (ImGui.button(category.getName())) {
                selectedCategory = category;
                selectedModule = null;
            }

            if (selected) ImGui.popStyleColor();

            ImGui.sameLine();
        }

        ImGui.endChild();

        ImGui.beginChild("Main", 0, 0, false);

        ImGui.beginChild("Modules", 200, 0, true);

        for (Module feature : Modules.getModules()) {
            if (feature.getCategory() != selectedCategory) continue;

            boolean selected = feature == selectedModule;

            if (selected) {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.1f, 0.2f, 1, 1.0f);
            }

            if (ImGui.button(feature.getName(), -1, 30)) {
                selectedModule = feature;
            }

            if (selected) ImGui.popStyleColor();
        }

        ImGui.endChild();
        ImGui.sameLine();
        ImGui.beginChild("Settings", 0, 0, true);

        if (selectedModule != null) {
            ImGui.text(selectedModule.getCategory().getName() + " / " + selectedModule.getName());

            ImGui.sameLine();

            ImGui.setCursorPosX(ImGui.getWindowWidth() - 60);
            if (ImGui.radioButton("##toggle", selectedModule.isToggled())) {
                selectedModule.toggle();
            }

            if (featureListeningForKey == selectedModule) {
                ImGui.text("Press key...");
                for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++) {
                    if (ImGui.isKeyPressed(i)) {
                        selectedModule.setKey(i);
                        featureListeningForKey = null;
                    }
                }
            }

            if (ImGui.isItemHovered() && ImGui.isMouseClicked(2)) {
                featureListeningForKey = selectedModule;
            }

            ImGui.spacing();

            for (Setting setting : selectedModule.getSettings()) {
                if (setting.isVisible()) {
                    setting.render();
                }
            }
        } else {
            ImGui.text("Select module");
        }

        ImGui.endChild();
        ImGui.endChild();

        ImGui.end();
    }
}