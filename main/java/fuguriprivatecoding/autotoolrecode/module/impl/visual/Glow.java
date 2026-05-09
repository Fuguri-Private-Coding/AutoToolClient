package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.shader.impl.BloomUtils;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

@ModuleInfo(name = "Glow", category = Category.VISUAL, description = "Бесплатное свечение скачать.")
public class Glow extends Module {

    public MultiMode toGlow = new MultiMode("ToGlow", this)
        .addModes("Chat", "Players");

    public IntegerSetting radius = new IntegerSetting("Radius", this, 1, 35, 6);
    public FloatSetting brightness = new FloatSetting("Brightness", this, 0,3,1,0.1f);
    public FloatSetting offset1 = new FloatSetting("Offset1", this, 1,5,1,0.1f);
    public FloatSetting offset2 = new FloatSetting("Offset2", this, 1,5,1,0.1f);

    public final ColorSetting chatColor = new ColorSetting("ChatColor", this, () -> toGlow.get("Chat"));

    public final ColorSetting playersColor = new ColorSetting("PlayersColor", this, () -> toGlow.get("Players"));

    private final CheckBox changeHurtColor = new CheckBox("ChangeHurtColor", this, () -> toGlow.get("Players"), true);
    private final CheckBox instantChangeColor = new CheckBox("InstantHurtColor", this, () -> toGlow.get("Players") && changeHurtColor.isToggled(), true);
    public final ColorSetting hurtPlayersColor = new ColorSetting("HurtPlayersColor", this, () -> toGlow.get("Players") && changeHurtColor.isToggled());

    @Override
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent && toGlow.get("Players")) {
            BloomUtils.startWrite();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                float instantHurtTime = player.hurtTime > 0 ? 1 : 0;
                float hurtTime = instantChangeColor.isToggled() ? instantHurtTime : player.hurtTime / 10f;

                Color color = playersColor.getInterpolateColor(hurtPlayersColor.getFadedColor(), hurtTime);

                if (!shouldContinueRender(player)) RenderUtils.renderPlayer(player, RenderUtils.getAbsoluteSmoothPos(player.getLastPositionVector(), player.getPositionVector()).subtract(RenderManager.getRenderPosition()), player.rotationYawHead, mc.timer.renderPartialTicks, color);
            }
            BloomUtils.stopWrite();
        }
    }

    private boolean shouldContinueRender(Entity player) {
        return mc.getRenderManager() == null || (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) || player.isDead;
    }
}

