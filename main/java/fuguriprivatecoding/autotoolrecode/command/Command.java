package fuguriprivatecoding.autotoolrecode.command;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.guis.console.ConsoleScreen;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
	private final String name;
	private final String usage;
	private final List<String> aliases;

	protected final ConsoleScreen console;

	public Command(String name, String usage, String... aliases) {
		this.name = name;
		this.usage = usage;
		this.aliases = Arrays.asList(aliases);
		console = Client.INST.getConsole();
	}

	public abstract void execute(String[] args);

	protected void usage() {
		ClientUtils.chatLog(usage);
		console.log(usage);
	}

	public void addMessage(String msg) {
		console.log(msg);
		ClientUtils.chatLog(msg);
	}
}
