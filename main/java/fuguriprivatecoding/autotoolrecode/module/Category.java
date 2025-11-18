package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

@Getter
public enum Category {
	COMBAT("Combat", new ResourceLocation("minecraft", "autotool/category/combat.png")),
	MOVE("Move", new ResourceLocation("minecraft", "autotool/category/move.png")),
	VISUAL("Visual", new ResourceLocation("minecraft", "autotool/category/visual.png")),
	CONNECTION("Connect", new ResourceLocation("minecraft", "autotool/category/connection.png")),
	LEGIT("Legit", new ResourceLocation("minecraft", "autotool/category/legit.png")),
	PLAYER("Player", new ResourceLocation("minecraft", "autotool/category/player.png")),
	MISC("Misc", new ResourceLocation("minecraft", "autotool/category/misc.png")),
	CLIENT("Client", new ResourceLocation("minecraft", "autotool/category/client.png")),
	;

	public final String name;

    public final ResourceLocation logo;

    public boolean hovered;

    public final EasingAnimation toggleAnim = new EasingAnimation();

	Category(final String name, ResourceLocation logo) {
		this.name = name;
        this.logo = logo;
	}
}
