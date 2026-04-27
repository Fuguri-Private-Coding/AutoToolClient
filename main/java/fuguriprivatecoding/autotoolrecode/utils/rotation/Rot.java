package fuguriprivatecoding.autotoolrecode.utils.rotation;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.joml.Vector2f;

@Setter
public class Rot {
	@Getter float yaw, pitch;

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

    public Rot lerp(Rot end, float deltaX, float deltaY) {
        return new Rot(
            MathHelper.lerp(deltaX, yaw, end.getYaw()),
            MathHelper.lerp(deltaY, pitch, end.getPitch())
        );
    }

    public Rot lerp(Rot end, float delta) {
        return new Rot(
            MathHelper.lerp(delta, yaw, end.getYaw()),
            MathHelper.lerp(delta, pitch, end.getPitch())
        );
    }

    public Rot deltaTo(Rot end) {
        return new Rot(
            MathHelper.wrapDegree(end.getYaw() - yaw),
            end.getPitch() - pitch
        );
    }

    public Rot fix() {
        final float gcd = RotUtils.getMouseGCD();
        this.yaw = (float) MathUtils.round(this.yaw, gcd);
        this.pitch = (float) MathUtils.round(this.pitch, gcd);
        return this;
    }

	public Rot plus(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        this.pitch = MathHelper.clamp(this.pitch, -90, 90);
        return this;
	}

	public Rot plus(Rot add) {
		return plus(add.yaw, add.pitch);
	}

    public Rot minus(float yaw, float pitch) {
        this.yaw -= yaw;
        this.pitch -= pitch;
        this.pitch = MathHelper.clamp(this.pitch, -90, 90);
        return this;
    }

	public Rot minus(Rot rotation) {
		return minus(rotation.yaw, rotation.pitch);
	}

    public Rot multiple(float multiplier) {
        this.yaw *= multiplier;
        this.pitch *= multiplier;
        return this;
    }

    public Rot multiple(Rot multiplier) {
        this.yaw *= multiplier.yaw;
        this.pitch *= multiplier.pitch;
        return this;
    }

    public Rot limit(float yaw, float pitch) {
        this.yaw = MathHelper.clamp(this.yaw, -yaw, yaw);
        this.pitch = MathHelper.clamp(this.pitch, -pitch, pitch);
        return this;
    }

	public Rot limit(Rot speed) {
        this.yaw = MathHelper.clamp(this.yaw, -speed.yaw, speed.yaw);
        this.pitch = MathHelper.clamp(this.pitch, -speed.pitch, speed.pitch);
        return this;
	}

    public Rot divine(float yaw, float pitch) {
        this.yaw /= yaw;
        this.pitch /= pitch;
        return this;
    }

    public Rot divide(Rot divisor) {
        this.yaw /= divisor.yaw;
        this.pitch /= divisor.pitch;
        return this;
    }

	public Rot copy() {
		return new Rot(
			yaw,
			Math.clamp(pitch, -90, 90)
		);
	}

	public float length() {
		return (float) Math.hypot(yaw, pitch);
	}

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();

        object.addProperty("yaw", yaw);
        object.addProperty("pitch", pitch);

        return object;
    }

    public static Rot fromJsonObject(JsonObject object) {
        if (!object.has("yaw") || !object.has("pitch")) {
            System.out.println("missing yaw or pitch to create rot from json-object");
            return null;
        }

        return new Rot(object.get("yaw").getAsFloat(), object.get("pitch").getAsFloat());
    }
}
