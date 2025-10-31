package fuguriprivatecoding.autotoolrecode.module;

import fuguriprivatecoding.autotoolrecode.utils.animation.EasingAnimation;
import lombok.Getter;

@Getter
public enum Category {
	COMBAT("Combat"),
	MOVE("Move"),
	VISUAL("Visual"),
	CONNECTION("Connect"),
	EXPLOIT("Exploit"),
	LEGIT("Legit"),
	PLAYER("Player"),
	MISC("Misc"),
	CLIENT("Client"),
	;

	public final String name;

    public boolean hovered;

    public final EasingAnimation toggleAnim = new EasingAnimation();

	Category(final String name) {
		this.name = name;
	}
}
