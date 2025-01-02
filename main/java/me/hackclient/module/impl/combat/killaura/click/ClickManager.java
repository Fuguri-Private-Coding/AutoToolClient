package me.hackclient.module.impl.combat.killaura.click;

import me.hackclient.utils.interfaces.InstanceAccess;
import me.hackclient.utils.rotation.RayCastUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentScore;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.RandomUtils;

public class ClickManager implements InstanceAccess {

	private final StopWatch timer, clickTimer;
	private int nextDelay;
	public int clicks;

	public int hurtTime;

	public ClickManager() {
		timer = new StopWatch();
		clickTimer = new StopWatch();
	}

	public void click(final EntityLivingBase target, final IClickingCFG cfg) {
		if (clickTimer.reachedMS() >= 500 && clicks == 0) {
			clicks++;
		}

		if (cfg.clickFix() && clicks > 1) {
			clicks = 1;
		}

		for (int i = 0; i < clicks; i++) {
			if (clickTimer.reachedMS() >= 500 || mc.thePlayer.hurtTime > (cfg.mineBlazeKbFix() ? cfg.minPlayerHurtTime() : 0)) {
				mouseClick(cfg.minCps(), cfg.maxCps(), target);
				if (RayCastUtils.raycastEntity(3, Rotation.getServerRotation().getYaw(), Rotation.getServerRotation().getPitch(), entity -> true) == target
				&& clickTimer.reachedMS() >= 500) {
					clickTimer.reset();
				}
			}
		}

		clicks = 0;
	}

	private void mouseClick(int minCps, int maxCps, EntityLivingBase target) {
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
			nextDelay = 1000 / 18;
		}
	}
}