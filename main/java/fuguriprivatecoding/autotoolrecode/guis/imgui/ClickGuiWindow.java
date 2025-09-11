package fuguriprivatecoding.autotoolrecode.guis.imgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import imgui.ImGui;
import imgui.type.ImInt;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class ClickGuiWindow extends ImGuiWindow {
    public ClickGuiWindow() {
        super("ClickGui");
    }

    private Module featureListeningForKey;
    private Category selectedCategory = Category.COMBAT;

    @Override
    protected void renderContent() {
        ImGui.getStyle().setWindowRounding(10);
        ImGui.getStyle().setPopupRounding(10);
        ImGui.getStyle().setTabRounding(10);

        if (ImGui.begin(name)) {
            String[] categoryNames = Arrays.stream(Category.values())
                    .map(category -> category.name().toLowerCase())
                    .toArray(String[]::new);

            ImInt currentIndex = new ImInt(selectedCategory.ordinal());
            if (ImGui.combo("Category", currentIndex, categoryNames)) {
                selectedCategory = Category.values()[currentIndex.get()];
            }

            ImGui.separator();
            for (Module feature : Client.INST.getModuleManager().getModules()) {
                if (feature.getCategory() != selectedCategory) continue;
                ImGui.pushID(feature.hashCode());

                String displayName = feature.getName();

                if (featureListeningForKey == feature) {
                    displayName = "...";
                    for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++) {
                        if (ImGui.isKeyPressed(i)) {
                            featureListeningForKey.setKey(i);
                            featureListeningForKey = null;
                            break;
                        }
                    }
                }

                if (ImGui.checkbox("", feature.isToggled())) feature.toggle();
                ImGui.sameLine();

                boolean opened = ImGui.collapsingHeader(displayName);

                if (ImGui.isItemHovered() && ImGui.isMouseClicked(2)) {
                    featureListeningForKey = feature;
                }

                if (opened) {
                    ImGui.pushID(feature.hashCode());
                    ImGui.indent();

                    for (Setting setting : feature.getSettings()) {
                        if (setting.isVisible()) {
                            setting.render();
                        }
                    }

                    ImGui.unindent();
                    ImGui.popID();
                }

                ImGui.popID();
            }
        }
        ImGui.end();
    }
}