package me.hackclient.command;

import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.guis.console.ConsoleGuiScreen;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
	private final String name;
	private final String usage;
	private final List<String> aliases;

	protected final ConsoleGuiScreen console;

	public Command(String name, String usage, String... aliases) {
		this.name = name;
		this.usage = usage;
		this.aliases = Arrays.asList(aliases);
		console = Client.INST.getConsole();
	}

	public abstract void execute(String[] args);

	protected void usage() {
		console.log(usage);
	}
}
