package me.hackclient.module;

public enum Category {
	COMBAT("Combat"),
	MOVE("Move"),
	VISUAL("Visual"),
	CONNECTION("Connection"),
	EXPLOIT("Exploit"),
	LEGIT("Legit"),
	PLAYER("Player"),
	MISC("Misc"),
	;

	public final String name;
	Category(final String name) {
		this.name = name;
	}
}
