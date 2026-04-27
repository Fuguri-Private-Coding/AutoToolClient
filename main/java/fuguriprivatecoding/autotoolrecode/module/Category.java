package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.Client;
import net.minecraft.util.ResourceLocation;
import lombok.Getter;

@Getter
public enum Category {
	COMBAT("Combat", Client.of("category/combat.png")),
	MOVE("Move", Client.of("category/move.png")),
	VISUAL("Visual", Client.of("category/visual.png")),
	CONNECTION("Connect", Client.of("category/connection.png")),
	LEGIT("Legit", Client.of("category/legit.png")),
	PLAYER("Player", Client.of("category/player.png")),
	MISC("Misc", Client.of("category/misc.png")),
	CLIENT("Client", Client.of("category/client.png")),
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
