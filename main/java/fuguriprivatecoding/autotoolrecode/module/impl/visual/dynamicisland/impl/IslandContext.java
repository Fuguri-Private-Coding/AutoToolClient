package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.GuiScreen;
import smtc.TrackInfo;

public class IslandContext {
    public final MsdfFont bold;
    public final MsdfFont regular;
    public final Colors whiteColor;
    public final MediaController mediaController;
    public final TrackInfo info;
    public final EasingAnimation imageSize;
    public final EasingAnimation textAlpha;
    public final boolean player;
    public final GuiScreen currentScreen;

    public IslandContext(MsdfFont bold, MsdfFont regular, Colors whiteColor, MediaController mediaController,
                         TrackInfo info, EasingAnimation imageSize, EasingAnimation textAlpha,
                         boolean player, GuiScreen currentScreen) {
        this.bold = bold;
        this.regular = regular;
        this.whiteColor = whiteColor;
        this.mediaController = mediaController;
        this.info = info;
        this.imageSize = imageSize;
        this.textAlpha = textAlpha;
        this.player = player;
        this.currentScreen = currentScreen;
    }
}
