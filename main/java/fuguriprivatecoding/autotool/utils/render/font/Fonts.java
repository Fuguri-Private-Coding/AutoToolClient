package fuguriprivatecoding.autotool.utils.render.font;

import net.minecraft.util.ResourceLocation;

public class Fonts {
    private static final int MAX_FONT_SIZE = 48;
    public static FontContainer[] roboto          = new FontContainer[MAX_FONT_SIZE];
    public static FontContainer[] jetBrains       = new FontContainer[MAX_FONT_SIZE];
    public static FontContainer[] robotoExtraBold = new FontContainer[MAX_FONT_SIZE];

    public static void init() {
        process(roboto, "Roboto");
        process(jetBrains, "JetBrains");
        process(robotoExtraBold, "Roboto-Black");
    }

    private static ResourceLocation getLoc(String name) {
        return new ResourceLocation("hackclient/fonts" + name + ".ttf");
    }

    private static void process(FontContainer[] fontContainer, String name) {
        for (int i = 1; i <= MAX_FONT_SIZE; i++) {
            fontContainer[i - 1] = new FontContainer(getLoc(name), "Default", i);
        }
    }
}
