package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.world.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

@ModuleInfo(name = "Disabler", category = Category.PLAYER, description = "Позволяет ослаблять/отключать античит.")
public class Disabler extends Module {

	CheckBox breaking = new CheckBox("Breaking", this, false);

	@Override
	public void onEvent(Event event) {
		if (event instanceof PacketEvent e) {
			Packet packet = e.getPacket();

			switch (packet) {
				case C07PacketPlayerDigging c07 -> {
					if ((c07.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
					|| c07.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) && breaking.isToggled()) {
						e.cancel();
					}
				}
                default -> {}
            }

		}
	}
}
