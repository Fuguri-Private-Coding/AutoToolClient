package me.hackclient.utils.rotation;

public class Delta extends Rotation {

	public Delta() {
		setYaw(0);
		setPitch(0);
	}

	public Delta(float yaw, float pitch) {
		setYaw(yaw);
		setPitch(pitch);
	}

	@Override
	public Delta copy() {
		return new Delta(getYaw(), getPitch());
	}
}
