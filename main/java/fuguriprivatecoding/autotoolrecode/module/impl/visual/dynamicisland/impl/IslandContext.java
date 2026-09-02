package fuguriprivatecoding.autotoolrecode.module.impl.visual.dynamicisland.impl;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.utils.music.MediaController;
import fuguriprivatecoding.autotoolrecode.utils.render.color.Colors;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.msdf.MsdfFont;
import net.minecraft.client.gui.GuiScreen;
import smtc.TrackInfo;

public record IslandContext(MsdfFont bold, MsdfFont regular, Colors whiteColor, MediaController mediaController,
                            TrackInfo info, EasingAnimation imageSize, EasingAnimation textAlpha, boolean player,
                            GuiScreen currentScreen) {
}
