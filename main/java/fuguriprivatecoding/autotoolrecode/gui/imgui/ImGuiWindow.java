package fuguriprivatecoding.autotoolrecode.gui.imgui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import lombok.Getter;

public abstract class ImGuiWindow {
    @Getter
    protected final String name;  // Unique window name
    @Getter
    protected boolean isOpen;
    protected ImBoolean openFlag = new ImBoolean(true);

    public ImGuiWindow(String name) {
        this.name = name;
    }

    public void render() {
        if (!isOpen) return;

        if (ImGui.begin(name, openFlag)) {
            renderContent();
        }
        ImGui.end();

        if (!openFlag.get()) {
            isOpen = false;
        }
    }

    protected abstract void renderContent();

    public void show() {
        isOpen = true;
        openFlag.set(true);
    }

    public void hide() {
        isOpen = false;
    }
}