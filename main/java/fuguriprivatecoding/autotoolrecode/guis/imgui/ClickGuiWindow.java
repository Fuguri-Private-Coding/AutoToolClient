package fuguriprivatecoding.autotoolrecode.guis.imgui;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.guis.clickgui.ClickGuiScreen;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.settings.Setting;
import imgui.ImGui;
import imgui.type.ImInt;

import java.util.Arrays;

public class ClickGuiWindow extends ImGuiWindow {
    public ClickGuiWindow() {
        super("ClickGui");
    }

    private Category selectedCategory = Category.COMBAT;

    @Override
    protected void renderContent() {
        // Category select
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

            // Добавляем индикатор ON/OFF к названию для визуализации состояния
            String displayName = feature.getName() + (feature.isToggled() ? " [ON]" : " [OFF]");

            // Если раскрыто, показываем настройки (здесь добавьте ваш код для настроек модуля)
            if (ImGui.collapsingHeader(displayName)) {
                if (ImGui.checkbox("toggled", feature.isToggled())) {
                    feature.toggle();
                }
                for (Setting setting : feature.getSettings()) {
                    if (!setting.isVisible()) {
                        continue;
                    }

//                    setting.render();
                }
                // Пример: ImGui.sliderFloat("Some setting", ...);
                // Или другие элементы ImGui для настроек, которые вы напишете сами
                // ImGui.text("Settings for " + feature.getName());
                // ...
            }
        }
    }
}