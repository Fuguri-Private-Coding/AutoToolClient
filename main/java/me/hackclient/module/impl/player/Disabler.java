package me.hackclient.module.impl.player;

import me.hackclient.event.Direction;
import me.hackclient.event.Event;
import me.hackclient.event.events.MotionEvent;
import me.hackclient.event.events.PacketEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.util.ChatComponentText;

import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "Disabler", category = Category.PLAYER)
public class Disabler extends Module {

	private final CopyOnWriteArrayList<C0FPacketConfirmTransaction> c0fs = new CopyOnWriteArrayList<>();

	public void onEnable() {
		c0fs.clear();
	}

	public void onDisable() {
		while (!c0fs.isEmpty()) {
			mc.getNetHandler().getNetworkManager().sendPacketNoEvent(c0fs.get(0));
			c0fs.remove(0);
		}
	}

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof PacketEvent packetEvent) {
			if (packetEvent.getDirection() == Direction.INCOMING)
				return;

			if (packetEvent.getPacket() instanceof C0FPacketConfirmTransaction c0f) {
				packetEvent.setCanceled(true);
				c0fs.add(c0f);
				mc.thePlayer.addChatMessage(new ChatComponentText("canceled C0f " + c0fs.size()));
			}

			int delay = 20;
			if (c0fs.size() < delay) {
				packetEvent.setCanceled(true);
			}
		}
		if (event instanceof MotionEvent) {
			int delay = 500;
			while (c0fs.size() > delay) {
				mc.getNetHandler().getNetworkManager().sendPacketNoEvent(c0fs.get(0));
				c0fs.remove(0);
			}
		}
	}
}
