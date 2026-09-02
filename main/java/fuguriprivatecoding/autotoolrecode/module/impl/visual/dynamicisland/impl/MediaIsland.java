package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.IslandComponent;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.TextureUtils;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;

public class MediaIsland extends IslandComponent {

    public static IslandComponent create(IslandContext ctx) {
        return ctx.player() && ctx.currentScreen() != null ? new MediaIsland() : null;
    }

    @Override
    public String getKey(IslandContext ctx) {
        return "media:" + ctx.info().title() + "|" + ctx.info().artist();
    }

    @Override
    public float getWidth(IslandContext ctx) {
        float img = ctx.imageSize().getValue();
        float rectWidthWithImage = img + 45;

        float titleWidth = Math.max(ctx.regular().width(ctx.info().title(), 8), rectWidthWithImage);
        float artistWidth = Math.max(ctx.regular().width(ctx.info().artist(), 6), rectWidthWithImage);

        return Math.max(titleWidth, artistWidth) + img + 5;
    }

    @Override
    public float getHeight() {
        return 25;
    }

    @Override
    public void draw(IslandContext ctx) {
        ResourceLocation songImage = ctx.mediaController().getSongLocation();

        float img = ctx.imageSize().getValue();
        float alpha = ctx.textAlpha().getValue();

        if (songImage != null) {
            TextureUtils.texture(songImage, 0, 0, img, img, 5, 1f, Colors.WHITE.withAlpha(alpha));
        }

        float textX = img + 5;
        float textY = 7;

        float heightTitle = ctx.regular().height(ctx.info().title(), 8);
        Color textColor = ctx.whiteColor().withAlpha(alpha);

        ctx.regular().draw(ctx.info().title(), textX, textY, 8, textColor);
        ctx.regular().draw(ctx.info().artist(), textX, textY + heightTitle + 3, 6, textColor);
    }
}
