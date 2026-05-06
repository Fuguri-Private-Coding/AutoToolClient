package fuguriprivatecoding.autotoolrecode.utils.value;

import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Vec3;
import org.joml.Vector2f;
import org.joml.Vector2i;

@UtilityClass
public class Constants {
    public final Vec3 VEC3_ZERO = new Vec3(0, 0, 0);

    public final Vector2f VEC2F_ZERO = new Vector2f(0, 0);
    public final Vector2i VEC2I_ZERO = new Vector2i(0, 0);

    public final Rot ROT_ZERO = new Rot(0, 0);
    public final Rot ROT_MAX = new Rot(180, 180);
}
