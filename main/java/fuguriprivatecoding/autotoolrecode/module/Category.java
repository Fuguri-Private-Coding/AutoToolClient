package fuguriprivatecoding.autotoolrecode.module;

public enum Category {
	COMBAT("Combat"),
	MOVE("Move"),
	VISUAL("Visual"),
	CONNECTION("Connection"),
	EXPLOIT("Exploit"),
	LEGIT("Legit"),
	PLAYER("Player"),
	MISC("Misc"),
	CLIENT("Client"),
	;

	public final String name;
	Category(final String name) {
		this.name = name;
	}
}
