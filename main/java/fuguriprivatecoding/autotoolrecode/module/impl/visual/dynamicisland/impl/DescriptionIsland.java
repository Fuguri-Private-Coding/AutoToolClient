package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.gui.clickgui.ClickScreen;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;

public class DescriptionIsland extends IslandComponent {
    private final String text;

    public DescriptionIsland(String text) {
        this.text = text;
    }

    public static IslandComponent create(IslandContext ctx) {
        if (!(ctx.currentScreen() instanceof ClickScreen)) {
            return null;
        }

        for (Module module : Modules.getModulesByCategory(ClickScreen.selectedCategory)) {
            if (module.isHovered() && !module.getDescription().equalsIgnoreCase("")) {
                return new DescriptionIsland(module.getDescription());
            }
        }

        return null;
    }

    @Override
    public String getKey(IslandContext ctx) {
        return text;
    }

    @Override
    public float getWidth(IslandContext ctx) {
        return ctx.regular().width(text, 8);
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void draw(IslandContext ctx) {
        ctx.regular().draw(text, 0, 0, 8, ctx.whiteColor().withAlpha(ctx.textAlpha().getValue()));
    }
}
