package fuguriprivatecoding.autotoolrecode.utils.rotation;

import net.minecraft.util.MathHelper;

public class Delta extends Rot {

	public Delta() {
		setYaw(0);
		setPitch(0);
	}

	public Delta(float yaw, float pitch) {
		setYaw(yaw);
		setPitch(pitch);
	}

	public Delta limit(float yaw, float pitch) {
		return new Delta(
				MathHelper.clamp(getYaw(), -yaw, yaw),
				MathHelper.clamp(getPitch() , -pitch, pitch)
		);
	}

	public Delta multi(float yaw, float pitch) {
		return new Delta(
				getYaw() * yaw,
				getPitch() * pitch
		);
	}

	public Delta divine(float yaw, float pitch) {
		return new Delta(
				getYaw() / yaw,
				getPitch() / pitch
		);
	}

	@Override
	public Delta copy() {
		return new Delta(getYaw(), getPitch());
	}
}
