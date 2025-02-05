package me.hackclient.utils.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;

public class Rotation {
	@Getter @Setter static boolean changed;
	@Getter static Rotation serverRotation = new Rotation();
	@Getter @Setter float yaw, pitch;

	public static void setServerRotation(Rotation serverRotation) {
		Rotation.serverRotation = serverRotation.copy();
		changed = true;
	}

	public Rotation() {
		yaw = 0;
		pitch = 0;
	}

	public Rotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Rotation add(float yaw, float pitch) {
		return new Rotation(
				this.yaw + yaw,
				MathHelper.clamp(this.pitch + pitch, -90, 90)
		);
	}

	public Rotation add(Rotation add) {
		return add(add.yaw, add.pitch);
	}

	public Rotation subtract(float yaw, float pitch) {
		return add(-yaw, -pitch);
	}

	public Rotation subtract(Rotation rotation) {
		return subtract(rotation.yaw, rotation.pitch);
	}

	public Rotation copy() {
		return new Rotation(
				yaw,
				pitch
		);
	}

	public double hypot() {
		return Math.hypot(yaw, pitch);
	}

	public Rotation fix() {
		Delta delta = RotationUtils.getDelta(serverRotation, this);
		delta = RotationUtils.fixDelta(delta);
		return new Rotation(
				serverRotation.getYaw() + delta.getYaw(),
				serverRotation.getPitch() + delta.getPitch()
		);
	}
}
