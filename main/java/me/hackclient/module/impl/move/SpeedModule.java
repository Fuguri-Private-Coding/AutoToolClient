package me.hackclient.module.impl.move;

import me.hackclient.event.Event;
import me.hackclient.event.events.MoveEvent;
import me.hackclient.event.events.MoveFlyingEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Speed", category = Category.MOVE, key = Keyboard.KEY_V)
public class SpeedModule extends Module {

	@Override
	public void onEvent(Event event) {
		super.onEvent(event);
		if (event instanceof MoveFlyingEvent moveFlyingEvent) {
			if (moveFlyingEvent.getForward() > 0) {
				moveFlyingEvent.setYaw(mc.thePlayer.rotationYaw - 45);
			}
		}
		if (event instanceof MoveEvent moveEvent) {
			if (moveEvent.getForward() > 0) {
				moveEvent.setStrafe(moveEvent.getStrafe() - 1);
			}
		}
	}
}
