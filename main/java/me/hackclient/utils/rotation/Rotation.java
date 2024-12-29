package me.hackclient.utils.rotation;

import net.minecraft.util.MathHelper;

public class Rotation {
	private static Rotation serverRotation = new Rotation();

	private float yaw, pitch;

	public Rotation() {
		yaw = 0;
		pitch = 0;
	}

	public Rotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
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

	public static Rotation getServerRotation() {
		return serverRotation;
	}

	public static void setServerRotation(Rotation serverRotation) {
		Rotation.serverRotation = serverRotation;
	}
}
