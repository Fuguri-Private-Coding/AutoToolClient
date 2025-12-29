package fuguriprivatecoding.autotoolrecode.command;

import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.gui.console.ConsoleScreen;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
	private final String name;
	private final String usage;
	private final List<String> aliases;

	public Command(String name, String usage, String... aliases) {
		this.name = name;
		this.usage = usage;
		this.aliases = Arrays.asList(aliases);
	}

	public abstract void execute(String[] args);

	protected void usage() {
		ClientUtils.chatLog(usage);
		ConsoleScreen.log(usage);
	}

	public void addMessage(String msg) {
		ConsoleScreen.log(msg);
	}
}
