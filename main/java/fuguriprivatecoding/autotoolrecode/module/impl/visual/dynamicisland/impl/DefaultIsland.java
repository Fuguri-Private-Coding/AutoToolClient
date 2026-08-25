package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.TextureUtils;
import net.minecraft.util.ResourceLocation;

public class DefaultIsland extends IslandComponent {

    public static IslandComponent create(IslandContext ctx) {
        return new DefaultIsland();
    }

    @Override
    public String getKey(IslandContext ctx) {
        return "default:" + (ctx.mediaController.getSongLocation() != null
                ? ctx.info.title() + ctx.info.artist() : "");
    }

    @Override
    public float getWidth(IslandContext ctx) {
        return ctx.regular.width(Client.getFullName(), 8)
                + (ctx.mediaController.getSongLocation() != null ? ctx.imageSize.getValue() : 0);
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public void draw(IslandContext ctx) {
        ResourceLocation songImage = ctx.mediaController.getSongLocation();
        float img = ctx.imageSize.getValue();
        float alpha = ctx.textAlpha.getValue();

        if (songImage != null) {
            TextureUtils.texture(songImage, (2.5f - img / 2f) * ctx.imageSize.getProgress(),
                    (2.5f - img / 2f) * ctx.imageSize.getProgress(), img, img, 5, 1f, Colors.WHITE.withAlpha(alpha));
        }

        ctx.regular.draw(Client.getFullName(), 10, 0, 8, ctx.whiteColor.withAlpha(alpha));
    }
}
