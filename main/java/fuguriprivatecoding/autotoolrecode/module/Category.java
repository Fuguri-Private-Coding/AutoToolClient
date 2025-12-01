package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import fuguriprivatecoding.autotoolrecode.Client;
import net.minecraft.util.ResourceLocation;
import lombok.Getter;

@Getter
public enum Category {
	COMBAT("Combat", Client.INST.of("category/combat.png")),
	MOVE("Move", Client.INST.of("category/move.png")),
	VISUAL("Visual", Client.INST.of("category/visual.png")),
	CONNECTION("Connect", Client.INST.of("category/connection.png")),
	LEGIT("Legit", Client.INST.of("category/legit.png")),
	PLAYER("Player", Client.INST.of("category/player.png")),
	MISC("Misc", Client.INST.of("category/misc.png")),
	CLIENT("Client", Client.INST.of("category/client.png")),
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
