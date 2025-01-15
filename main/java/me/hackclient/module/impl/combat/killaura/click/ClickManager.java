package me.hackclient.module.impl.combat.killaura.click;

import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.ThreadLocalRandom;

public class ClickManager implements InstanceAccess {

	final StopWatch timer, hitSelectTimer;
	int nextDelay;

	int hitSelectDelay = 500;
	public int clicks;

	public int hurtTime;

	public ClickManager() {
		timer = new StopWatch();
		hitSelectTimer = new StopWatch();
	}

	public void click(final EntityLivingBase target, final IClickingCFG cfg) {
		if (cfg.clickFix() && clicks > 1) {
			clicks = 1;
		}

		for (int i = 0; i < clicks; i++) {
			if (hitSelectTimer.reachedMS() >= hitSelectDelay || mc.thePlayer.hurtTime > (cfg.mineBlazeKbFix() ? cfg.minPlayerHurtTime() : 0) ) {
				mouseClick(cfg.minCps(), cfg.maxCps(), target);
				if (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target
						&& hitSelectTimer.reachedMS() >= hitSelectDelay) {
					hitSelectTimer.reset();
					hitSelectDelay = 500 + RandomUtils.nextInt(cfg.minRandomizeDelay(), cfg.maxRandomizeDelay());
				}
			}
		}

		clicks = 0;
	}

	void mouseClick(int minCps, int maxCps, EntityLivingBase target) {
		if (mc.currentScreen != null)
			return;

		Entity entity = RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity1 -> true);
		if (entity == null || entity == target) {
			mc.clickMouse();
			nextDelay = RandomUtils.nextInt(
					1000 / maxCps,
					1000 / minCps
			);
		}
	}

	public void checkTime() {
		if (timer.reachedMS() >= nextDelay) {
			clicks++;
			timer.reset();
		}
	}
}