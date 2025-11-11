package fuguriprivatecoding.autotoolrecode.utils.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

@Setter
public class Rot {
	public static final Rot ZERO = new Rot(0,0);
	@Getter @Setter static boolean changed;
	@Getter static Rot serverRotation = new Rot();
	@Getter float yaw, pitch;

	public static void setServerRotation(Rot serverRotation) {
		Rot.serverRotation = serverRotation.copy();
		changed = true;
	}

	public static Rot getLastReported() {
		return new Rot(
				MathHelper.wrapDegree(Minecraft.getMinecraft().thePlayer.lastReportedYaw),
				MathHelper.wrapDegree(Minecraft.getMinecraft().thePlayer.lastReportedPitch)
		);
	}

	public static Rot fromRotationVec(Vec3 lookVec) {
		return new Rot(
				MathHelper.wrapDegree((float) (Math.toDegrees(Math.atan2(lookVec.zCoord, lookVec.xCoord)) - 90)),
				MathHelper.wrapDegree((float) (-Math.toDegrees(Math.atan2(lookVec.yCoord, Math.sqrt(lookVec.xCoord * lookVec.xCoord + lookVec.zCoord * lookVec.zCoord)))))
		);
	}

	public Vec3 getVec3d() {
		return Entity.getVecForRotation(pitch, yaw);
	}

	public Vector2f getVec2f() {
		return new Vector2f(yaw, pitch);
	}

	public Rot() {
		yaw = 0;
		pitch = 0;
	}

	public Rot(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Rot add(float yaw, float pitch) {
		return new Rot(
				this.yaw + yaw,
				MathHelper.clamp(this.pitch + pitch, -90, 90)
		);
	}

	public Rot add(Rot add) {
		return add(add.yaw, add.pitch);
	}

	public Rot subtract(float yaw, float pitch) {
		return add(-yaw, -pitch);
	}

	public Rot subtract(Rot rotation) {
		return subtract(rotation.yaw, rotation.pitch);
	}

    public Rot multiplier(float multiplier) {
        return new Rot(yaw * multiplier, pitch * multiplier);
    }

	public Rot copy() {
		return new Rot(
				yaw,
				Math.clamp(pitch, -90, 90)
		);
	}

	public double hypot() {
		return Math.hypot(yaw, pitch);
	}

	public Rot fix() {
		Delta delta = RotUtils.getDelta(serverRotation, this);
		delta = RotUtils.fixDelta(delta);
		return new Rot(
				serverRotation.getYaw() + delta.getYaw(),
				Math.clamp(serverRotation.getPitch() + delta.getPitch(), -90, 90)
		);
	}
}
