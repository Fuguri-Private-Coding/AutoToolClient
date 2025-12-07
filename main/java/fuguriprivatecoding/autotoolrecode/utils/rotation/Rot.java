package fuguriprivatecoding.autotoolrecode.utils.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

@Setter
public class Rot {
	public static final Rot ZERO = new Rot(0,0);
	@Getter float yaw, pitch;

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

    public Rot limit(float yaw, float pitch) {
        return new Rot(
            MathHelper.clamp(getYaw(), -yaw, yaw),
            MathHelper.clamp(getPitch() , -pitch, pitch)
        );
    }

    public Rot divine(float yaw, float pitch) {
        return new Rot(
            getYaw() / yaw,
            getPitch() / pitch
        );
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
		return RotUtils.fixDelta(this);
	}
}
