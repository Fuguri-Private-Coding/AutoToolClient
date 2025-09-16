package fuguriprivatecoding.autotoolrecode.deeplearn.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import lombok.Getter;
import net.minecraft.util.Vec3;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.util.vector.Vector2f;

@Getter
public class TrainingData {
    // Getters
    @SerializedName(CURRENT_DIRECTION_VECTOR)
    private final Vec3 currentVector;
    @SerializedName(PREVIOUS_DIRECTION_VECTOR)
    private final Vec3 previousVector;
    @SerializedName(TARGET_DIRECTION_VECTOR)
    private final Vec3 targetVector;
    @SerializedName(DELTA_VECTOR)
    private final Vector2f velocityDelta;
    @SerializedName(P_DIFF)
    private final Vec3 playerDiff;
    @SerializedName(T_DIFF)
    private final Vec3 targetDiff;
    @SerializedName(DISTANCE)
    private final float distance;
    @SerializedName(HURT_TIME)
    private final int hurtTime;
    @SerializedName(AGE)
    private final int age;

    public static final String CURRENT_DIRECTION_VECTOR = "a";
    public static final String PREVIOUS_DIRECTION_VECTOR = "b";
    public static final String TARGET_DIRECTION_VECTOR = "c";
    public static final String DELTA_VECTOR = "d";
    public static final String HURT_TIME = "e";
    public static final String AGE = "f";
    public static final String P_DIFF = "g";
    public static final String T_DIFF = "h";
    public static final String DISTANCE = "i";

    public TrainingData(Vec3 currentVector, Vec3 previousVector, Vec3 targetVector,
                        Vector2f velocityDelta, Vec3 playerDiff, Vec3 targetDiff,
                        float distance, int hurtTime, int age) {
        this.currentVector = currentVector;
        this.previousVector = previousVector;
        this.targetVector = targetVector;
        this.velocityDelta = velocityDelta;
        this.playerDiff = playerDiff;
        this.targetDiff = targetDiff;
        this.distance = distance;
        this.hurtTime = hurtTime;
        this.age = age;
    }

    public Rot getCurrentRotation() {
        return Rot.fromRotationVec(currentVector);
    }

    public Rot getTargetRotation() {
        return Rot.fromRotationVec(targetVector);
    }

    public Rot getPreviousRotation() {
        return Rot.fromRotationVec(previousVector);
    }

    public Rot getTotalDelta() {
        return RotUtils.getDelta(getCurrentRotation(), getTargetRotation());
    }

    public Rot getPreviousVelocityDelta() {
        return  RotUtils.getDelta(getPreviousRotation(), getCurrentRotation());
    }

    public float[] getAsInput() {
        return new float[] {
                getTotalDelta().getYaw(),
                getTotalDelta().getPitch(),
                getPreviousVelocityDelta().getYaw(),
                getPreviousVelocityDelta().getPitch(),
                (float) (targetDiff.horizontalLength() + playerDiff.horizontalLength()),
                distance
        };
    }

    public float[] getAsOutput() {
        return new float[] {velocityDelta.x, velocityDelta.y};
    }

    public static List<TrainingData> parse(File... files) {
        List<TrainingData> result = new ArrayList<>();
        for (File file : files) {
            result.addAll(parse(file));
        }
        return result;
    }

    public static final Gson publicGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static List<TrainingData> parse(File file) {
        List<TrainingData> data = new ArrayList<>();
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    data.addAll(parse(child));
                }
            }
        } else if (FilenameUtils.getExtension(file.getName()).equals("json")) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                List<TrainingData> parsed = publicGson.fromJson(
                        reader,
                        new TypeToken<List<TrainingData>>(){}.getType()
                );
                data.addAll(parsed);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
        return data;
    }
}