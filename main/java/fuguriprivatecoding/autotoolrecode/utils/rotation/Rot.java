package fuguriprivatecoding.autotoolrecode.utils.rotation;

import com.google.gson.JsonObject;
import fuguriprivatecoding.autotoolrecode.utils.math.MathUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.MathHelper;
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
        return lerp(end, delta, delta);
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

    public Rot fixed() {
        final float gcd = RotUtils.getMouseGCD();
        return new Rot(
            (float) MathUtils.round(this.yaw, gcd),
            (float) MathUtils.round(this.pitch, gcd)
        );
    }

	public Rot plus(Rot add) {
		return new Rot(
            this.yaw + add.yaw,
            this.pitch + add.pitch
        );
	}

	public Rot minus(Rot rot) {
		return new Rot(
            this.yaw - rot.yaw,
            this.pitch - rot.pitch
        );
	}

    public Rot normalized() {
        float length = lengthFloat();

        if (length == 0) {
            return new Rot();
        }

        return new Rot(
            yaw / length,
            pitch / length
        );
    }

    public float lengthFloat() {
        return (float) Math.hypot(this.yaw, this.pitch);
    }

    public double lengthDouble() {
        return Math.hypot(this.yaw, this.pitch);
    }

    public Rot multiplied(Rot factor) {
        return new Rot(
            yaw * factor.yaw,
            pitch * factor.pitch
        );
    }

    public Rot multiplied(float factor) {
        return new Rot(
            yaw * factor,
            pitch * factor
        );
    }

    public Rot divided(Rot factor) {
        return new Rot(
            yaw / factor.yaw,
            pitch / factor.pitch
        );
    }

    public Rot divided(float factor) {
        return new Rot(
            yaw / factor,
            pitch / factor
        );
    }

    public Rot limited(Rot speed) {
        return new Rot(
            Math.clamp(yaw, -speed.yaw, speed.yaw),
            Math.clamp(pitch, -speed.pitch, speed.pitch)
        );
    }

    public Rot limitedLine(Rot speed) {
        return limited(normalized().abs().multiplied(speed));
    }

    public Rot abs() {
        return new Rot(
            Math.abs(yaw),
            Math.abs(pitch)
        );
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
